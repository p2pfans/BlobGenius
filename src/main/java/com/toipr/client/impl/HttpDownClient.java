package com.toipr.client.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.client.DataClients;
import com.toipr.client.DownloadClient;
import com.toipr.client.ObjectManager;
import com.toipr.model.data.DataConst;
import com.toipr.util.HashHelper;
import com.toipr.util.Utils;
import com.toipr.util.threads.ThreadPoolWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpDownClient extends DefaultDataClient implements DownloadClient, Runnable {
    /**
     * 数字对象管理器
     */
    protected ObjectManager manager;

    protected Object lockObj = new Object();

    protected List<TaskEntry> lstTask = new ArrayList<TaskEntry>();
    protected List<TaskEntry> lstActive = new ArrayList<TaskEntry>();

    protected Map<String, TaskEntry> mapTask = new HashMap<String, TaskEntry>();

    public HttpDownClient(){
        this.manager = DataClients.getManager();
    }

    /**
     * 取消任务
     * @param handle
     */
    public void cancel(Object handle){
        TaskEntry task = (TaskEntry)handle;
        task.setState(DataConst.State_Cancel);
        synchronized (lockObj){
            lstTask.remove(task);
            lstActive.remove(task);
            mapTask.remove(task.getUuid());
        }
    }

    /**
     * 下载数据对象，存放到path目录
     * @param rid 资源ID
     * @param uuid 数字对象ID
     * @param path 存放路径
     * @return 任务句柄
     */
    public Object download(String rid, String uuid, String path){
        if(mapTask.containsKey(uuid)){
            return mapTask.get(uuid);
        }

        Object target = manager.getObject(rid, uuid);
        if(target==null){
            return null;
        }

        TaskEntry task = new TaskEntry(target, path);
        synchronized (lockObj) {
            lstTask.add(task);
            mapTask.put(uuid, task);
            if(worker==null){
                worker = new Thread(this);
                worker.start();
            }
        }
        return task;
    }

    /**
     * 下载数据块
     * @param rid 资源ID
     * @param boid 数据块ID
     * @param hash 数据校验码
     * @return 数据数组
     */
    public byte[] getBlobData(String rid, String boid, String hash){
        String link = finder.getServer(rid, "blobs", false);
        link += String.format("/data/download/blob/%s/%s", rid, boid);

        try {
            byte[] data = getData(link);
            if (data == null) {
                return null;
            }

            if (!Utils.isNullOrEmpty(hash)) {
                String str = HashHelper.computeHash(data, "SHA-256");
                if (str.compareTo(hash) != 0) {
                    return null;
                }
            }
            return data;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void run(){
        try {
            while (!isExit) {
                TaskEntry taskObj = null;
                synchronized (lockObj) {
                    if (lstTask.size() == 0 || lstActive.size()>=5) {
                        Thread.sleep(100);
                    } else {
                        taskObj = lstTask.remove(0);
                    }
                }

                if(taskObj!=null){
                    if(taskObj.prepare()){
                        synchronized (lockObj){
                            lstActive.add(taskObj);
                        }
                    } else {
                        synchronized (lockObj){
                            mapTask.remove(taskObj.getPath());
                        }
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    protected void onTaskJoin(TaskEntry taskObj){
        synchronized (lockObj){
            lstTask.add(taskObj);
            mapTask.put(taskObj.getUuid(), taskObj);
        }
    }

    protected void onTaskLeave(TaskEntry taskObj){
        synchronized (lockObj){
            lstTask.remove(taskObj);
            lstActive.remove(taskObj);
            mapTask.remove(taskObj.getUuid());
        }
    }

    /**
     * 数据下载任务, 支持按数据块多线程同步下载
     */
    public class DownloadJob implements Runnable {
        protected Object target;
        public Object getTarget(){
            return this.target;
        }
        public String getName(){
            JSONObject json = (JSONObject)target;
            return json.getString("name");
        }

        protected TaskEntry taskObj;
        public TaskEntry getTaskObj(){
            return this.taskObj;
        }

        /**
         * 待写入的数据块偏移
         */
        protected int offset=0;

        protected String uuid;
        protected String name;
        protected long doneSize = 0;
        protected long totalSize = -1;

        /**
         * 文件输出流
         */
        protected FileOutputStream writer;

        /**
         * 文件块索引列表
         */
        protected List<Object> lstTask;

        protected Map<Integer, byte[]> mapBlobs;

        public DownloadJob(Object target, TaskEntry taskObj){
            this.target = target;
            this.taskObj = taskObj;

            JSONObject json = (JSONObject)target;
            this.uuid = json.getString("uuid");
            this.name = json.getString("name");
            this.totalSize = json.getLong("size");
        }

        protected boolean init(){
            /**
             * 1. 判断文件是否已经存在
             */
            if(isRightContent()){
                taskObj.onTaskComplete(this);
                return true;
            }

            JSONObject json = (JSONObject)target;
            long size = json.getInteger("size");
            if(size==0){
                /**
                 * 创建空文件
                 */
                if(!createEmptyFile()){
                    taskObj.onTaskError(this);
                    if(listener!=null){
                        listener.errorNotify(uuid, name, true, null, target);
                    }
                    return false;
                }
                taskObj.onTaskComplete(this);
                return true;
            }

            /**
             * 2. 初始化数据块索引，比对已下载部分内容，支持断点续传
             */
            if(!initBlobs()){
                taskObj.onTaskError(this);
                if(listener!=null){
                    listener.errorNotify(uuid, name, true, null, target);
                }
                return false;
            }

            /**
             * 3. 通知文件开始下载
             */
            taskObj.onDownloadStart(this);
            if(listener!=null){
                listener.beforeNotify(uuid, name, totalSize, true, target);
            }

            /**
             * 4. 提交线程池等待后续执行
             */
            int nThreads = 1;//lstTask.size()>DataConst.max_thread_a_file?DataConst.max_thread_a_file:1;
            for(int i=0; i<nThreads; i++){
                ThreadPoolWorker.submit(this);
            }
            return true;
        }

        public void close(){
            synchronized (this) {
                lstTask.clear();
                if(mapBlobs!=null) {
                    mapBlobs.clear();
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    writer = null;
                }
            }
        }

        @Override
        public void run(){
            JSONObject json = null;
            try {
                while (!isExit && taskObj.getState()!=DataConst.State_Cancel) {
                    synchronized (this) {
                        if (lstTask.size() == 0) {
                            break;
                        }
                        json = (JSONObject) lstTask.remove(0);
                    }

                    /**
                     * 1. 下载数据块并校验数据
                     */
                    int index = json.getInteger("serial");
                    int size = json.getInteger("size");
                    String boid = json.getString("boid");
                    String hash = json.getString("hash");
                    byte[] data = getBlobData(taskObj.getRid(), boid, hash);
                    if (data == null || data.length != size) {
                        //todo 报告数据块错误
                        taskObj.onTaskError(this);
                        if(listener!=null){
                            listener.errorNotify(uuid, name, true, null, target);
                        }
                        break;
                    }

                    /**
                     * 2. 数据写入文件，或放入待写入区
                     */
                    if (!writeData(index, data)) {
                        taskObj.onTaskError(this);
                        if(listener!=null){
                            listener.errorNotify(uuid, name, true, null, target);
                        }
                        break;
                    }

                    /**
                     * 3. 通知进度事件监听器下载进度
                     */
                    if (listener != null) {
                        listener.progressNotify(uuid, name, totalSize, doneSize, true, target);
                    }

                    if (lstTask.isEmpty()) {
                        if(writer!=null){
                            writer.close();
                            writer = null;
                        }
                        /**
                         * 4. 比对文件校验码，不匹配则抛出异常
                         */
                        if(!isRightContent()){
                            throw new Exception("file content not right");
                        }

                        /**
                         * 5. 通知文件下载完成
                         */
                        taskObj.onTaskComplete(this);
                        if (listener != null) {
                            listener.completeNotify(uuid, name, totalSize, true, target);
                        }
                    }
                }
            }catch(Exception ex){
                /**
                 * 6. 通知父任务，下载异常中断
                 */
                taskObj.onTaskError(this);
                if(listener!=null){
                    listener.errorNotify(uuid, name, true, ex, target);
                }
                ex.printStackTrace();
            }
        }

        /**
         * 写入当前数据块与待写入区数据块
         * @param index 数据块序号
         * @param data 数据数组
         * @return true=成功
         */
        protected boolean writeData(int index, byte[] data){
            try {
                FileOutputStream fos = getFileStream();
                if(fos==null){
                    return false;
                }

                /**
                 * 数据块序号不对，放入待写入映射表
                 */
                if (index != offset) {
                    synchronized (this) {
                        if (mapBlobs == null) {
                            mapBlobs = new HashMap<Integer, byte[]>();
                        }
                        mapBlobs.put(index, data);
                    }
                    return true;
                }

                do {
                    fos.write(data);
                    synchronized (this) {
                        doneSize += data.length;
                        offset++;
                        /**
                         * 处理待写入映射表数据
                         */
                        if(mapBlobs==null || mapBlobs.isEmpty() || !mapBlobs.containsKey(offset)) {
                            //待写入区无数据，或者不是下一个要写入的数据块，直接返回
                            break;
                        }
                        data = mapBlobs.remove(offset);
                    }
                }while(!isExit);
                return true;
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        /**
         * 创建或获取文件流对象
         * @return
         */
        protected FileOutputStream getFileStream(){
            /**
             * Double Check加锁机制
             */
            if(writer==null) {
                synchronized (this) {
                    if (writer == null) {
                        try {
                            String path = taskObj.getPath();
                            path += File.separator + getName();
                            /**
                             * 注意：追加文件内容，支持断点续传
                             */
                            writer = new FileOutputStream(path, true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            return writer;
        }

        /**
         * 创建空文件
         * @return 成功=true
         */
        protected boolean createEmptyFile(){
            String path = taskObj.getPath();
            path += File.separator + getName();
            File fp = new File(path);
            if(fp.exists()){
                if(fp.length()==0) {
                    return true;
                }
                fp.delete();
            }

            try{
                FileOutputStream fos = new FileOutputStream(path);
                fos.close();
                return true;
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        protected boolean initBlobs(){
            /**
             * 从服务器获取数字对象的数据块索引
             */
            List<Object> rlist = manager.getBlobIds(taskObj.getRid(), uuid);
            if(rlist==null || rlist.size()==0){
                return false;
            }

            String path = taskObj.getPath();
            path += File.separator + getName();
            File fp = new File(path);
            if(!fp.exists() || fp.length()==0){
                lstTask = rlist;
                return true;
            }

            JSONObject json = (JSONObject)target;
            long size = json.getLong("size");
            if(fp.length()>size){
                /**
                 * 本地文件更大，出现错误，直接删除
                 */
                fp.delete();
                lstTask = rlist;
                return true;
            }

            int blobSize = json.getInteger("blobSize");
            if(fp.length()%blobSize != 0){
                /**
                 * 当前文件不是数据块整数倍，直接删除
                 */
                fp.delete();
                lstTask = rlist;
                return true;
            }

            /**
             * 判断已有文件块内容是否正确
             */
            boolean isNotRight = false;
            for(Object temp : rlist){
                if(!isBlobRight(path, blobSize, temp)){
                    isNotRight = true;
                    break;
                }
            }
            if(isNotRight){
                /**
                 * 文件块内容与服务器不一致，删除文件，从头开始下载
                 */
                fp.delete();
                lstTask = rlist;
            } else {
                /**
                 * 断点续传, 继续下载未完成的数据块
                 */
                int blobCount = (int)(fp.length() / blobSize);
                for(int i=blobCount; i<rlist.size(); i++){
                    lstTask.add(rlist.get(i));
                }
            }
            return true;
        }

        /**
         * 判断文件块内容与服务器是否一致
         * @param path 文件路径
         * @param blobSize 数据块大小
         * @param blob 数据块索引对象
         * @return true=内容一致 false=不一致或异常
         */
        protected boolean isBlobRight(String path, int blobSize, Object blob){
            try{
                JSONObject json = (JSONObject)blob;
                String hash = json.getString("hash");
                if(!Utils.isNullOrEmpty(hash)){
                    return true;
                }

                long index = json.getInteger("serial");

                int size = json.getInteger("size");
                byte[] data = new byte[size];
                try(FileInputStream fis = new FileInputStream(path)){
                    if(index>0){
                        long offset = index * blobSize;
                        fis.skip(offset);
                    }
                    if(!Utils.readAll(data, data.length, fis)){
                        return false;
                    }
                }

                String hashStr = HashHelper.computeHash(data, "SHA-256");
                if(hash.compareToIgnoreCase(hashStr)!=0){
                    return false;
                }
                return true;
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        /**
         * 判断文件是否存在，并且校验码是否相同
         * @return true=成功
         */
        protected boolean isRightContent() {
            try {
                JSONObject json = (JSONObject)target;
                long size = json.getLong("size");
                String path = taskObj.getPath();
                path += File.separator + getName();
                /**
                 * 1. 文件存在且大小相同
                 */
                File fp = new File(path);
                if(!fp.exists() || fp.length()!=size){
                    return false;
                }

                /**
                 * 2. 比较文件校验码是否相同
                 */
                String hash = json.getString("hash");
                if(Utils.isNullOrEmpty(hash)){
                    return true;
                }
                String hashStr = HashHelper.computeHash(path, "SHA-256");
                if(hash.compareToIgnoreCase(hashStr)==0){
                    return true;
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 下载任务
     * 功能1：数据对象，创建DownloadJob对象
     * 功能2：目录对象，获取子目录或当前目录数据对象，
     *       子目录加入主控线程等待执行，数据对象任务队列
     */
    public class TaskEntry {
        protected Object target;
        public Object getTarget(){
            return this.target;
        }

        protected String rid;
        public String getRid(){
            return this.rid;
        }
        protected String uuid;
        public String getUuid(){
            return this.uuid;
        }

        protected String path;
        public String getPath(){
            return this.path;
        }

        protected int state = 0;
        public int getState(){
            return this.state;
        }
        public void setState(int state){
            this.state = state;
            if(state==DataConst.State_Cancel && !subTaskList.isEmpty()){
                /**
                 * 取消正在排队执行的子任务
                 */
                for(TaskEntry temp : subTaskList){
                    temp.setState(DataConst.State_Cancel);
                }
            }
        }

        protected TaskEntry parent;
        public TaskEntry getParent(){
            return this.parent;
        }
        public void setParent(TaskEntry parent){
            this.parent = parent;
        }

        protected int total = 0;
        public int getTotal(){
            return this.total;
        }

        protected List<TaskEntry> subTaskList = new ArrayList<TaskEntry>();
        protected List<DownloadJob> lstJobs = new ArrayList<DownloadJob>();

        public TaskEntry(Object target, String path){
            this.path = path;
            this.target = target;

            JSONObject json = (JSONObject)target;
            this.rid = json.getString("rid");
            this.uuid = json.getString("uuid");
        }

        public boolean prepare(){
            /**
             * 查看任务是否已取消
             */
            if(parent!=null && parent.getState()==DataConst.State_Cancel){
                return false;
            }
            this.state = DataConst.State_Prepare;

            JSONObject json = (JSONObject)target;
            int flags = json.getInteger("flags");
            if((flags& DataConst.DataFlags_Directory)==0){
                DownloadJob job = new DownloadJob(target, this);
                if(!job.init()) {
                    return false;
                }
                lstJobs.add(job);
                return true;
            }

            /**
             * 创建目录
             */
            String line = path;
            if(line.endsWith(File.separator)){
                line += json.getString("name");
            } else {
                line += File.separator + json.getString("name");
            }
            File dir = new File(line);
            if(!dir.exists()){
                dir.mkdirs();
            }
            this.path = line;

            /**
             * 从目录对象获取子目录或文件对象列表
             */
            int start = 0;
            List<Object> rlist = new ArrayList<Object>();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("rid", json.get("rid"));
            params.put("pid", json.get("uuid"));
            do {
                total = manager.queryObjects(params, start, 500, rlist);
                if(rlist.size()==0){
                    break;
                }
                initJobs(rlist);
                start += rlist.size();
                rlist.clear();
            }while(start<total);
            return (start>0);
        }

        protected void initJobs(List<Object> rlist){
            for(Object temp : rlist){
                JSONObject json = (JSONObject)temp;
                int flags = json.getInteger("flags");
                if((flags& DataConst.DataFlags_Directory)==0){
                    DownloadJob job = new DownloadJob(temp, this);
                    if(job.init()){
                        lstJobs.add(job);
                    }
                } else {
                    TaskEntry task = new TaskEntry(json, path);
                    task.setParent(this);
                    /**
                     * 任务加入主控线程队列，采用广度优先树遍历算法思想
                     */
                    onTaskJoin(task);
                    subTaskList.add(task);
                }
            }
        }

        public void onDownloadStart(Object job){
            if(job instanceof DownloadJob){
                this.state = DataConst.State_Active;
            }
        }

        public void onTaskComplete(Object job){
            onJobLeave(job);
            if(lstJobs.isEmpty() && subTaskList.isEmpty()){
                this.state = DataConst.State_Completed;
                /**
                 * 通知主控线程，离开任务队列
                 */
                onTaskLeave(this);
                if(parent!=null){
                    /**
                     * 通知父任务，子任务执行完成
                     */
                    parent.onTaskComplete(this);
                }
            }
        }

        public void onTaskError(Object job){
            onJobLeave(job);

            this.state = DataConst.State_Error;
            /**
             * 通知主控线程，任务离开队列
             */
            onTaskLeave(this);

            if(parent!=null){
                /**
                 * 通知父任务，子任务执行错误
                 */
                parent.onTaskError(this);
            }
        }

        protected void onJobLeave(Object job){
            DownloadJob down = null;
            synchronized (this) {
                if (job instanceof DownloadJob) {
                    down = (DownloadJob)job;
                    lstJobs.remove(job);
                } else {
                    subTaskList.remove((TaskEntry) job);
                }
            }
            if(down!=null){
                down.close();
                down = null;
            }
        }
    }
}

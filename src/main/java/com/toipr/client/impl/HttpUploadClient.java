package com.toipr.client.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.client.*;
import com.toipr.model.data.DataConst;
import com.toipr.util.Utils;
import com.toipr.util.threads.ThreadPoolWorker;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * HTTP上传客户端
 * 功能1：上传文件或目录
 * 功能2：上传数据数组
 */
public class HttpUploadClient extends DefaultDataClient implements UploadClient, Runnable {
    protected Object lockObj = new Object();
    protected List<TaskEntry> lstTask = new LinkedList<TaskEntry>();
    protected List<TaskEntry> lstActive = new LinkedList<TaskEntry>();
    protected Map<String, TaskEntry> mapTask = new HashMap<String, TaskEntry>();

    /**
     * 数字对象管理器
     */
    protected ObjectManager manager;

    public HttpUploadClient(){
        this.manager = DataClients.getManager();
    }

    /**
     * 关闭上传客户端
     */
    public void close(){
        isExit = true;
        synchronized (lockObj) {
            lstTask.clear();
            mapTask.clear();
        }
    }

    /**
     * 取消任务
     * @param task
     */
    public void cancel(Object task){
        TaskEntry item = (TaskEntry)task;
        item.cancel();
        synchronized (lockObj) {
            lstTask.remove(task);
            mapTask.remove(item.getPath());
        }
    }

    /**
     * 上传文件或目录
     * @param rid 资源ID
     * @param pid 目录ID
     * @param path 文件路径
     * @return 任务句柄
     */
    public Object upload(String rid, String pid, String path){
        if(mapTask.containsKey(path)){
            return mapTask.get(path);
        }

        File file = new File(path);
        if(!file.exists()){
            return null;
        }

        TaskEntry item = new TaskEntry(rid, pid, file, null);
        item.setState(DataConst.State_Waiting);
        synchronized (lockObj) {
            lstTask.add(item);
            mapTask.put(path, item);
            if(worker==null){
                Thread myobj = new Thread(this);
                myobj.start();
                worker = myobj;
            }
        }
        return item;
    }

    /**
     * 上传数据块
     * @param rid 资源ID
     * @param uuid 数字对象ID
     * @param index 块序号
     * @param data 数据数组
     * @return true=成功
     */
    public boolean upload(String rid, String uuid, int index, byte[] data){
        DataPart part = getDataPart(rid, uuid, index, data);
        if(part==null){
            return false;
        }
        return uploadDataPart(rid, part);
    }

    @Override
    public void run(){
        try{
            while(!isExit){
                TaskEntry item = null;
                /**
                 * 1. 获取队列中的上传任务，最大并发10个任务
                 */
                if(lstTask.size()==0 || lstActive.size()>=10){
                    Thread.sleep(100);
                } else {
                    synchronized (lockObj) {
                        if(lstTask.size()>0) {
                            item = lstTask.remove(0);
                        }
                    }
                }

                if(item!=null){
                    /**
                     * 2. 初始化上传任务
                     */
                    if(item.prepare()) {
                        synchronized (lockObj) {
                            lstActive.add(item);
                        }
                    } else {
                        synchronized (lockObj){
                            mapTask.remove(item.getPath());
                        }
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 通知任务加入执行队列
     * @param taskObj
     */
    protected void onTaskJoin(TaskEntry taskObj){
        synchronized (lockObj){
            lstTask.add(taskObj);
            mapTask.put(taskObj.getPath(), taskObj);
        }
    }

    /**
     * 通知任务离开执行队列，完成或出错时回调
     * @param taskObj
     */
    protected void onTaskLeave(TaskEntry taskObj){
        synchronized (lockObj){
            lstTask.remove(taskObj);
            lstActive.remove(taskObj);
            mapTask.remove(taskObj.getPath());
        }
    }

    protected DataPart getDataPart(String rid, String uuid, int index, byte[] data){
        Object target = manager.getObject(rid, uuid);
        if(target==null){
            return null;
        }

        DataPart part = new DataPart(null, target);
        part.setIndex(index);
        part.setData(data);
        return part;
    }

    protected boolean uploadDataPart(String rid, DataPart part){
        HttpClient client = HttpClients.createDefault();

        /**
         * 构造Multipart对象，用multipart/form-data格式POST提交
         */
        HttpEntity entity = getMultipart(part);
        if(entity==null){
            return false;
        }

        /**
         * 上传失败重复执行N次
         */
        int times = 0;
        do {
            /**
             * 根据资源ID与数据类型，获取服务器地址
             * 多服务器的情况，每次请求的地址可能不一样
             */
            String link = finder.getServer(rid, "blobs", true);
            if(link==null){
                return false;
            }
            link += "/data/upload/blob";

            HttpPost post = new HttpPost(link);
            if(!Utils.isNullOrEmpty(token)) {
                post.addHeader("Cookie", String.format("token=%s;", token));
            }
            post.setEntity(entity);

            try {
                HttpResponse resp = client.execute(post);

                HttpEntity ret = resp.getEntity();
                String text = EntityUtils.toString(ret);
                JSONObject json = JSONObject.parseObject(text);
                int code = json.getInteger("status");
                if (code == 200) {//上传成功，直接返回
                    return true;
                }
                if(code==404){//对象不存在，返回错误
                    return false;
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            times++;
        }while(times<DataConst.def_fail_retry);
        return false;
    }

    protected HttpEntity getMultipart(DataPart part){
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            JSONObject json = (JSONObject) part.getTarget();
            if(!json.containsKey("uuid") || !json.containsKey("rid")){
                return null;
            }

            String str = json.getString("uuid");
            builder.addTextBody("uuid", str);

            str = json.getString("rid");
            builder.addTextBody("rid", str);

            if(json.containsKey("doid")){
                str = json.getString("doid");
                builder.addTextBody("doid", str);
            }

            builder.addBinaryBody("data", part.getData());

            builder.addTextBody("hash", part.getHash());
            builder.addTextBody("index", "" + part.getIndex());
            builder.addTextBody("size", "" + part.getSize());
            builder.addTextBody("offset", "" + part.getOffset());
            builder.addTextBody("total", "" + part.getTotal());
            return builder.build();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 上传工作项，负责数据块准备与上传，由线程池负责上传
     */
    public class UploadJob implements Runnable {
        /**
         * 文件或目录对象
         */
        protected File file;
        public File getFile(){
            return this.file;
        }

        /**
         * 数字对象
         */
        protected Object target;
        public Object getTarget(){
            return this.target;
        }

        /**
         * 上传任务
         */
        protected TaskEntry taskObj;
        public TaskEntry getTaskObj(){
            return this.taskObj;
        }

        protected ObjectManager manager;

        protected String uuid;
        protected String name;
        /**
         * 对象总长度
         */
        protected long totalSize = 0;
        /**
         * 已完成上传长度
         */
        protected long doneSize = 0;

        /**
         * 文件数据块队列
         */
        protected List<DataPart> parts = new LinkedList<DataPart>();

        public UploadJob(TaskEntry taskObj, File file, ObjectManager manager){
            this.file = file;
            this.taskObj = taskObj;
            this.manager = manager;
        }

        public void close(){
            synchronized (this){
                parts.clear();
            }
        }

        /**
         * 初始化文件对象传输，等待后续执行
         * @return true=成功
         */
        public boolean init(){
            if(isExit || !file.exists()){
                return false;
            }

            String name = file.getAbsolutePath();
            int pos = name.lastIndexOf(File.separator);
            if(pos>0){
                name = name.substring(pos+1);
            }
            this.name = name;

            String path = taskObj.getPath() + File.separator + name;
            /**
             * 1. 创建数字对象
             */
            if(Utils.isNullOrEmpty(taskObj.getUuid())) {
                target = manager.createObject(taskObj.getRid(), taskObj.getPid(), 0, name, path, file);
            } else {
                target = manager.createObject(taskObj.getRid(), taskObj.getUuid(), 0, name, path, file);
            }
            if(target==null) {
                taskObj.onTaskError(this);
                return false;
            }
            if(file.length()==0){
                taskObj.onTaskComplete(this);
                return true;
            }

            /**
             * 2. 通知监听器，开始传输文件
             */
            JSONObject json = (JSONObject)target;
            uuid = json.getString("uuid");
            long size = json.getLong("size");
            if(listener!=null){
                if(!listener.beforeNotify(uuid, this.name, size, false, target)){
                    taskObj.onTaskError(this);
                    return false;
                }
            }
            taskObj.onTaskStart(this);
            totalSize = size;

            /**
             * 3. 准备文件数据块队列，分块传输，降低服务器限制或其他错误发生的概率
             */
            if(!initBlobs(size, json)){
                taskObj.onTaskError(this);
                return false;
            }
            int blobCount = parts.size();
            if(blobCount==0){
                taskObj.onTaskComplete(this);
                return true;
            }

            /**
             * 4. 提交线程池等待后续执行
             */
            int nThreads = blobCount>DataConst.max_thread_a_file ? DataConst.max_thread_a_file : blobCount;
            for(int i=0; i<nThreads; i++){
                ThreadPoolWorker.submit(this);
            }
            return true;
        }

        protected boolean initBlobs(long size, JSONObject json){
            /**
             * 1. 获取服务器已经上传的数据块，支持断点续传
             */
            List<Object> blobs = manager.getBlobIds(taskObj.getRid(), uuid);

            long offset = 0;
            int blobSize =  DataConst.DataBlob_DefSize;
            if(json.containsKey("blobSize")) {
                blobSize = json.getInteger("blobSize");
            }

            /**
             * 2. 初始化本地文件数据块
             */
            int blobCount = (int)((size + blobSize-1)/blobSize);
            for(int i=0; i<blobCount; i++){
                DataPart part = new DataPart(file, target);
                part.setIndex(i);
                if(size>blobSize){
                    part.setSize(blobSize);
                } else {
                    part.setSize((int)size);
                }
                part.setOffset(offset);
                part.setTotal(blobCount);
                offset += blobSize;
                size -= blobSize;

                if(blobs!=null){
                    /**
                     * 3. 比对服务器与本地文件块数据校验码，排除已经上传的数据块
                     */
                    JSONObject blob = findBlob(i, blobs);
                    if(blob!=null){
                        int mysize = blob.getInteger("size");
                        String hash = blob.getString("hash");
                        if(part.blobExists(mysize, hash)){
                            continue;
                        }
                    }
                }
                parts.add(part);
            }
            return true;
        }

        protected JSONObject findBlob(int index, List<Object> blobs){
            for(Object item : blobs){
                JSONObject json = (JSONObject)item;
                int temp = json.getInteger("serial");
                if(temp==index){
                    return json;
                }
            }
            return null;
        }

        @Override
        public void run(){
            DataPart part = null;
            try {
                while (!isExit && taskObj.getState() != DataConst.State_Cancel) {
                    /**
                     * 1. 获取待上传的数据块
                     */
                    synchronized (this) {
                        if (parts.size() == 0) {
                            return;
                        }
                        part = parts.remove(0);
                    }

                    /**
                     * 2. 上传数据块
                     */
                    if (!uploadDataPart(taskObj.getRid(), part)) {
                        taskObj.onTaskError(this);
                        if (listener != null) {
                            listener.errorNotify(uuid, name, false, null, target);
                        }
                        return;
                    }

                    /**
                     * 3. 上传进度通知
                     */
                    synchronized (this) {
                        doneSize += part.getSize();
                    }
                    if (listener != null) {
                        listener.progressNotify(uuid, name, totalSize, doneSize, false, target);
                    }

                    if (doneSize == totalSize) {
                        manager.setState(taskObj.getRid(), uuid, DataConst.State_Completed);
                        /**
                         * 4. 通知上传任务完成
                         */
                        taskObj.onTaskComplete(this);
                        if (listener != null) {
                            listener.completeNotify(uuid, name, totalSize, false, target);
                        }
                    }
                }
            }catch(Exception ex){
                /**
                 * 5. 通知父任务，上传异常中断
                 */
                taskObj.onTaskError(this);
                if(listener!=null){
                    listener.errorNotify(uuid, name, true, ex, target);
                }
                ex.printStackTrace();
            }
        }
    }

    /**
     * 上传任务对象，负责目录对象创建与枚举
     */
    public class TaskEntry implements FileVisitor<Path> {
        /**
         * 文件或目录对象
         */
        private File root;
        private File getRoot() { return this.root; }
        public boolean isFile(){
            return this.root.isFile();
        }

        /**
         * 存储路径, 服务器端
         */
        private String path;
        public String getPath(){
            return this.path;
        }

        /**
         * 资源ID
         */
        private String rid;
        public String getRid(){
            return this.rid;
        }

        /**
         * 父目录ID
         */
        private String pid;
        public String getPid(){
            return this.pid;
        }

        /**
         * 当前目录ID
         */
        private String uuid;
        public String getUuid() {
            return this.uuid;
        }

        /**
         * 父任务对象, null为根任务
         */
        private TaskEntry parent;
        public TaskEntry getParent(){
            return this.parent;
        }

        /**
         * 任务状态
         */
        private int state = 0;
        public int getState(){
            return this.state;
        }
        public void setState(int state){
            this.state = state;
        }

        /**
         * 总条目数量
         */
        private int itemCount=0;
        public int getItemCount(){
            return this.itemCount;
        }
        private int doneItemCount=0;
        public int getDoneItemCount(){
            return this.doneItemCount;
        }
        private int errItemCount=0;
        public int getErrItemCount(){ return this.errItemCount;}

        /**
         * 总存储数量
         */
        private long totalSize=0;
        public long getTotalSize(){
            return this.totalSize;
        }
        private long doneTotalSize=0;
        private long getDoneTotalSize(){
            return this.doneTotalSize;
        }

        protected Object lockObj = new Object();
        /**
         * 文件上传任务列表
         */
        protected List<UploadJob> lstJobs = new LinkedList<UploadJob>();
        /**
         * 子任务列表, Composite设计模式
         */
        protected List<TaskEntry> subTaskList = new ArrayList<TaskEntry>();

        public TaskEntry(String rid, String pid, File root, TaskEntry parent){
            this.rid = rid;
            this.pid = pid;
            this.root = root;
            this.parent = parent;
        }

        /**
         * 中断任务执行
         */
        public void cancel(){
            this.state = DataConst.State_Cancel;
            if(subTaskList!=null){
                for(TaskEntry temp : subTaskList){
                    temp.cancel();
                }
            }
            lstJobs.clear();
        }

        /**
         * 初始化上传任务
         * @return true=成功
         */
        public boolean prepare(){
            if(isExit || this.state==DataConst.State_Cancel){
                return false;
            }
            this.state = DataConst.State_Prepare;

            /**
             * 文件对象，直接准备上传
             */
            if(root.isFile()){
                UploadJob job = new UploadJob(this, root, manager);
                if(!job.init()){
                    return false;
                }
                lstJobs.add(job);
                return true;
            }

            /**
             * 初始化目录对象
             */
            if(!initDir()){
                return false;
            }

            /**
             * 目录对象，枚举目录下文件或目录对象
             */
            Path start = Paths.get(root.getAbsolutePath());
            try {
                Files.walkFileTree(start, this);
                return true;
            } catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        protected boolean initDir(){
            if(pid.compareTo(DataConst.defaultDirectory)!=0){
                /**
                 * 1. 父目录不是根，获取父目录对象，初始化相对路径
                 */
                Object parent = manager.getObject(rid, pid);
                if(parent==null){
                    return false;
                }
                JSONObject json = (JSONObject)parent;
                this.path = json.getString("path");
            }

            /**
             * 2. 生成服务器目录名称与相对路径
             */
            String line = this.root.getAbsolutePath();
            int pos = line.lastIndexOf(File.separator);
            if(pos<0){
                return false;
            }
            line = line.substring(pos+1);
            if(Utils.isNullOrEmpty(this.path)) {
                this.path = line;
            } else {
                this.path += File.separator + line;
            }

            /**
             * 3. 创建服务器目录对象
             */
            Object dir = manager.createDir(rid, pid, 0, line, path);
            if(dir==null){
                return false;
            }
            JSONObject json = (JSONObject)dir;
            if(!json.containsKey("uuid")){
                return false;
            }
            this.uuid = json.getString("uuid");
            return true;
        }

        public void onTaskStart(Object job){
            this.state = DataConst.State_Active;
            synchronized (lockObj){
                if(job instanceof UploadJob) {
                    UploadJob obj = (UploadJob)job;
                    totalSize += obj.getFile().length();
                }
            }
        }

        public void onTaskComplete(Object job){
            onJobLeave(job);

            if(lstJobs.size()==0 && subTaskList.size()==0){
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
            synchronized (lockObj){
                errItemCount++;
            }

            /**
             * 通知主控线程，离开任务队列
             */
            onTaskLeave(this);
            if(parent!=null){
                /**
                 * 通知父任务，子任务执行出错
                 */
                parent.onTaskError(this);
            }
        }

        /**
         * 任务离开队列
         * @param job
         */
        protected void onJobLeave(Object job){
            UploadJob down = null;
            synchronized (this) {
                if (job instanceof UploadJob) {
                    down = (UploadJob)job;
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

        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if(isExit || state==DataConst.State_Cancel){
                return FileVisitResult.TERMINATE;
            }

            File file = dir.toFile();
            if(file.compareTo(root)==0){
                return FileVisitResult.CONTINUE;
            }

            TaskEntry task = new TaskEntry(rid, uuid, file, this);
            /**
             * 主任务线程排队执行
             */
            onTaskJoin(task);
            synchronized (lockObj){
                subTaskList.add(task);
                itemCount++;
            }
            /**
             * 只遍历自己的子目录与文件，采用广度优先遍历算法
             */
            return FileVisitResult.SKIP_SUBTREE;
        }

        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if(isExit || state==DataConst.State_Cancel){
                return FileVisitResult.TERMINATE;
            }

            UploadJob job = new UploadJob(this, file.toFile(), manager);
            if(!job.init()){
                return FileVisitResult.CONTINUE;
            }
            synchronized (lockObj){
                lstJobs.add(job);
                itemCount++;
            }

            try {
                while (!isExit && lstJobs.size() >= DataConst.max_file_inqueue && this.state!=DataConst.State_Cancel) {
                    Thread.sleep(100);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if(isExit || state==DataConst.State_Cancel){
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if(isExit || state==DataConst.State_Cancel){
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }
    }
}

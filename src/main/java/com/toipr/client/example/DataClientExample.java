package com.toipr.client.example;

import com.alibaba.fastjson.JSONObject;
import com.toipr.client.DataClients;
import com.toipr.client.DownloadClient;
import com.toipr.client.ObjectManager;
import com.toipr.client.UploadClient;
import com.toipr.model.data.DataConst;

import java.util.*;

public class DataClientExample {
    protected Map<String, Object> params = new HashMap<String, Object>();
    public boolean init(String... args){
        parse(args);
        if(!params.containsKey("uname") || !params.containsKey("upass") || !params.containsKey("server")){
            return false;
        }
        if(!DataClients.createFinder((String)params.get("server"))){
            return false;
        }
        if(!DataClients.createManager((String)params.get("uname"), (String)params.get("upass"), params)){
            return false;
        }
        if(!DataClients.createUploadClient((String)params.get("uname"), (String)params.get("upass"), params)){
            return false;
        }
        if(!DataClients.createDownloadClient((String)params.get("uname"), (String)params.get("upass"), params)){
            return false;
        }
        return true;
    }

    public void runTest(){
//        Object dir = testCreateDir("docs", "docs");

//        int flags = ObjectManager.DataBlob_Stream;
//        Object target = testCreateObject(dir, "testobj.blob", "docs\\testobj.blob", flags, 0, "application/oct-stream");
//
//        JSONObject json = (JSONObject)target;
//        Object gobj = testGetObject(json.getString("rid"), json.getString("doid"));

//        Random rand = new Random();
//        for(int i=0; i<5; i++){
//            byte[] data = new byte[1024*1024];
//            rand.nextBytes(data);
//            testUploadBlob(target, i, data);
//        }
//        testSetState(target, 0);

//        List<Object> rlist = new ArrayList<Object>();
//        int total = testListDir(json.getString("rid"), json.getString("pid"), rlist);
//        if(rlist.size()>0){
//            for(Object item : rlist){
//                System.out.println(item.toString());
//            }
//        }

//        testUploadDir(null, "g:\\data\\docs");
//        Object file = testUploadFile(dir, "g:\\EmEditor.zip");
        testDownObject("Ky4bJyO3", "VNU8lFUyMjGt", "g:\\data\\downtest");
    }

    /**
     * 创建目录对象
     * @param name 目录名称
     * @return
     */
    protected Object testCreateDir(String name, String path){
        ObjectManager manager = DataClients.getManager();

        int flags = 0;
        return manager.createDir("Ky4bJyO3", "root", flags, name, path);
    }

    /**
     * 创建数字对象
     * @param parent 父目录
     * @param name 对象名称
     * @param flags 对象属性
     * @param size 对象大小
     * @param mime 数据类型
     * @return 数字对象实例
     */
    protected Object testCreateObject(Object parent, String name, String path, int flags, long size, String mime){
        ObjectManager manager = DataClients.getManager();

        int blobSize = 1024*1024;

        String hash = "";
        String pid = "root";
        String rid = "Ky4bJyO3";
        if(parent!=null){
            JSONObject json = (JSONObject)parent;
            rid = json.getString("rid");
            pid = json.getString("doid");
            if(flags==0){
                flags = json.getInteger("flags");
            }
        }
        return manager.createObject(rid, pid, flags, name, path, hash, size, blobSize, mime);
    }

    /**
     * 获取数字对象描述信息
     * @param rid 资源ID
     * @param doid 数字对象ID
     * @return
     */
    protected Object testGetObject(String rid, String doid){
        ObjectManager manager = DataClients.getManager();

        return manager.getObject(rid, doid);
    }

    /**
     *
     * @param parent 父目录
     * @param filePath 文件目录
     * @return
     */
    protected Object testUploadFile(Object parent, String filePath){
        String rid, pid;
        JSONObject json = (JSONObject)parent;
        rid = json.getString("rid");
        pid = json.getString("doid");

        UploadClient client = DataClients.getUploadClient();
        return client.upload(rid, pid, filePath);
    }

    protected Object testUploadDir(Object parent, String filePath){
        String rid, pid;
        if(parent!=null) {
            JSONObject json = (JSONObject) parent;
            rid = json.getString("rid");
            pid = json.getString("doid");
        } else {
            rid = DataConst.defaultResource;
            pid = DataConst.defaultDirectory;
        }
        UploadClient client = DataClients.getUploadClient();
        return client.upload(rid, pid, filePath);
    }

    /**
     * 上传数据块
     * @param target 数据对象
     * @param index 数据块编号
     * @param data 数据数组
     * @return
     */
    protected Object testUploadBlob(Object target, int index, byte[] data){
        String rid, doid;
        JSONObject json = (JSONObject)target;
        rid = json.getString("rid");
        doid = json.getString("doid");

        UploadClient client = DataClients.getUploadClient();
        return client.upload(rid, doid, index, data);
    }

    protected void testSetState(Object target, int state){
        ObjectManager manager = DataClients.getManager();

        String rid, doid;
        JSONObject json = (JSONObject)target;
        rid = json.getString("rid");
        doid = json.getString("doid");

        manager.setState(rid, doid, state);
    }

    /**
     * 下载数字对象
     * @param rid 资源ID
     * @param doid 数字对象ID
     * @param filePath 存储文件路径
     * @return 数字对象实例
     */
    protected Object testDownObject(String rid, String doid, String filePath){
        DownloadClient client = DataClients.getDownloadClient();

        return client.download(rid, doid, filePath);
    }

    /**
     * 获取资源rid的目录pid下所有数字对象
     * @param rid 资源ID
     * @param pid 数字对象ID，父目录ID
     * @return
     */
    protected int testListDir(String rid, String pid, List<Object> rlist){
        ObjectManager manager = DataClients.getManager();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("rid", rid);
        map.put("pid", pid);

        return manager.queryObjects(map, 0, 20, rlist);
    }

    protected void parse(String... args){
        for(int i=0; i<args.length; i++){
            String line = args[i];
            int pos = line.indexOf("=");
            if(pos>0){
                String str = line.substring(0, pos).trim();
                line = line.substring(pos+1).trim();
                if(line.startsWith("\"")){
                    line = line.substring(1).trim();
                }
                if(line.endsWith("\"")){
                    line = line.substring(0, line.length()-1);
                }

                switch(str){
                    case "-u":
                    case "-user":
                        params.put("uname", line);
                        break;
                    case "-p":
                    case "-pass":
                        params.put("upass", line);
                        break;
                    case "-s":
                    case "-server":
                        params.put("server", line);
                        break;
                }
            }
        }
    }
}

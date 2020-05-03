package com.toipr.client;

import com.toipr.client.impl.HttpDownClient;
import com.toipr.client.impl.HttpObjectManager;
import com.toipr.client.impl.HttpServerFinder;
import com.toipr.client.impl.HttpUploadClient;

public class DataClients {
    protected static ServerFinder finder = null;
    protected static ObjectManager manager = null;
    protected static UploadClient upClient = null;
    protected static DownloadClient downClient = null;

    public static synchronized boolean createManager(String uname, String upass, Object... args){
        if(manager==null){
            HttpObjectManager myobj = new HttpObjectManager();
            if(!myobj.auth(uname, upass, args)){
                return false;
            }
            manager = myobj;
        }
        return true;
    }
    public static ObjectManager getManager(){
        return manager;
    }

    public synchronized static boolean createFinder(String server){
        if(finder==null){
            finder = new HttpServerFinder(server);
        }
        return true;
    }
    public static ServerFinder getFinder(){
        return finder;
    }

    public synchronized  static boolean createUploadClient(String uname, String upass, Object... args){
        if(upClient==null){
            HttpUploadClient client = new HttpUploadClient();
            if(!client.auth(uname, upass)){
                return false;
            }
            upClient = client;
        }
        return true;
    }
    public static UploadClient getUploadClient(){
        return upClient;
    }

    public synchronized  static boolean createDownloadClient(String uname, String upass, Object... args){
        if(downClient==null){
            HttpDownClient client = new HttpDownClient();
            if(!client.auth(uname, upass)){
                return false;
            }
            downClient = client;
        }
        return true;
    }
    public static DownloadClient getDownloadClient(){
        return downClient;
    }
}

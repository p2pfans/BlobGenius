package com.toipr.conf;

import java.util.HashMap;
import java.util.Map;

public class MySettings {
    private String hostid;
    public String getHostid(){
        return this.hostid;
    }
    public void setHostid(String hostid){
        this.hostid = hostid;
    }

    private int port;
    public int getPort(){
        return this.port;
    }
    public void setPort(int port){
        this.port = port;
    }

    protected Map<String, Object> mapSettings = new HashMap<String, Object>();
    public Object getProperty(String name){
        if(mapSettings.containsKey(name)){
            return mapSettings.get(name);
        }
        return null;
    }

    public boolean hasProperty(String name){
        return mapSettings.containsKey(name);
    }

    public void removeProperty(String name){
        synchronized (mapSettings){
            mapSettings.remove(name);
        }
    }

    public synchronized void setProperty(String name, Object value){
        synchronized (mapSettings) {
            mapSettings.put(name, value);
        }
    }
}

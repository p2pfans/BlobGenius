package com.toipr.service.server.impl;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataBlobRef;
import com.toipr.model.data.DataObject;
import com.toipr.service.server.DataServer;
import com.toipr.util.threads.ThreadPoolWorker;

import java.util.Date;

public class DataWorker implements Runnable {
    protected int direction;
    protected DataServer server;

    protected boolean isCopy = true;

    protected Object target;
    public DataWorker setTarget(Object target){
        this.target = target;
        return this;
    }

    protected String uuid;
    public DataWorker setUuid(String uuid){
        this.uuid = uuid;
        return this;
    }

    protected String oid;
    public DataWorker setOid(String oid){
        this.oid = oid;
        return this;
    }

    protected String action;
    public DataWorker setAction(String action){
        this.action = action;
        return this;
    }

    protected Date lastAccess;
    public DataWorker setLastAccess(Date lastAccess){
        this.lastAccess = lastAccess;
        return this;
    }

    protected int state;
    public DataWorker setState(int state){
        this.state = state;
        return this;
    }

    public DataWorker(Object target, int direction, DataServer server){
        this.target = target;
        this.server = server;
        this.direction = direction | DataServer.jobChain;
    }

    public void submit(){
        /**
         * 提交线程池等待执行
         */
        ThreadPoolWorker.submit(this);
    }

    @Override
    public void run(){
        if(target!=null) {
            if (target instanceof DataBlob) {
                server.addBlob((DataBlob) target, isCopy, direction);
            } else if (target instanceof DataBlobRef) {
                server.addBlobIds((DataBlobRef) target, direction);
            } else {
                server.addObject((DataObject) target, direction);
            }
        } else {
            switch(action){
                case "incBlobDown":
                    server.incBlobDown(uuid, lastAccess, direction);
                    break;
                case "incObjectDown":
                    server.incObjectDown(oid, uuid, lastAccess, direction);
                    break;
                case "incBlobRefs":
                    server.incBlobRefs(uuid, direction);
                    break;
                case "decBlobRefs":
                    server.decBlobRefs(uuid, direction);
                    break;

                case "removeBlob":
                    server.removeBlob(uuid, direction);
                    break;
                case "removeBlobIds":
                    server.removeBlobIds(uuid, direction);
                    break;
                case "removeObject":
                    server.removeObject(oid, uuid, direction);
                    break;
            }
        }
    }
}

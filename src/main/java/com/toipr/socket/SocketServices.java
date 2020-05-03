package com.toipr.socket;

import com.toipr.socket.server.FileBlobServer;
import org.springframework.context.ApplicationContext;

public class SocketServices {
    protected static BlobServer blobServer = null;
    public static synchronized  boolean createBlobServer(ApplicationContext context, Object... args){
        if(blobServer==null){
            FileBlobServer myobj = new FileBlobServer(context);
            if(!myobj.init(args)){
                return false;
            }
            blobServer = myobj;
        }
        return true;
    }

    public static BlobServer getBlobServer(){
        return blobServer;
    }
}

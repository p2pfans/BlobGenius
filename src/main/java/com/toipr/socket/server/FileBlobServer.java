package com.toipr.socket.server;

import com.toipr.conf.MySettings;
import com.toipr.model.data.DataBlob;
import com.toipr.model.node.DataNode;
import com.toipr.model.user.UserInfo;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.data.BlobStore;
import com.toipr.service.data.BlobStores;
import com.toipr.service.node.NodeService;
import com.toipr.service.node.NodeServices;
import com.toipr.service.user.UserService;
import com.toipr.service.user.UserServices;
import com.toipr.socket.BlobServer;
import com.toipr.socket.CmdConst;
import com.toipr.util.Utils;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class FileBlobServer implements BlobServer, Runnable {
    protected boolean isExit = false;
    protected List<BlobClient> lstClient;

    protected MySettings settings;
    protected ServerSocket server;
    protected ApplicationContext context;

    public FileBlobServer(ApplicationContext context){
        this.context = context;
    }

    /**
     * 初始化服务器
     * @param args 自定义参数
     * @return true=成功
     */
    public boolean init(Object... args){
        try {
            settings = (MySettings)context.getBean("mysettings");
            if(settings==null){
                return false;
            }

            NodeService service = NodeServices.getInstance();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("hid", settings.getHostid());
            params.put("dataType", "blobs");

            List<DataNode> lstNodes = new ArrayList<DataNode>();
            service.getNodeList(params, 0, 0, lstNodes);
            for(DataNode node : lstNodes){
                BlobStores.createBlobStore(node.getRid(), node.getRid(), true, node.getFilePath());
            }

            server = new ServerSocket();
            server.bind(new InetSocketAddress(settings.getPort()));
            lstClient = new LinkedList<BlobClient>();

            Thread tobj = new Thread(this);
            tobj.start();
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭服务器
     */
    public void close(){
        isExit = true;
        try {
            if (server != null) {
                server.close();
                server = null;
            }

            for(BlobClient client : lstClient){
                client.close();
            }
            lstClient.clear();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 添加存储的资源与本地路径
     * @param rid 资源ID
     * @param filePath 文件路径
     */
    public void addSource(String rid, String filePath){
        BlobStores.createBlobStore(rid, rid, true, filePath);
    }

    @Override
    public void run(){
        while(!isExit){
            try {
                Socket socket = server.accept();
                if(socket!=null){
                    BlobClient client = new BlobClient(socket);
                    client.init(this);
                }
            }catch(Exception ex){
                ;
            }
        }
    }

    public void onClientJoin(BlobClient client){

    }

    public void onClientLeave(BlobClient client){

    }

    protected class BlobClient implements Runnable {
        protected boolean isExit = false;

        protected Socket channel;
        protected FileBlobServer server;

        protected ObjectInputStream ois;
        protected ObjectOutputStream oos;

        public BlobClient(Socket channel){
            this.channel = channel;
        }

        public void close(){
            isExit = true;
            try {
                server = null;
                if (channel != null) {
                    channel.close();
                    channel = null;
                }
            }catch(Exception ex){
                ;
            }
        }

        public boolean init(FileBlobServer server){
            this.server = server;
            try{
                ois = new ObjectInputStream(channel.getInputStream());
                oos = new ObjectOutputStream(channel.getOutputStream());

                Thread tobj = new Thread(this);
                tobj.start();
                return true;
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        public void run(){
            try{
                if(!procHandshake()){
                    server.onClientLeave(this);
                    return;
                }

                while(!isExit){
                    int tag = ois.readInt();
                    int cmd = ois.readShort();
                    if (tag != CmdConst.MagicCookie || cmd != CmdConst.Cmd_DataBlob) {
                        server.onClientLeave(this);
                        return;
                    }

                    cmd = ois.readShort();
                    switch(cmd){
                        case CmdConst.DataBlob_GetData:
                            doGetBlobData();
                            break;
                        case CmdConst.DataBlob_Get:
                            doGetBlob();
                            break;
                        case CmdConst.DataBlob_Save:
                            doSaveBlob();
                            break;
                        case CmdConst.DataBlob_Exists:
                            doBlobExists();
                            break;
                        case CmdConst.DataBlob_Remove:
                            doBlobRemove();
                            break;
                        default:
                            server.onClientLeave(this);
                            return;
                    }
                }
            }catch(Exception ex){
                server.onClientLeave(this);
            }
        }

        protected void doGetBlobData(){
            try {
                String rid = getIdStr(ois);
                String bid = getIdStr(ois);
                if(Utils.isNullOrEmpty(rid) || Utils.isNullOrEmpty(bid)){
                    return;
                }

                BlobStore service = BlobStores.getBlobStore(rid);
                DataBlob blob = service.getBlob(bid);
                oos.writeInt(CmdConst.MagicCookie);
                oos.writeShort(CmdConst.Cmd_DataBlob);
                oos.writeShort(CmdConst.DataBlob_GetData_Reply);
                if(blob==null){
                    oos.writeInt(404);
                } else {
                    oos.writeInt(200);
                    oos.writeInt(blob.getSize());
                    oos.write(blob.getData());
                }
                oos.flush();
            }catch(Exception ex){
                server.onClientLeave(this);
            }
        }

        protected void doGetBlob(){
            try {
                String rid = getIdStr(ois);
                String bid = getIdStr(ois);
                if(Utils.isNullOrEmpty(rid) || Utils.isNullOrEmpty(bid)){
                    return;
                }

                BlobStore service = BlobStores.getBlobStore(rid);
                DataBlob blob = service.getBlob(bid);
                oos.writeInt(CmdConst.MagicCookie);
                oos.writeShort(CmdConst.Cmd_DataBlob);
                oos.writeShort(CmdConst.DataBlob_Get_Reply);
                if(blob==null){
                    oos.writeInt(404);
                } else {
                    oos.writeInt(200);
                    blob.writeObject(oos);
                }
                oos.flush();
            }catch(Exception ex){
                server.onClientLeave(this);
            }
        }

        protected void doSaveBlob(){
            try {
                String rid = getIdStr(ois);
                if(Utils.isNullOrEmpty(rid)){
                    onClientLeave(this);
                    return;
                }

                DataBlob blob = DataBlob.readObject(ois);
                if(blob==null){
                    onClientLeave(this);
                    return;
                }
                oos.writeInt(CmdConst.MagicCookie);
                oos.writeShort(CmdConst.Cmd_DataBlob);
                oos.writeShort(CmdConst.DataBlob_Save_Reply);

                BlobStore service = BlobStores.getBlobStore(rid);
                if(service.saveBlob(blob)){
                    oos.writeInt(200);
                } else {
                    oos.writeInt(500);
                }
                oos.flush();
            }catch(Exception ex){
                onClientLeave(this);
            }
        }

        protected void doBlobExists(){
            try {
                String rid = getIdStr(ois);
                String bid = getIdStr(ois);
                if(Utils.isNullOrEmpty(rid) || Utils.isNullOrEmpty(bid)){
                    return;
                }
                oos.writeInt(CmdConst.MagicCookie);
                oos.writeShort(CmdConst.Cmd_DataBlob);
                oos.writeShort(CmdConst.DataBlob_Exists_Reply);

                BlobStore service = BlobStores.getBlobStore(rid);
                if(service==null){
                    oos.writeInt(500);
                } else if(service.blobExists(bid)){
                    oos.writeInt(200);
                } else {
                    oos.writeInt(404);
                }
                oos.flush();
            }catch(Exception ex){
                server.onClientLeave(this);
            }
        }

        protected void doBlobRemove(){
            try {
                String rid = getIdStr(ois);
                String bid = getIdStr(ois);
                if(Utils.isNullOrEmpty(rid) || Utils.isNullOrEmpty(bid)){
                    return;
                }
                oos.writeInt(CmdConst.MagicCookie);
                oos.writeShort(CmdConst.Cmd_DataBlob);
                oos.writeShort(CmdConst.DataBlob_Remove_Reply);

                BlobStore service = BlobStores.getBlobStore(rid);
                if(service.removeBlob(bid)){
                    oos.writeInt(200);
                } else {
                    oos.writeInt(500);
                }
                oos.flush();
            }catch(Exception ex){
                server.onClientLeave(this);
            }
        }

        protected boolean procHandshake(){
            try{
                channel.setSoTimeout(5000);
                int tag = ois.readInt();
                int cmd = ois.readShort();
                if (tag != CmdConst.MagicCookie || cmd != CmdConst.Cmd_Handshake) {
                    return false;
                }
                cmd = ois.readShort();
                if(cmd!=CmdConst.Handshake_Token && cmd!=CmdConst.Handshake_Auth && cmd!=CmdConst.Handshake_Empty){
                    return false;
                }

                oos.writeInt(CmdConst.MagicCookie);
                oos.writeShort(CmdConst.Cmd_Handshake);
                if(cmd==CmdConst.Handshake_Empty){
                    oos.writeShort(CmdConst.Handshake_Empty);
                    oos.writeInt(200);
                } else if(cmd==CmdConst.Handshake_Token){
                    oos.writeShort(CmdConst.Handshake_Token_Replay);

                    int secret = ois.readInt();
                    String token = getSecretString(secret, ois);
                    CacheServer server = CacheServices.getServer("users", token, false, null);
                    if(server.exists(token)){
                        oos.writeInt(200);
                    } else {
                        oos.writeInt(401);
                    }
                } else {
                    int secret = ois.readInt();
                    String uname = getSecretString(secret, ois);
                    String upass = getSecretString(secret, ois);

                    oos.writeShort(CmdConst.Handshake_Auth_Reply);

                    UserService service = UserServices.getInstance();
                    UserInfo obj = service.checkUser(uname, upass);
                    if(obj==null){
                        oos.writeInt(401);
                    } else {
                        oos.writeInt(200);
                    }
                }
                oos.flush();

                channel.setSoTimeout(3*60*1000);
                return true;
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return false;
        }

        protected String getSecretString(int secret, ObjectInputStream ois){
            try{
                int idLen = ois.readShort();
                if(idLen<6 || idLen>64){
                    throw new Exception("id length is not right");
                }

                byte[] idArr = new byte[256];
                if(!Utils.readAll(idArr, idLen, ois)){
                    return null;
                }
                for(int i=0; i<idLen; i++){
                    idArr[i] = (byte)(((int)idArr[i])^secret);
                }
                return new String(idArr, 0, idLen, "utf-8");
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return null;
        }

        protected String getIdStr(ObjectInputStream ois){
            try{
                int idLen = ois.readShort();
                if(idLen<8 || idLen>64){
                    throw new Exception("id length is not right");
                }

                byte[] idArr = new byte[256];
                if(!Utils.readAll(idArr, idLen, ois)){
                    return null;
                }
                return new String(idArr, 0, idLen, "utf-8");
            } catch(Exception ex){
                ;
            }
            server.onClientLeave(this);
            return null;
        }
    }
}

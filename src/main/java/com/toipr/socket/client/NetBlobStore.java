package com.toipr.socket.client;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataResource;
import com.toipr.model.node.DataNode;
import com.toipr.service.data.BlobStore;
import com.toipr.service.resource.ResourceService;
import com.toipr.service.resource.ResourceServices;
import com.toipr.socket.CmdConst;
import com.toipr.util.Utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NetBlobStore implements BlobStore {
    protected DataNode config;
    protected DataResource resource;

    protected String rid;
    protected int port = 35168;
    protected String host;
    protected byte[] ridArr;

    protected Object lockObj = new Object();
    protected List<Socket> lstClient = new LinkedList<Socket>();

    protected Map<Socket, ObjectInputStream> mapInput = new HashMap<Socket, ObjectInputStream>();
    protected Map<Socket, ObjectOutputStream> mapOutput = new HashMap<Socket, ObjectOutputStream>();

    /**
     * 初始化文件存储
     * @param rid 资源ID
     * @param args 自定义参数
     * @return true=成功
     */
    public boolean init(String rid, Object... args){
        String text;
        DataNode node = null;
        if(args[0] instanceof DataNode) {
            node = (DataNode) args[0];
            text = node.getFileHost();
        } else {
            text = (String)args[0];
        }

        ResourceService service = ResourceServices.getInstance();
        this.resource = service.getResource(rid);
        if(this.resource==null){
            return false;
        }
        this.rid = rid;

        try {
            this.ridArr = this.rid.getBytes("utf-8");
        } catch(Exception ex){
            return false;
        }

        int pos = text.indexOf(":");
        if(pos<0){
            host = text;
        } else {
            host = text.substring(0, pos).trim();
            text = text.substring(pos+1).trim();
            port = Integer.parseInt(text);
            if(port<0 || port>65535){
                port = 35168;
            }
        }
        this.config = node;
        return true;
    }

    /**
     * 读取数据块数据
     * @param doid 数据块ID
     * @return 数据数组
     */
    public byte[] getData(String doid){
        Socket client = getClient();
        if(client==null){
            return null;
        }

        try{
            ObjectOutputStream oos = getOutputStream(client);
            oos.writeInt(CmdConst.MagicCookie);
            oos.writeShort(CmdConst.Cmd_DataBlob);
            oos.writeShort(CmdConst.DataBlob_GetData);

            byte[] idArr = doid.getBytes("utf-8");
            oos.writeShort(ridArr.length);
            oos.write(ridArr);
            oos.writeShort(idArr.length);
            oos.write(idArr);
            oos.flush();

            ObjectInputStream ois = getInputStream(client);
            int tag = ois.readInt();
            int type = ois.readShort();
            int cmd = ois.readShort();
            if (tag != CmdConst.MagicCookie || type != CmdConst.Cmd_DataBlob || cmd != CmdConst.DataBlob_GetData_Reply) {
                throw new Exception("bad command response");
            }

            byte[] data = null;
            int code = ois.readInt();
            if (code == 200) {
                int size = ois.readInt();
                if (size <= 0 || size > 4 * 1024 * 1024) {
                    throw new Exception("data blob size not right");
                }
                data = new byte[size];
                Utils.readAll(data, size, ois);
            }
            backClient(client, false);
            return data;
        }catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return null;
    }

    /**
     * 读取数据块
     * @param doid 数据块ID
     * @return DataBlob实例
     */
    public DataBlob getBlob(String doid){
        Socket client = getClient();
        if(client==null){
            return null;
        }

        try{
            ObjectOutputStream oos = getOutputStream(client);
            oos.writeInt(CmdConst.MagicCookie);
            oos.writeShort(CmdConst.Cmd_DataBlob);
            oos.writeShort(CmdConst.DataBlob_Get);

            byte[] idArr = doid.getBytes("utf-8");
            oos.writeShort(ridArr.length);
            oos.write(ridArr);
            oos.writeShort(idArr.length);
            oos.write(idArr);
            oos.flush();

            ObjectInputStream ois = getInputStream(client);
            int tag = ois.readInt();
            int type = ois.readShort();
            int cmd = ois.readShort();
            if (tag != CmdConst.MagicCookie || type != CmdConst.Cmd_DataBlob || cmd != CmdConst.DataBlob_Get_Reply) {
                throw new Exception("bad command response");
            }

            DataBlob blob = null;
            int code = ois.readInt();
            if (code == 200) {
                blob = DataBlob.readObject(ois);
                if (blob == null) {
                    throw new Exception("DataBlob content error");
                }
            }
            backClient(client, false);
            return blob;
        }catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return null;
    }

    /**
     * 存储数据块对象
     * @param blob
     * @return true=成功
     */
    public boolean saveBlob(DataBlob blob){
        Socket client = getClient();
        if(client==null){
            return false;
        }

        try{
            ObjectOutputStream oos = getOutputStream(client);
            oos.writeInt(CmdConst.MagicCookie);
            oos.writeShort(CmdConst.Cmd_DataBlob);
            oos.writeShort(CmdConst.DataBlob_Save);
            oos.writeShort(ridArr.length);
            oos.write(ridArr);
            blob.writeObject(oos);
            oos.flush();

            ObjectInputStream ois = getInputStream(client);
            int tag = ois.readInt();
            int type = ois.readShort();
            int cmd = ois.readShort();
            if (tag != CmdConst.MagicCookie || type != CmdConst.Cmd_DataBlob || cmd != CmdConst.DataBlob_Save_Reply) {
                throw new Exception("bad command response");
            }

            int code = ois.readInt();
            backClient(client, false);
            return code == 200;
        }catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return false;
    }

    /**
     * 数据块是否存在
     * @param doid 数据块ID
     * @return true=存在
     */
    public boolean blobExists(String doid){
        Socket client = getClient();
        if(client==null){
            return false;
        }

        try{
            ObjectOutputStream oos = getOutputStream(client);
            oos.writeInt(CmdConst.MagicCookie);
            oos.writeShort(CmdConst.Cmd_DataBlob);
            oos.writeShort(CmdConst.DataBlob_Exists);

            byte[] idArr = doid.getBytes("utf-8");
            oos.writeShort(ridArr.length);
            oos.write(ridArr);
            oos.writeShort(idArr.length);
            oos.write(idArr);
            oos.flush();

            ObjectInputStream ois = getInputStream(client);
            int tag = ois.readInt();
            int type = ois.readShort();
            int cmd = ois.readShort();
            if (tag != CmdConst.MagicCookie || type != CmdConst.Cmd_DataBlob || cmd != CmdConst.DataBlob_Exists_Reply) {
                throw new Exception("bad command response");
            }

            int code = ois.readInt();
            backClient(client, false);
            return code == 200;
        }catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return false;
    }

    /**
     * 删除数据块
     * @param doid 数据块ID
     * @return true=成功
     */
    public boolean removeBlob(String doid){
        Socket client = getClient();
        if(client==null){
            return false;
        }

        try{
            ObjectOutputStream oos = getOutputStream(client);
            oos.writeInt(CmdConst.MagicCookie);
            oos.writeShort(CmdConst.Cmd_DataBlob);
            oos.writeShort(CmdConst.DataBlob_Remove);

            byte[] idArr = doid.getBytes("utf-8");
            oos.writeShort(ridArr.length);
            oos.write(ridArr);
            oos.writeShort(idArr.length);
            oos.write(idArr);
            oos.flush();

            ObjectInputStream ois = getInputStream(client);
            int tag = ois.readInt();
            int type = ois.readShort();
            int cmd = ois.readShort();
            if (tag != CmdConst.MagicCookie || type != CmdConst.Cmd_DataBlob || cmd != CmdConst.DataBlob_Remove_Reply) {
                throw new Exception("bad command response");
            }

            int code = ois.readInt();
            backClient(client, false);
            return code == 200;
        }catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return false;
    }

    protected Socket getClient(){
        Socket client = null;
        synchronized (lockObj){
            while(lstClient.size()>0){
                client = lstClient.remove(0);
                if(!client.isClosed()){
                    return client;
                }
            }
        }

        try {
            client = new Socket(host, port);
            if(!doHandshake(client)){
                client.close();
                client = null;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return client;
    }

    protected boolean doHandshake(Socket client){
        try{
            ObjectOutputStream oos = getOutputStream(client);
            oos.writeInt(CmdConst.MagicCookie);
            oos.writeShort(CmdConst.Cmd_Handshake);
            oos.writeShort(CmdConst.Handshake_Empty);
            oos.flush();

            ObjectInputStream ois = getInputStream(client);
            int tag = ois.readInt();
            int type = ois.readShort();
            int cmd = ois.readShort();
            if (tag != CmdConst.MagicCookie && type != CmdConst.Cmd_Handshake && cmd != CmdConst.Handshake_Empty) {
                return false;
            }

            int code = ois.readInt();
            return (code==200);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    protected ObjectInputStream getInputStream(Socket client){
        ObjectInputStream stream = null;
        if(mapInput.containsKey(client)){
            return mapInput.get(client);
        }

        try {
            stream = new ObjectInputStream(client.getInputStream());
            mapInput.put(client, stream);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return stream;
    }

    protected ObjectOutputStream getOutputStream(Socket client){
        ObjectOutputStream stream = null;
        if(mapOutput.containsKey(client)){
            return mapOutput.get(client);
        }

        try {
            stream = new ObjectOutputStream(client.getOutputStream());
            mapOutput.put(client, stream);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return stream;
    }

    protected void backClient(Socket client, boolean isClose){
        if(isClose){
            try {
                mapInput.remove(client);
                mapOutput.remove(client);
                client.close();
            } catch(Exception ex){
                ex.printStackTrace();
            }
            return;
        }

        synchronized (lockObj){
            lstClient.add(client);
        }
    }

    protected byte[] fromSecretString(int secret, String text){
        try{
            byte[] idArr = text.getBytes("utf-8");
            for(int i=0; i<idArr.length; i++){
                idArr[i] = (byte)(((int)idArr[i])^secret);
            }
            return idArr;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}

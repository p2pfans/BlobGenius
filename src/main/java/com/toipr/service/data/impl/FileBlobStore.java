package com.toipr.service.data.impl;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataResource;
import com.toipr.model.node.DataNode;
import com.toipr.service.data.BlobStore;
import com.toipr.service.resource.ResourceService;
import com.toipr.service.resource.ResourceServices;
import com.toipr.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBlobStore implements BlobStore {
    protected DataNode config;
    protected DataResource resource;

    protected String rootPath = "";
    public FileBlobStore(){
    }

    /**
     * 初始化文件存储
     * @param rid 资源ID
     * @param args 自定义参数
     * @return true=成功
     */
    public boolean init(String rid, Object... args){
        if(args==null && args.length<1){
            return false;
        }
        rootPath = (String)args[0];

        ResourceService service = ResourceServices.getInstance();
        this.resource = service.getResource(rid);
        if(this.resource==null){
            return false;
        }

        File path = Paths.get(rootPath, resource.getOid(), rid).toFile();
        if(!path.exists()){
            if(!path.mkdirs()){
                return false;
            }
        }
        rootPath = path.getAbsolutePath();
        return true;
    }

    /**
     * 读取数据块数据
     * @param doid 数据块ID
     * @return 数据数组
     */
    public byte[] getData(String doid){
        DataBlob blob = getBlob(doid);
        if(blob==null){
            return null;
        }
        return blob.getData();
    }

    /**
     * 读取数据块
     * @param doid 数据块ID
     * @return DataBlob实例
     */
    public DataBlob getBlob(String doid){
        File fp = getPath(doid);
        if(fp==null || !fp.exists() || !fp.isFile()){
            return null;
        }

        try {
            FileInputStream fis = new FileInputStream(fp);
            DataBlob blob = DataBlob.readObject(fis);
            fis.close();
            return blob;
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 存储数据块对象
     * @param blob
     * @return true=成功
     */
    public boolean saveBlob(DataBlob blob){
        File fp = getPath(blob.getBoid());
        if(fp==null){
            return false;
        }
        if(fp.exists()){
            return true;
        }

        try{
            FileOutputStream fos = new FileOutputStream(fp);
            blob.writeObject(fos);
            fos.close();
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 数据块是否存在
     * @param doid 数据块ID
     * @return true=存在
     */
    public boolean blobExists(String doid){
        File blob = getPath(doid);
        if(blob==null){
            return false;
        }
        return blob.exists();
    }

    /**
     * 删除数据块
     * @param doid 数据块ID
     * @return true=成功
     */
    public boolean removeBlob(String doid){
        File blob = getPath(doid);
        if(blob==null){
            return false;
        }
        if(blob.exists()){
            return blob.delete();
        }
        return true;
    }

    public File getPath(String doid){
        Path tPath = Paths.get(rootPath, doid.substring(3, 5), doid.substring(7, 9), doid.substring(11, 13),
                doid.substring(13, 15), doid.substring(17, 19), doid.substring(21, 23), doid + ".blob");

        File dir = tPath.getParent().toFile();
        if(!dir.exists()){
            if(!dir.mkdirs()){
                return null;
            }
        }
        return tPath.toFile();
    }
}

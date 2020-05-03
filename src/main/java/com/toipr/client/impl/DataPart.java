package com.toipr.client.impl;

import com.toipr.util.HashHelper;
import com.toipr.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class DataPart {
    public DataPart(File file, Object target){
        this.file = file;
        this.target = target;
    }

    /**
     * 文件对象
     */
    private File file;
    public File getFile(){
        return this.file;
    }
    public void setFile(File file){
        this.file = file;
    }

    /**
     * 数字对象
     */
    private Object target;
    public Object getTarget(){
        return this.target;
    }
    public void setTarget(Object target){
        this.target = target;
    }

    /**
     * 数据块大小
     */
    private int size;
    public int getSize(){
        return this.size;
    }
    public void setSize(int size){
        this.size = size;
    }

    /**
     * 数据块序号
     */
    private int index;
    public int getIndex(){
        return this.index;
    }
    public void setIndex(int index){
        this.index =index;
    }

    private long offset = 0;
    public long getOffset(){
        return this.offset;
    }
    public void setOffset(long offset){
        this.offset = offset;
    }

    /**
     * 总块数
     */
    private int total;
    public int getTotal(){
        return this.total;
    }
    public void setTotal(int total){
        this.total = total;
    }

    /**
     * 数据块哈希
     */
    private String hash;
    public String getHash(){
        return this.hash;
    }
    public void setHash(String hash){
        this.hash=hash;
    }

    /**
     * 数据数组
     */
    private byte[] data;
    public byte[] getData(){
        if(data==null && file!=null){
            try{
                byte[] buff = new byte[size];
                FileInputStream ins = new FileInputStream(file);
                if(offset>0) {
                    ins.skip(offset);
                }
                Utils.readAll(buff, buff.length, ins);
                ins.close();

                hash = HashHelper.computeHash(buff, "SHA-256");
                data = buff;
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        return data;
    }
    public void setData(byte[] data){
        if(data!=null){
            try {
                this.hash = HashHelper.computeHash(data, "SHA-256");
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        this.data = data;
        this.size = data.length;
    }

    /**
     * 判断服务器内容是否与本地文件一致
     * @param size 数据块大小
     * @param hash 数据块校验码
     * @return true=存在 false=不存在
     */
    public boolean blobExists(int size, String hash){
        if(this.size != size){
            return false;
        }
        if(Utils.isNullOrEmpty(this.hash)) {
            getData();
        }
        if(hash.compareTo(this.hash)==0){
            return true;
        }
        return false;
    }

    /**
     * 从文件生成数据块
     * @param path 文件路径
     * @param off 数据偏移
     * @param len 数据长度
     * @return true=成功
     */
    public boolean readData(String path, int off, int len){
        try{
            try(FileInputStream ins = new FileInputStream(path)){
                return readData(ins, off, len);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 从数据流读取数据块
     * @param ins 数据流
     * @param off 数据偏移
     * @param len 数据长度
     * @return true=成功
     */
    public boolean readData(InputStream ins, int off, int len){
        try {
            byte[] buff = new byte[len];
            if(off>0) {
                ins.skip(off);
            }
            if(!Utils.readAll(buff, buff.length, ins)){
                return false;
            }
            data = buff;
            size = len;
            offset = off;

            hash = HashHelper.computeHash(data, "SHA-256");
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
}

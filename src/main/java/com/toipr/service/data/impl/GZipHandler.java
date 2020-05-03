package com.toipr.service.data.impl;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataConst;
import com.toipr.service.data.DataHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipHandler extends BaseDataHandler {
    public GZipHandler(boolean isInput){
        super(isInput);

        this.flags = DataConst.DataFlags_Compress;
    }

    @Override
    protected byte[] doDataProcess(InputStream ins, int flags){
        try {
            if (isInput) {
                return compress(ins);
            }
            return decompress(ins);
        }catch(Exception ex){
            ;
        }
        return null;
    }

    protected byte[] compress(InputStream ins) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[16384];
        GZIPOutputStream gzip = new GZIPOutputStream(baos);
        int len = ins.read(buffer);
        while(len>0){
            gzip.write(buffer, 0, len);
            len = ins.read(buffer);
        }
        gzip.flush();
        gzip.close();

        return baos.toByteArray();
    }

    protected byte[] decompress(InputStream ins) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[16384];
        GZIPInputStream gzip = new GZIPInputStream(ins);
        int len = gzip.read(buffer);
        while(len>0){
            baos.write(buffer, 0, len);
            len = gzip.read(buffer);
        }
        gzip.close();

        return baos.toByteArray();
    }

    @Override
    public byte[] doDataProcess(byte[] data, int off, int len, int flags){
        try{
            if(isInput){
                return compress(data, off, len);
            }
            return decompress(data, off, len);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    protected byte[] compress(byte[] data, int off, int len) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GZIPOutputStream gzip = new GZIPOutputStream(baos);
        gzip.write(data, off, len);
        gzip.flush();
        gzip.close();

        return baos.toByteArray();
    }

    protected byte[] decompress(byte[] data, int off, int len) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[16384];
        ByteArrayInputStream bais = new ByteArrayInputStream(data, off, len);
        GZIPInputStream gzip = new GZIPInputStream(bais);
        len = gzip.read(buffer);
        while(len>0){
            baos.write(buffer, 0, len);
            len = gzip.read(buffer);
        }
        gzip.close();
        bais.close();

        return baos.toByteArray();
    }
}

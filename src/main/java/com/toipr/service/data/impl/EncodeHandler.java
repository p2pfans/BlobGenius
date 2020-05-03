package com.toipr.service.data.impl;


import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataConst;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EncodeHandler extends BaseDataHandler {
    protected static byte[] codec = {0x4c, 0x48, 0x50, 0x54, 0x4f, 0x49, 0x50, 0x52};

    public EncodeHandler(boolean isInput){
        super(isInput);

        this.flags = DataConst.DataFlags_Encode;
    }

    @Override
    protected byte[] doDataProcess(InputStream ins, int flags){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] data = new byte[16384];

            int pos = 0;
            int len = ins.read(data);
            while(len>0){
                for(int i=0; i<len; i++, pos++){
                    data[i] ^= codec[pos%codec.length];
                }
                baos.write(data, 0, len);
                len = ins.read(data);
            }
            return baos.toByteArray();
        }catch(Exception ex){
            ;
        }
        return null;
    }

    @Override
    public byte[] doDataProcess(byte[] data, int off, int len, int flags){
        try {
            int pos = 0;
            byte[] buffer = new byte[len];
            for(int i=0; i<len; i++, pos++){
                buffer[i] = (byte)(data[i+off] ^ codec[pos%codec.length]);
            }
            return buffer;
        }catch(Exception ex){
            ;
        }
        return null;
    }
}

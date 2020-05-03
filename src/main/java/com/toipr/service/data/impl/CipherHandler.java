package com.toipr.service.data.impl;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataConst;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.conf.SettingsService;
import com.toipr.service.conf.SettingsServices;
import com.toipr.util.Utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class CipherHandler extends BaseDataHandler {
    protected SecretKey secretKey;
    public CipherHandler(boolean isInput){
        super(isInput);

        this.flags = DataConst.DataFlags_Cipher;
        this.init();
    }

    protected void init(){
        try {
            SettingsService service = SettingsServices.getInstance();
            Object objKey = service.getConf("secretKey");
            if (objKey != null) {
                byte[] data = Utils.hexStringToByteArray((String) objKey);
                secretKey = new SecretKeySpec(data, "AES");
            } else {
                String keyStr;
                CacheServer server = CacheServices.getServer("config", "secretKey", false, null);
                if(server.exists("secretKey")){
                    keyStr = server.getString("secretKey", null);
                    byte[] data = Utils.hexStringToByteArray(keyStr);
                    secretKey = new SecretKeySpec(data, "AES");
                } else {
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(128);

                    secretKey = keyGen.generateKey();
                    byte[] data = secretKey.getEncoded();
                    keyStr = Utils.byteArrayToHexString(data, 0, data.length);
                    if(server.addCache("secretKey", 0, CacheServer.ifNotExists, keyStr)){
                        service.addConf("secretKey", "string", keyStr);
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected byte[] doDataProcess(InputStream ins, int flags){
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            if(isInput){
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] data = new byte[16384];
            do{
                int len = ins.read(data);
                if(len==-1){
                    break;
                }
                data = cipher.update(data, 0, len);
                baos.write(data);
            }while(true);
            return baos.toByteArray();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected byte[] doDataProcess(byte[] data, int off, int len, int flags){
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            if(isInput){
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
            return cipher.doFinal(data, off, len);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}

package com.toipr.service.data;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataConst;
import com.toipr.service.data.impl.CipherHandler;
import com.toipr.service.data.impl.EncodeHandler;
import com.toipr.service.data.impl.GZipHandler;

public class DataHandlers {
    /**
     * 输入数据处理链条，原始数据->存储系统
     */
    protected static DataHandler chainInput = null;
    /**
     * 输出数据处理链条，存储系统->原始数据
     */
    protected static DataHandler chainOutput = null;

    /**
     * 创建数据处理器，根据flags一次可以创建多个处理器
     * @param isInput true=输入处理器
     * @param index 处理器序号，决定数据传递先后
     * @param flags 处理器标志位
     * @return true=成功
     */
    public static synchronized boolean createHandler(boolean isInput, int index, int flags){
        if((flags & DataConst.DataFlags_Encode)!=0){
            if(!initHandler(isInput, index, DataConst.DataFlags_Encode, isInput ? chainInput : chainOutput)){
                return false;
            }
        }
        if((flags & DataConst.DataFlags_Cipher)!=0){
            if(!initHandler(isInput, index, DataConst.DataFlags_Cipher, isInput ? chainInput : chainOutput)){
                return false;
            }
        }
        if((flags & DataConst.DataFlags_Compress)!=0){
            if(!initHandler(isInput, index, DataConst.DataFlags_Compress, isInput ? chainInput : chainOutput)){
                return false;
            }
        }
        return true;
    }

    public static DataHandler getChainHandler(boolean isInput){
        if(isInput){
            return chainInput;
        }
        return chainOutput;
    }

    protected static boolean initHandler(boolean isInput, int index, int flags, DataHandler chain){
        if(chain!=null && chain.exists(flags)){
            return true;
        }

        DataHandler handler = null;
        if(flags==DataConst.DataFlags_Compress){
            handler = new GZipHandler(isInput);
        } else if(flags==DataConst.DataFlags_Encode){
            handler = new EncodeHandler(isInput);
        } else if(flags==DataConst.DataFlags_Cipher){
            handler = new CipherHandler(isInput);
        }

        if(handler!=null){
            if(index<0){
                index = flags;
            }
            handler.setIndex(index);
            if(chain==null){
                chain = handler;
            } else {
                chain = chain.addHandler(handler);
            }
            if(isInput){
                chainInput = chain;
            } else {
                chainOutput = chain;
            }
        }
        return (handler!=null);
    }
}

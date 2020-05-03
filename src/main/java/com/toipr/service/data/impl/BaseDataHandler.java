package com.toipr.service.data.impl;

import com.toipr.service.data.DataHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseDataHandler implements DataHandler {
    protected int index = 0;
    protected int flags = 0;
    protected DataHandler handler;

    protected boolean isInput;
    protected BaseDataHandler(boolean isInput){
        this.isInput = isInput;
    }

    /**
     * 获取当前数据处理器
     * @return 数据处理器对象
     */
    public DataHandler getHandler(){
        return handler;
    }

    /**
     * 设置数据处理器，责任链模式
     * 实现类先调用自己的处理逻辑，再调用责任链后一个处理逻辑, 按index顺序从小到大排序
     * @param handler 数据处理器
     * @return 返回排在第一顺位的数据处理器
     */
    public DataHandler addHandler(DataHandler handler) {
        if(this.handler==null && handler==null){
            return this;
        }

        /**
         * 比较当前处理器与新处理器优先顺序
         * 输入流：正序，按处理器序号从小到大组成责任链
         * 输出流：逆序，按处理器序号从大到小组成责任链
         */
        int p1 = this.index;
        int p2 = handler.getIndex();
        boolean isMeFirst = false;
        if(isInput){
            if(p1 <= p2) {
                isMeFirst = true;
            }
        } else {
            if(p1 > p2){
                isMeFirst = true;
            }
        }

        if (isMeFirst) {
            if(this.handler!=null){
                this.handler = this.handler.addHandler(handler);
            } else {
                this.handler = handler;
            }
            return this;
        }
        handler.addHandler(this);
        return handler;
    }

    /**
     * 数据处理责任标志是否存在
     * @param flags 处理标志
     * @return true=已存在
     */
    public boolean exists(int flags){
        if((flags&this.flags)!=0){
            return true;
        }
        if(handler!=null){
            return handler.exists(flags);
        }
        return false;
    }

    /**
     * 设置数据处理责任标志
     * @param flags 处理标志
     */
    public void setFlags(int flags){
        this.flags = flags;
    }

    /**
     * 获取处理顺序
     * @return
     */
    public int getIndex(){
        return this.index;
    }

    /**
     * 设置处理顺序
     * @param index
     */
    public void setIndex(int index){
        this.index = index;
    }

    /**
     * 处理输入流读取的数据
     * @param ins 数据输入流
     * @param flags 处理标志
     * @return 处理后的数据
     */
    public byte[] process(InputStream ins, int flags){
        try {
            byte[] data = null;
            /**
             * 与自身职责相符合的处理，否则直接向后传递
             */
            if((this.flags&flags)!=0) {
                data = doDataProcess(ins, flags);
            }

            if(handler!=null){
                if(data!=null){
                    ins = new ByteArrayInputStream(data);
                }
                data = handler.process(ins, flags);
            }
            return data;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 处理数组从off开始、len字节长的数据
     * @param data 数据数组
     * @param off 开始位置
     * @param len 数据长度
     * @param flags 处理标志
     * @return 处理后的数据
     */
    public byte[] process(byte[] data, int off, int len, int flags){
        /**
         * 自己的职责先处理
         */
        if((this.flags & flags)!=0){
            data = doDataProcess(data, off, len, flags);
            if(data!=null) {
                off = 0;
                len = data.length;
            }
        }

        /**
         * 向后传递，后续环节继续处理数据
         */
        if(handler!=null && data!=null){
            data = handler.process(data, off, len, flags);
        }
        return data;
    }

    protected byte[] doDataProcess(InputStream ins, int flags){
        return null;
    }

    protected byte[] doDataProcess(byte[] data, int off, int len, int flags){
        return null;
    }
}

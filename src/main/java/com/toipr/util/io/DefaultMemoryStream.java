package com.toipr.util.io;

import java.io.IOException;
import java.io.InputStream;

public class DefaultMemoryStream extends InputStream {
    protected int start;
    protected int endpos;
    protected byte[] buffer;

    protected int pointer = 0;

    public DefaultMemoryStream(byte[] buffer){
        this(buffer, 0, buffer.length);
    }

    public DefaultMemoryStream(byte[] buffer, int start, int length){
        this.buffer = buffer;
        this.start = start;
        this.pointer = start;
        this.endpos = start + length;
    }

    @Override
    public int read() throws IOException {
        if(pointer>=endpos){
            return -1;
        }
        return buffer[pointer++];
    }

    @Override
    public synchronized void reset() throws IOException {
        pointer = 0;
    }

    @Override
    public synchronized int available() throws IOException {
        return (endpos - start);
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long temp = pointer + n;
        if(temp<start || temp>endpos){
            throw new IOException("skip over data buffer range");
        }
        pointer = (int)temp;
        return n;
    }
}

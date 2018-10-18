package com.hlzx.wenutil.http.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by alan on 2016/3/12.
 */
public class CounterOutputStream extends OutputStream{

    private final AtomicLong length = new AtomicLong(0L);

    public CounterOutputStream() {
    }

    public void write(long count) {
        if (count > 0)
            length.addAndGet(count);
    }

    public long get() {
        return length.get();
    }

    @Override
    public void write(int oneByte) throws IOException {
        length.addAndGet(oneByte);
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        length.addAndGet(buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        length.addAndGet(count);
    }


}

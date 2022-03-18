package io.flutter.plugins.webviewflutter;

import java.io.IOException;
import java.io.InputStream;

public class ResourceInputStream extends InputStream {
    private InputStream inputStream;
    private final Object mLock = new Object();

    public ResourceInputStream() {
        super();
    }

    public void attachRealInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        synchronized (mLock) {
            mLock.notifyAll();
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        checkInnerInputStream();
        return inputStream.read();
    }


    @Override
    public int read(byte[] b) throws IOException {
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        checkInnerInputStream();
        return inputStream.skip(n);
        //  return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        checkInnerInputStream();
        return inputStream.available();
        //  return super.available();
    }

    @Override
    public void close() throws IOException {
        checkInnerInputStream();
        inputStream.close();
        super.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        checkInnerInputStream();
        inputStream.mark(readlimit);
        super.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        checkInnerInputStream();
        inputStream.reset();
        super.reset();
    }

    @Override
    public boolean markSupported() {
        return super.markSupported();
    }


    public void checkInnerInputStream() {
        while (inputStream == null) {
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

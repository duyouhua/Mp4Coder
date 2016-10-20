package me.zuichu.mp4coder.muxer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static me.zuichu.mp4coder.tools.CastUtils.l2i;


public class FileRandomAccessSourceImpl implements RandomAccessSource {
    private RandomAccessFile raf;

    public FileRandomAccessSourceImpl(RandomAccessFile raf) {
        this.raf = raf;
    }

    public ByteBuffer get(long offset, long size) throws IOException {
        byte[] b = new byte[l2i(size)];
        raf.seek(offset);
        raf.read(b);
        return ByteBuffer.wrap(b);
    }


    public void close() throws IOException {
        raf.close();
    }
}

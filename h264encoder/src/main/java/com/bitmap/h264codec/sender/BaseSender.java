package com.bitmap.h264codec.sender;

/**
 * @author obo
 * @date 2018/2/5
 */

public abstract class BaseSender {
    public abstract void send(byte[] data, int length);
}

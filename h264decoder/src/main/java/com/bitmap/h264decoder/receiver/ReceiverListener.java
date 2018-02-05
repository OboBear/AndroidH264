package com.bitmap.h264decoder.receiver;

/**
 * @author obo
 * @date 2018/2/5
 */

public interface ReceiverListener {
    void onResponse(byte[] data, int length);
}
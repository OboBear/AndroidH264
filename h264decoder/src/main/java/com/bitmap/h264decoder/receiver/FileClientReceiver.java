package com.bitmap.h264decoder.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author obo
 * @date 2018/2/5
 */

public class FileClientReceiver extends BaseReceiver{
    private ReceiverListener mReceiverListener;
    private InputStream inputStream;
    private static final int SIZE = 4096;
    public FileClientReceiver(String fileName) {
        File file = new File(fileName);
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void receiver(ReceiverListener receiverListener) {
        mReceiverListener = receiverListener;
        if (inputStream == null) {
            return;
        }
        byte data[] = new byte[SIZE];
        try {
            int readLength = 0;
            while ((readLength = inputStream.read(data)) != -1) {
                byte[] readDataCopy = new byte[readLength];
                System.arraycopy(data, 0, readDataCopy, 0, readLength);
                mReceiverListener.onResponse(readDataCopy, readLength);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

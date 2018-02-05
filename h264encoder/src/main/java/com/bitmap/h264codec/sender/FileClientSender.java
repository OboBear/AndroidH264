package com.bitmap.h264codec.sender;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author obo
 * @date 2018/2/3
 */

public class FileClientSender extends BaseSender {
    private static final String DESTINATION_PATH =  Environment.getExternalStorageDirectory() + "/aaaa";
    private static final String DESTINATION_FILE_NAME =  "h264.h264";
    private File destinationFile = new File(DESTINATION_PATH + "/" + DESTINATION_FILE_NAME );
    private OutputStream outputStream;
    public FileClientSender() {
        File director = new File(DESTINATION_PATH);
        if (!director.exists()) {
            director.mkdirs();
        }
        try {
            outputStream = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data, int length) {
        try {
            outputStream.write(data, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

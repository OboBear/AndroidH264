package com.bitmap.h264decoder.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.bitmap.h264decoder.util.KMPUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author obo
 * @date 2018/2/5
 */

public class AvcDecoder {
    private static final String TAG = "AvcDecoder";
    private MediaCodec mediaCodec;
    private int mWidth;
    private int mHeight;
    byte[] mInfo = null;
    long timeoutUs = 10000;
    byte[] marker0 = new byte[]{0, 0, 0, 1};
    private byte[] yuv420 = null;

    public interface AvcDecoderListener {
        byte[] getEncodedData();
    }

    public AvcDecoder(int width, int height, int framerate, Surface surface) {
        mWidth = width;
        mHeight = height;
        yuv420 = new byte[width*height*3/2];

        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_ROTATION, 90);
        mediaCodec.configure(mediaFormat, surface, null, 0);
        mediaCodec.start();
    }
    ByteBuffer[] inputBuffers;
    AvcDecoderListener mDecoderListener;
    public void decode(AvcDecoderListener decoderListener) {
        mDecoderListener = decoderListener;
        inputBuffers = mediaCodec.getInputBuffers();
        while (true) {
            byte[] h264Frame = takeAvailableFrame();
            offerDecode(h264Frame, h264Frame.length);
        }
    }

    byte[] currentFrame = new byte[524288];
    int currentFrameSize = 0;
    byte[] inputByte;
    int inputNextStart = 0;
    private byte[] takeAvailableFrame() {
        int nextIndex = -1;
        while (true) {
            if (inputByte == null || inputNextStart == -1) {
                inputByte = mDecoderListener.getEncodedData();
                inputNextStart = 0;
                // 当前帧还没完结，需要探测两字节数组中间分割处否符合条件
                if (currentFrameSize > 0) {
                    System.arraycopy(inputByte, inputNextStart, currentFrame, currentFrameSize, 4);
                    int start = 1;
                    if (currentFrameSize > 3) {
                        start = currentFrameSize - 3;
                    }
                    nextIndex = KMPUtil.KMPMatch(marker0, currentFrame, start, currentFrameSize + 4);
                    if (nextIndex != -1) {
                        byte[] resultFrame = new byte[nextIndex];
                        System.arraycopy(currentFrame, 0, resultFrame, 0, nextIndex);
                        System.arraycopy(currentFrame, nextIndex, currentFrame, 0, 4);
                        currentFrameSize = currentFrameSize - nextIndex;
                        return resultFrame;
                    }
                }
            }
            //匹配0001
            nextIndex = KMPUtil.KMPMatch(marker0, inputByte, inputNextStart + 2, inputByte.length);

            //没有匹配到0001
            if (nextIndex == -1) {
                //全部字节都放到当前帧里面
                System.arraycopy(inputByte, inputNextStart, currentFrame, currentFrameSize, inputByte.length - inputNextStart);
                currentFrameSize += inputByte.length - inputNextStart;
                inputNextStart = -1;
            } else {
                //匹配到了0001
                byte[] resultFrame = new byte[currentFrameSize + nextIndex - inputNextStart];
                if (currentFrameSize > 0) {
                    System.arraycopy(currentFrame, 0, resultFrame, 0, currentFrameSize);
                }
                System.arraycopy(inputByte, inputNextStart, resultFrame, currentFrameSize, nextIndex - inputNextStart);
                currentFrameSize = 0;
                inputNextStart = nextIndex;
                return resultFrame;
            }
        }
    }

    private boolean offerDecode(byte[] h264, int length) {
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(timeoutUs);
        Log.i(TAG, "inputBufferIndex = " + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(h264, 0, length);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, 0
                    , 0);
        } else {
            return false;
        }
        // Get output buffer index
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs);
        while (outputBufferIndex >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs);
        }
        Log.e("Media", "onFrame end");
        return true;
    }
}

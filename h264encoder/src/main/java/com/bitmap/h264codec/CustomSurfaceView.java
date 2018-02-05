package com.bitmap.h264codec;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.bitmap.h264codec.encoder.AvcEncoder;
import com.bitmap.h264codec.sender.BaseSender;
import com.bitmap.h264codec.sender.FileClientSender;
import com.bitmap.h264codec.sender.UDPClientSender;
import com.bitmap.library.Constant;

import java.io.IOException;

/**
 * @author obo
 * @date 2018/2/2
 */

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CustomSurfaceView";
    AvcEncoder avcCodec;
    byte[] h264 = new byte[Constant.PREVIEW_WIDTH * Constant.PREVIEW_HEIGHT * 3 / 2];
    Handler childHandler;

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        avcCodec = new AvcEncoder(Constant.PREVIEW_WIDTH, Constant.PREVIEW_HEIGHT, Constant.FRAME_RATE, Constant.BIT_RATE);
        getHolder().addCallback(this);
        HandlerThread handlerThread = new HandlerThread("second");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        if (Constant.WRITE_TO_FILE) {
            sender = new FileClientSender();
        } else {
            sender = new UDPClientSender();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.setPreviewCallback(null);
        camera.stopPreview();
    }

    Camera camera;

    private void startPreview(SurfaceHolder holder) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.YV12);
        parameters.setPreviewFrameRate(16);
        parameters.setPreviewSize(Constant.PREVIEW_WIDTH, Constant.PREVIEW_HEIGHT);
        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setPreviewCallback(this);
        camera.startPreview();
    }

    BaseSender sender;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 获取到一帧预览数据
        // 将yv12的帧转化为h254的帧
        final int ret = avcCodec.offerEncoder(data, h264);
        // 发送h264数据
        childHandler.post(new Runnable() {
            @Override
            public void run() {
                sender.send(h264, ret);
            }
        });
    }
}

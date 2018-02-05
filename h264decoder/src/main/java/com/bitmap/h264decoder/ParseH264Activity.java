package com.bitmap.h264decoder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.bitmap.h264decoder.decoder.AvcDecoder;
import com.bitmap.h264decoder.receiver.BaseReceiver;
import com.bitmap.h264decoder.receiver.FileClientReceiver;
import com.bitmap.h264decoder.receiver.ReceiverListener;
import com.bitmap.h264decoder.receiver.UDPClientReceiver;
import com.bitmap.library.Constant;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author obo
 */
public class ParseH264Activity extends AppCompatActivity {
    private static final String TAG = "ParseH264Activity";
    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    BaseReceiver receiver;
    AvcDecoder mAvcDecoder;
    volatile LinkedBlockingQueue<byte[]> linkedBlockingDeque = new LinkedBlockingQueue<>(50);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_parse_h264_file);

        mSurface = findViewById(R.id.preview);
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mAvcDecoder = new AvcDecoder(Constant.PREVIEW_WIDTH, Constant.PREVIEW_HEIGHT, Constant.BIT_RATE, holder.getSurface());
                // 开启线程获取流
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        if (Constant.READ_FROM_FILE) {
                            // 从本地h264文件获取
                            receiver = new FileClientReceiver("/mnt/sdcard/aaaa/h264.h264");
                        } else {
                            // 从远程服务端获取
                            receiver = new UDPClientReceiver();
                        }
                        receiver.receiver(new ReceiverListener() {
                            @Override
                            public void onResponse(byte[] data, int length) {
                                try {
                                    // 获取到数据后放到队列中等待解析
                                    linkedBlockingDeque.put(data.clone());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "udpClientReceiver onReceiver data = " + data.length);
                            }
                        });
                    }
                }.start();

                // 开启线程解析
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        // 使用AVCDecoder来播放流
                        mAvcDecoder.decode(new AvcDecoder.AvcDecoderListener() {
                            @Override
                            public byte[] getEncodedData() {
                                try {
                                    // 从队列中获取数据进行解析
                                    return linkedBlockingDeque.take();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        });
                    }
                }.start();

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

}

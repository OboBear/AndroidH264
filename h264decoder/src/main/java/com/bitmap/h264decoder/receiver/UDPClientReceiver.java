package com.bitmap.h264decoder.receiver;

import android.util.Log;

import com.bitmap.library.Constant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author obo
 * @date 2018/2/3
 */

public class UDPClientReceiver extends BaseReceiver {
    private static final String TAG = "UDPClientReceiver";

    private DatagramSocket mDatagramSocket;
    private DatagramPacket mDatagramPacket;

    public UDPClientReceiver() {
        try {
            mDatagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private static final int MAX_SIZE = 32768;

    private ReceiverListener mReceiverListener;

    @Override
    public void receiver(ReceiverListener receiverListener) {
        mReceiverListener = receiverListener;
        System.out.println("接收端启动......");
        Log.i(TAG, "");

        byte[] receiverData = new byte[MAX_SIZE];
        byte[] requestData = new byte[5];

        InetAddress address = null;
        try {
            address = InetAddress.getByName(Constant.SERVER_URL);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        mDatagramPacket = new DatagramPacket(requestData, requestData.length, address, Constant.SERVER_SEND_PORT);

        Log.i(TAG, "start send");
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        mDatagramSocket.send(mDatagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();


        while (true) {
            // 3、通过UDP的Socket服务将数据包发送出去，使用send方法
            try {
                DatagramPacket response = new DatagramPacket(receiverData, receiverData.length);
                Log.i(TAG, "end send");
                mDatagramSocket.receive(response);
                mReceiverListener.onResponse(receiverData.clone(), response.getLength());
                Log.i(TAG, "end receive");
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "error e = " + e.getMessage());
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        UDPClientReceiver udpClientReceiver = new UDPClientReceiver();
        udpClientReceiver.receiver(new ReceiverListener() {
            @Override
            public void onResponse(byte[] data, int length) {
                System.out.println("");
            }
        });
    }


}

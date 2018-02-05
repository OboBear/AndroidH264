package com.bitmap.h264codec.sender;

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

public class UDPClientSender extends BaseSender {

    private static final int MAX_SEND_SIZE = 32768;
    private DatagramSocket mDatagramSocket;
    private DatagramPacket mDatagramPacket;

    public UDPClientSender() {
        try {
            mDatagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public byte[] sendData = new byte[MAX_SEND_SIZE];
    public int currendSendSize;

    @Override
    public void send(byte[] data, int length) {
        if (currendSendSize + length < MAX_SEND_SIZE) {
            System.arraycopy(data, 0, sendData, currendSendSize, length);
            currendSendSize += length;
            if (currendSendSize + length < MAX_SEND_SIZE) {
                realSend(sendData, currendSendSize);
                currendSendSize = 0;
            }
        } else {
            realSend(sendData, currendSendSize);
            currendSendSize = 0;
            if (length > sendData.length) {
                System.arraycopy(data, 0, sendData, 0, length);
            }
        }
    }

    private void realSend(byte[] data, int length) {
        if (mDatagramPacket == null) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(Constant.SERVER_URL);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            mDatagramPacket = new DatagramPacket(data, length, address, Constant.SERVER_RECEIVE_PORT);
        } else {
            mDatagramPacket.setData(data, 0, length);
        }
        try {
            mDatagramSocket.send(mDatagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

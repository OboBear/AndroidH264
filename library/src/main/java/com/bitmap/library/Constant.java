package com.bitmap.library;

/**
 * @author obo
 * @date 2018/2/5
 */

public class Constant {
    public static final int PREVIEW_WIDTH = 640;
    public static final int PREVIEW_HEIGHT = 480;
    public static final int FRAME_RATE = 16;
    public static final int BIT_RATE = 160000;

    // 服务器地址
    public static final String SERVER_URL = "192.168.31.228";
    // 服务端接收数据的端口
    public static final int SERVER_RECEIVE_PORT = 10086;
    // 生成的h264文件数据是否保存到本地文件, 否的话则发送到网络环境
    public static final boolean WRITE_TO_FILE = false;

    // 服务端提供数据的端口
    public static final int SERVER_SEND_PORT = 10087;
    // 是否从本地文件中读取h264数据, 否的话则从网络环境读取
    public static final boolean READ_FROM_FILE = false;
}

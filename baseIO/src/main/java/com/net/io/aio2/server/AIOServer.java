package com.net.io.aio2.server;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * AIO编程模型中使用了Channel通道进行通信，要借助Buffer向通道进行数据读写，服务端主要有接收、读、写事件，由于是异步的原因每个事件都要配置一个CompletionHandler，作为回调作用
 * 异步IO在调用accept()、read()、write()不会使得线程阻塞，但是需要传入CompletionHandler实例，在未来给系统调用，简称回调。
 * 1、AsynchronousServerSocketChannel.open()打开服务端通道并绑定端口。
 * 2、调用serverSocketChannel.accept接收客户端连接，方法进行连接;
 *  对应的ServerAcceptCompletionHandler作用：1、将当前连接的客户端的socketChannel注册读事件，对应的ServerReadCompletionHandler；2、调用serverSocketChannel继续注册接收事件。
 *  对应的ServerReadCompletionHandler的作用：向客户端返回数据完成以后，继续监听该客户端的读事件
 *
 *
 */
public class AIOServer {
    private AsynchronousServerSocketChannel serverSocketChannel;

    public AIOServer() throws IOException {
        serverSocketChannel =AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(12121));
        ThreadLogUtil.printMsg("服务器已启动，等待客户端接入。。。");
        serverSocketChannel.accept(serverSocketChannel,new ServerAcceptCompletionHandler());
    }

    /**
     * 让线程阻塞，使得程序不会走完导致监听不到事件
     */
    public void sync(){
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new AIOServer().sync();
    }

}

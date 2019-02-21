package com.net.io.aio.server;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 服务端 接收时CompletionHandler 第一个参数固定为AsynchronousSocketChannel，第二个参数自定义 参考
 * serverSocketChannel.accept(serverSocketChannel,new ServerAcceptCompletionHandler());
 */
public class ServerAcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel,AsynchronousServerSocketChannel> {

    /**
     *
     * @param socketChannel 连接的客户端socket
     * @param serverSocketChannel 参考 serverSocketChannel.accept第二个参数
     */
    @Override
    public void completed(AsynchronousSocketChannel socketChannel, AsynchronousServerSocketChannel serverSocketChannel) {
        try {
            ThreadLogUtil.printMsg("客户端["+socketChannel.getLocalAddress().toString()+"]连接成功！！！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ThreadLogUtil.printMsg("服务器等待新客户端接入。。。");
        serverSocketChannel.accept(serverSocketChannel,this);
        ByteBuffer byteBuffer =ByteBuffer.allocateDirect(1024);
        socketChannel.read(byteBuffer,byteBuffer,new ServerReadCompletionHandler(socketChannel));

    }

    /**
     *
     * @param exc 异常
     * @param serverSocketChannel 参考 serverSocketChannel.accept第二个参数
     */
    @Override
    public void failed(Throwable exc, AsynchronousServerSocketChannel serverSocketChannel) {

    }
}

package com.net.io.aio2.server;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 写的CompletionHandler 第一个参数固定表示写时内容长度，第二个参数ByteBuffer
 *  * 参考：
 *  * socketChannel.write(byteBuffer,byteBuffer,new ServerWriteCompletionHandler(socketChannel));
 */
public class ServerWriteCompletionHandler implements CompletionHandler<Integer,ByteBuffer> {
private AsynchronousSocketChannel socketChannel;
    public ServerWriteCompletionHandler(AsynchronousSocketChannel socketChannel) {
        this.socketChannel=socketChannel;
    }
    /**
     * 读完成时
     * @param result 写的内容长度
     * @param attachment socketChannel.write的第二个参数
     */
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        if (attachment.hasRemaining()){
            socketChannel.write(attachment,attachment,this);
        }else {
            try {
                ThreadLogUtil.printMsg("服务端回复客户端["+socketChannel.getRemoteAddress().toString()+"]完成,并重新等待客户端回复。。。");
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteBuffer byteBuffer =ByteBuffer.allocateDirect(1024);
            socketChannel.read(byteBuffer,byteBuffer,new ServerReadCompletionHandler(socketChannel));
        }
    }
    /**
     *
     * @param exc 异常
     * @param attachment 参考 socketChannel.write的第二个参数
     */
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
}

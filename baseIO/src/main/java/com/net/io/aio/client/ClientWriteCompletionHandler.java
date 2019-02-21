package com.net.io.aio.client;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * 写的CompletionHandler 第一个参数固定表示写时内容长度，第二个参数ByteBuffer
 *  * 参考：
 *  * socketChannel.write(byteBuffer,byteBuffer,new ClientWriteCompletionHandler(socketChannel,countDownLatch));
 */
public class ClientWriteCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
    private AsynchronousSocketChannel socketChannel;
    private CountDownLatch countDownLatch;

    public ClientWriteCompletionHandler(AsynchronousSocketChannel socketChannel, CountDownLatch countDownLatch) {
        this.socketChannel = socketChannel;
        this.countDownLatch = countDownLatch;
    }
    /**
     * 读完成时
     * @param result 写的内容长度
     * @param attachment socketChannel.write的第二个参数
     */
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        ThreadLogUtil.printMsg("result:"+result);
        if (attachment.hasRemaining()){
            socketChannel.write(attachment,attachment,this);
        }else {
            ByteBuffer byteBuffer =ByteBuffer.allocateDirect(1024);
            socketChannel.read(byteBuffer,byteBuffer,new ClientReadCompletionHandler(socketChannel,countDownLatch));
        }
    }
    /**
     *
     * @param exc 异常
     * @param attachment 参考 socketChannel.write的第二个参数
     */
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        ThreadLogUtil.printMsg(exc.getMessage());
        try {
            socketChannel.close();
            countDownLatch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

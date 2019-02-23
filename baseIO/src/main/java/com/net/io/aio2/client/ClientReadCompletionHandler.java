package com.net.io.aio2.client;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;


/**
 * 读的情况，第一个参数Integer 是固定的表示 读取的内容长度 ，第二个参数ByteBuffer
 * 参考：
 * socketChannel.read(byteBuffer,byteBuffer,new ClientReadCompletionHandler(socketChannel,countDownLatch));
 */
public class ClientReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
    private AsynchronousSocketChannel socketChannel;
    private CountDownLatch countDownLatch;

    public ClientReadCompletionHandler(AsynchronousSocketChannel socketChannel, CountDownLatch countDownLatch) {
        this.socketChannel = socketChannel;
        this.countDownLatch = countDownLatch;
    }

    /**
     * 读完成时
     * @param result 读取内容长度
     * @param attachment socketChannel.read的第二个参数
     */
    @Override
    public void completed(Integer result, ByteBuffer attachment) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //flip操作：把当前游标值赋值给结束标志，重置游标为0，此时读取的才是有效数据
        attachment.flip();

        ThreadLogUtil.printMsg("result:"+result);
        byte[] bs = new byte[attachment.remaining()];
        attachment.get(bs);
        try {
            String msg =new String(bs,"UTF-8");
            ThreadLogUtil.printMsg(msg);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
    /**
     *
     * @param exc 异常
     * @param attachment 参考 socketChannel.read的第二个参数
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

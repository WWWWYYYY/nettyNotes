package com.net.io.aio2.server;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * 读的情况，第一个参数Integer 是固定的表示 读取的内容长度 ，第二个参数ByteBuffer
 * 参考：
 * socketChannel.read(byteBuffer,byteBuffer,new ServerReadCompletionHandler(socketChannel));
 */
public class ServerReadCompletionHandler implements CompletionHandler<Integer,ByteBuffer> {
    private AsynchronousSocketChannel socketChannel;
    public static String NEW_LINE = System.getProperty("line.separator");
    public ServerReadCompletionHandler(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
    /**
     * 读完成时
     * @param result 读取内容长度
     * @param attachment socketChannel.read的第二个参数
     */
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        //如果条件成立，说明客户端主动终止了TCP套接字，这时服务端终止就可以了
        if (result==-1){
            try {
                ThreadLogUtil.printMsg("客户端["+socketChannel.getRemoteAddress()+"]已经断开了连接");
                socketChannel.close();
            } catch (IOException e) {
                ThreadLogUtil.printMsg("客户端异常！！！");
            }
            return;
        }
        ThreadLogUtil.printMsg("消息长度："+result);

        //flip操作：把当前游标值赋值给结束标志，重置游标为0，此时读取的才是有效数据
        attachment.flip();

        byte[] bs =new byte[attachment.remaining()];
        attachment.get(bs);

        try {
            String msg =new String(bs,"utf-8");
            String addr=socketChannel.getRemoteAddress().toString();
            ThreadLogUtil.printMsg("客户端["+addr+"]发送信息：["+msg+"]");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            responseClient();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void responseClient() throws IOException {

        String str =new Date().toString()+NEW_LINE;
        ThreadLogUtil.printMsg("服务端回复客户端["+socketChannel.getRemoteAddress().toString()+"]内容：["+str+"]");
        byte[] bs =str.getBytes();
        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(bs.length);
        byteBuffer.put(bs);
        byteBuffer.flip();
        socketChannel.write(byteBuffer,byteBuffer,new ServerWriteCompletionHandler(socketChannel));
    }
    /**
     *
     * @param exc 异常
     * @param attachment 参考 socketChannel.read的第二个参数
     */
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
}

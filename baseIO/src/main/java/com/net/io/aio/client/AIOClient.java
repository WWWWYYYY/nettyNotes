package com.net.io.aio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class AIOClient {
    private AsynchronousSocketChannel socketChannel;
    private CountDownLatch countDownLatch;

    private class AIOThread extends Thread{
        private AsynchronousSocketChannel socketChannel;

        public AIOThread(AsynchronousSocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            //在打开通道之后，要连接之后才能通信
            socketChannel.connect(new InetSocketAddress("127.0.0.1",12121),socketChannel,new ClientConnectCompletionHandler());
            try {
                countDownLatch.await();//由于socketChannel.connect是异步方式，借助countDownLatch使程序阻塞住，保证run方法不会结束
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AIOClient() throws IOException {
        socketChannel=AsynchronousSocketChannel.open();//打开一个本地的异步通道
        countDownLatch=new CountDownLatch(1);
        new AIOThread(socketChannel).start();
    }

    public void sendMsg(String msg){
        byte[] bs =msg.getBytes();
        ByteBuffer byteBuffer =ByteBuffer.allocateDirect(bs.length);//申请直接内存，不能直接读取channel数据，只能通过buffer
        byteBuffer.put(bs);
        byteBuffer.flip();
        socketChannel.write(byteBuffer,byteBuffer,new ClientWriteCompletionHandler(socketChannel,countDownLatch));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String NEW_LINE = System.getProperty("line.separator");
        AIOClient client =new AIOClient();
        Thread.sleep(2000);//调用socketChannel.connect不能马上发送，还不知道连接成功没有
        String str ="";
        for (int i=0;i<40;i++){
            str+=new Date().toString();
        }
        client.sendMsg(str+NEW_LINE);
    }

}

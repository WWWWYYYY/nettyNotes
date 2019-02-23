package com.net.io.aio2.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;

/**
 * 客户端写法和服务写法一样。服务端主要有接收、读、写事件，由于是异步的原因每个事件都要配置一个CompletionHandler，作为回调作用
 * 1、调用AsynchronousSocketChannel.open()打开客户端通道
 * 2、调用socketChannel.connect 方法进行连接 对应的CompletionHandler将 客户端状态设置为已连接（由于异步的原因，在写之前必须要知道是否连接了就通过status判断）
 * 3、调用socketChannel.write写，对应的CompletionHandler.complete 调用socketChannel.read 注册读事件 等待服务器返回数据
 *
 * ps：AIO不同BIO，AIO不需要再为一个客户端创建一个线程来处理socket通信了
 */
public class AIOClient {
    public static final int CONNECTED=1;//已连接
    private AsynchronousSocketChannel socketChannel;
    private int status = -1;

    public AIOClient() throws IOException, InterruptedException {
        socketChannel = AsynchronousSocketChannel.open();//打开一个本地的异步通道
        //在打开通道之后，要连接之后才能通信
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 12121), this, new ClientConnectCompletionHandler());//向系统注册连接事件，完成后调用ClientWriteCompletionHandler的complete方法
    }

    public void sendMsg(String msg) throws InterruptedException {
        //判断是否已经连接,调用socketChannel.connect不能马上发送，还不知道连接成功没有
        while (!isConnected()){
            throw new  RuntimeException("服务器还未连接成功,情稍后再试！！");
        }
        byte[] bs = msg.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bs.length);//申请直接内存，不能直接读取channel数据，只能通过buffer
        byteBuffer.put(bs);
        byteBuffer.flip();
        socketChannel.write(byteBuffer, byteBuffer, new ClientWriteCompletionHandler(socketChannel));//向系统注册写事件，完成后调用ClientWriteCompletionHandler的complete方法
    }
    public boolean isConnected(){
        return status==1;
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }





    public static void main(String[] args) throws IOException, InterruptedException {
        String NEW_LINE = System.getProperty("line.separator");
        AIOClient client = new AIOClient();
        String str ;
        Scanner sc =new Scanner(System.in);
        while (true){
            str=sc.next();
            try {
                client.sendMsg(str+ NEW_LINE);
                System.out.println("已经向服务端发送信息["+str+"]等待系统调用complete方法返回结果！！！");
            }catch (Exception e){
                continue;
            }
        }
    }

}

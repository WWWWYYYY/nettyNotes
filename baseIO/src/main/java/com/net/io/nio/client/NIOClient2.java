package com.net.io.nio.client;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 阻塞模式的NIO 不建议使用 NIO阻塞式还不如直接使用BIO
 * 了解即可
 */
public class NIOClient2 {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 12121;
    private SocketChannel socketChannel;

    private class HandleThread implements Runnable {
        private SocketChannel socketChannel;
        private boolean running = false;

        public HandleThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            running = true;
        }

        @Override
        public void run() {
            while (running) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);//申请堆空间
                int count = 0;//通道数据写入buffer，并返回数据长度
                try {
                    count = socketChannel.read(byteBuffer);//阻塞模式下 和BIO一样 在read时进入阻塞状态
                    if (count < 0) {//如果监听到了读事件却没有从通道里读取出数据，则应该取消这个事件并且关闭通道
                        socketChannel.close();
                    } else {//有数据就读取buffer
                        byteBuffer.flip();
                        byte[] bs = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bs);
                        //拿到byte数组以后该干嘛就干嘛。这里只是打印一下
                        ThreadLogUtil.printMsg(new String(bs, "utf-8"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    /**
     * 1、打开选择器：选择器的作用就是注册相应channel的的事件
     * 2、打开客户端socket通道：承载传输数据的通道
     * 3、socket通道连接服务器
     * 4、启动一个监听并处理事件的线程
     * @throws IOException
     */
    public NIOClient2() throws IOException, InterruptedException {
        socketChannel=SocketChannel.open();
        //如果为 true，则此通道将被置于阻塞模式；socketChannel.connect连接不成功会一直阻塞，
        // 如果为 false，则此通道将被置于非阻塞模式；socketChannel.connect不管结果如何都不会阻塞，
        // 如果没有成功通过监听连接事件，监听到以后调用socketChannel.finishConnect方法完成连接操作
        //ps：在连接之前一定要设置非阻塞，不然监听不到连接事件，整个客户端也不能正常使用，如果要使用阻塞型则直接用BIO，并且使用阻塞型程序写法也不一样了。阻塞情况下用不上selector
        socketChannel.configureBlocking(true);
        boolean result = socketChannel.connect(new InetSocketAddress(HOST, PORT));

        new Thread(new HandleThread(socketChannel)).start();
    }

    /**
     * 1、注册读事件：一般向服务器发生信息，服务器都会回复信息，也有不回复的。
     * 2、将数据放入buffer中再写入到channel
     * @param msg
     */
    public boolean sendMsg(String msg) throws IOException {
        if (!socketChannel.isConnected())return false;
        //客户端向服务端发送信息都会等待一个结果。所以需要监听返回数据（读事件）
//        socketChannel.register(selector,SelectionKey.OP_READ);
        byte[] bs =msg.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bs.length);
        byteBuffer.put(bs);
        byteBuffer.flip();
        //在buffer里读数据》然后向通道里写数据 ；所以需要调用flip方法
        socketChannel.write(byteBuffer);
        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NIOClient2 nioClient = new NIOClient2();
        Thread.sleep(1000);

//        存在服务端返回的数据包含了多个数据包
        boolean result = nioClient.sendMsg("123");
        if (!result){
            System.out.println("通道还未连接成功");
            Thread.sleep(1000);
        }
        for (int i=0;i<5;i++){
            nioClient.sendMsg("nihao");
            Thread.sleep(1000);
        }

    }
}

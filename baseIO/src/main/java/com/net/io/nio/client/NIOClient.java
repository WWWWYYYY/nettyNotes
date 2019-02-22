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

public class NIOClient {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 12121;
    private Selector selector;
    private SocketChannel socketChannel;

    private class HandleThread implements Runnable {
        private Selector selector;
        private SocketChannel socketChannel;
        private boolean running =false;

        public HandleThread(Selector selector, SocketChannel socketChannel) throws IOException {
            this.selector=selector;
            this.socketChannel=socketChannel;
            running =true;
        }

        @Override
        public void run() {
            while (running){
                try {
                    //调用select方法后阻塞等待，等到系统通知，当发生连接成功的时间或者、读、写事件时都会让线程从wait状态转为running状态
                    selector.select();
                    //可能同一时间有多个事件发生
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    SelectionKey key;
                    while (iterator.hasNext()){
                        key=iterator.next();
                        //todo 到时候测试不移除是什么状态
                        iterator.remove();//如果不移除就会重复监听
                        try {
                            //处理selectionKey
                            handleKey(key);
                        }catch (IOException e) {
                            e.printStackTrace();
                            if(key!=null){
                                key.cancel();
                                if(key.channel()!=null){
                                    key.channel().close();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 服务端socketChannel 可以监听到 连接事件、读事件、写事件
         * @param key
         * @throws IOException
         */
        private void handleKey(SelectionKey key) throws IOException {
            if (!key.isValid())return;
            SocketChannel channel = (SocketChannel) key.channel();
            if (key.isConnectable()){//如果是连接事件
                if (channel.finishConnect()){
                    ThreadLogUtil.printMsg("连接服务器成功！！！");
                }else {
                    //0表示正常退出，非零参数表示非正常退出
                    System.exit(1);
                }
            }
            if (key.isReadable()){//如果是读事件
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);//申请堆空间
                int count = channel.read(byteBuffer);//通道数据写入buffer，并返回数据长度
                if (count<0){//如果监听到了读事件却没有从通道里读取出数据，则应该取消这个事件并且关闭通道
                    key.cancel();
                    channel.close();
                }else {//有数据就读取buffer
                    byteBuffer.flip();
                    byte[] bs = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bs);
                    //拿到byte数组以后该干嘛就干嘛。这里只是打印一下
                    ThreadLogUtil.printMsg(new String(bs,"utf-8"));
                }
            }
            //写事件.如果客户端向服务端大量的数据，而缓存区是有限的，所以如果还有数据没有传输时，需要注册写事件。
            // 如果监听到写事件说明，通道里的数据传输过了并通道被清空了可以继续把数据写到通道里了
//            if (key.isWritable()){//如果是写事件
//                System.out.println(123);
//            }

        }
    }

    /**
     * 1、打开选择器：选择器的作用就是注册相应channel的的事件
     * 2、打开客户端socket通道：承载传输数据的通道
     * 3、socket通道连接服务器
     * 4、启动一个监听并处理事件的线程
     * @throws IOException
     */
    public NIOClient() throws IOException, InterruptedException {
        selector =Selector.open();//创建选择器(反应器)
        socketChannel=SocketChannel.open();
        //如果为 true，则此通道将被置于阻塞模式；socketChannel.connect连接不成功会一直阻塞，
        // 如果为 false，则此通道将被置于非阻塞模式；socketChannel.connect不管结果如何都不会阻塞，
        // 如果没有成功通过监听连接事件，监听到以后调用socketChannel.finishConnect方法完成连接操作
        //ps：在连接之前一定要设置非阻塞，不然监听不到连接事件，整个客户端也不能正常使用，如果要使用阻塞型则直接用BIO，并且使用阻塞型程序写法也不一样了。阻塞情况下用不上selector
        socketChannel.configureBlocking(false);
        boolean result = socketChannel.connect(new InetSocketAddress(HOST, PORT));
        if (!result){
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
        new Thread(new HandleThread(selector,socketChannel)).start();
    }

    /**
     * 1、注册读事件：一般向服务器发生信息，服务器都会回复信息，也有不回复的。
     * 2、将数据放入buffer中再写入到channel
     * @param msg
     */
    public boolean sendMsg(String msg) throws IOException {
        if (!socketChannel.isConnected())return false;
        //客户端向服务端发送信息都会等待一个结果。所以需要监听返回数据（读事件）
        socketChannel.register(selector,SelectionKey.OP_READ);
        byte[] bs =msg.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bs.length);
        byteBuffer.put(bs);
        byteBuffer.flip();
        //在buffer里读数据》然后向通道里写数据 ；所以需要调用flip方法
        socketChannel.write(byteBuffer);
        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NIOClient nioClient = new NIOClient();
        Thread.sleep(1000);

        //存在服务端返回的数据包含了多个数据包
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

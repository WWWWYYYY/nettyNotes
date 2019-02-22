package com.net.io.nio.server;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    public static final int PORT=12121;

    private class HandleThread implements Runnable {
        private Selector selector;
        private ServerSocketChannel serverSocketChannel;
        private boolean running =false;

        public HandleThread(Selector selector, ServerSocketChannel serverSocketChannel) {
            this.selector = selector;
            this.serverSocketChannel = serverSocketChannel;
            running=true;
        }

        @Override
        public void run() {
            while (running){
                try {
                    selector.select();//selector等待的是所有的channel的事件，不论是serverSocketChannel还是对应客户端的socketChannel
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();//可能同一时间有多个事件发生
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    SelectionKey key;
                    while (iterator.hasNext()){
                        key=iterator.next();
                        iterator.remove();//如果不移除就会重复监听
                        handleKey(key);//处理selectionKey
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleKey(SelectionKey key) throws IOException {
            if (!key.isValid())return;
//            SocketChannel channel = (SocketChannel) key.channel();
            if (key.isAcceptable()){//接收事件
                //1、触发接收事件后，获取对应的socketChannel
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel client = ssc.accept();
                //2、设置对应客户端的socketChannel为非阻塞
                client.configureBlocking(false);
                ThreadLogUtil.printMsg("客户端【"+client.getRemoteAddress()+"】已连接");
                //3、连接成功以后，必然客户端会些数据过来，就要让socketChannel在selector中注册读事件
                client.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()){//读事件，当客户端关闭时也是读事件调用channel.read时抛出异常
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int count = channel.read(byteBuffer);
                if (count<0){
                    key.cancel();
                    channel.close();
                }else {//如果有数据，则处理后并返回结果给客户端
                    byteBuffer.flip();
                    byte[] bs =new byte[byteBuffer.remaining()];
                    byteBuffer.get(bs);
                    String msg=new String(bs,"utf-8");
                    ThreadLogUtil.printMsg("服务器收到【"+channel.getLocalAddress()+"】的信息："+msg);
                    String result = dealData(msg);
                    doWrite(channel,result);
                }
            }
            //写事件.如果服务器向客户端回复大量的数据，而缓存区是有限的，所以如果还有数据没有传输时，需要注册写事件。
            // 如果监听到写事件说明，通道里的数据传输过了并通道被清空了可以继续把数据写到通道里了
//            if (key.isWritable()){
//
//
//            }

        }

        /**
         * 只有写大量数据的时候才需要注册写监听
         * @param channel
         * @param result
         * @throws IOException
         */
        private void doWrite(SocketChannel channel, String result) throws IOException {
            byte[] bs =result.getBytes();
            ByteBuffer buffer =ByteBuffer.allocate(bs.length);
            buffer.put(bs);
            buffer.flip();
            channel.write(buffer);
        }

        /**
         * 处理客户端的数据，并返回相应的数据。
         * @param msg
         * @return
         */
        private String dealData(String msg) {
            return new Date().toString()+msg;
        }
    }

    /**
     * 1、打开selector反应器
     * 2、打开服务端通道
     * 3、绑定服务端通道对应的socket的端口号
     * 4、通道设置非阻塞（NIO一定要设置成非阻塞，设置成阻塞代码写法也不一样了）
     * 5、注册serverSocketChannel的接收事件
     * 6、启动线程 进行事件监听并处理
     * @throws IOException
     */
    public NIOServer() throws IOException {
        selector=Selector.open();
        serverSocketChannel=ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        new Thread(new HandleThread(selector,serverSocketChannel)).start();
        System.out.println("服务器已启动，端口号："+PORT);
    }

    public static void main(String[] args) throws IOException {
        new NIOServer();
    }
}

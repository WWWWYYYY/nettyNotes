package com.net.io.aio.server;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * AIO不需要新启线程，AIO就是中任务读、写、接收、连接方法都是非阻塞的，调用connect、accept、read、write方法都是注册事件监听的行为，注册了以后等待系统调用CompletionHandler的complete方法
 * com.net.io.aio包下的例子是一种错误的思路。
 * 正确例子参考com.net.io.aio2包
 */
@Deprecated
public class AIOServer {
    private AsynchronousServerSocketChannel serverSocketChannel;

    private CountDownLatch countDownLatch;

    private class AIOThread extends Thread{
        private AsynchronousServerSocketChannel serverSocketChannel;

        public AIOThread(AsynchronousServerSocketChannel serverSocketChannel) {
            this.serverSocketChannel = serverSocketChannel;
        }

        @Override
        public void run() {
            ThreadLogUtil.printMsg("服务器已启动，等待客户端接入。。。");
            serverSocketChannel.accept(serverSocketChannel,new ServerAcceptCompletionHandler());
            ThreadLogUtil.printMsg("1111111");
            try {
                countDownLatch.await();
                serverSocketChannel.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public AIOServer() throws IOException {
        serverSocketChannel =AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(12121));
        countDownLatch =new CountDownLatch(1);
        new AIOThread(serverSocketChannel).start();
    }

    public static void main(String[] args) throws IOException {
        new AIOServer();
    }
}

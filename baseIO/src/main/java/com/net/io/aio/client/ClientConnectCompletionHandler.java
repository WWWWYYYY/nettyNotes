package com.net.io.aio.client;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * 客户端 连接的CompletionHandler 第一个类型固定是Void，第二个类型AsynchronousSocketChannel根据自己需要，第二个参数被视为参数进行传递
 * 参考：
 * socketChannel.connect(new InetSocketAddress("127.0.0.1",12121),socketChannel,new ClientConnectCompletionHandler());
 */
@Deprecated
public class ClientConnectCompletionHandler implements CompletionHandler<Void,AsynchronousSocketChannel> {

    public ClientConnectCompletionHandler() {
    }
    /**
     * 客户端连接
     * @param result 固定为null
     * @param attachment 参考 socketChannel.connect的第二个入参
     */
    @Override
    public void completed(Void result, AsynchronousSocketChannel attachment) {
//        countDownLatch.countDown();
        ThreadLogUtil.printMsg("result:"+result);
        ThreadLogUtil.printMsg("连接服务器成功！");
    }

    /**
     *
     * @param exc 异常
     * @param attachment 参考 socketChannel.connect的第二个入参
     */
    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
        ThreadLogUtil.printMsg(exc.getMessage());
        try {
            attachment.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

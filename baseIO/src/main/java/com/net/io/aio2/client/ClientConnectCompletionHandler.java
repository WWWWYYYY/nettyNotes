package com.net.io.aio2.client;

import com.net.tool.ThreadLogUtil;

import java.io.IOException;
import java.nio.channels.CompletionHandler;


/**
 * 客户端 连接的CompletionHandler 第一个类型固定是Void，第二个类型AsynchronousSocketChannel根据自己需要，第二个参数被视为参数进行传递
 * 参考：
 * socketChannel.connect(new InetSocketAddress("127.0.0.1",12121),socketChannel,new ClientConnectCompletionHandler());
 */
public class ClientConnectCompletionHandler implements CompletionHandler<Void,AIOClient> {

    public ClientConnectCompletionHandler() {
    }
    /**
     * 客户端连接
     * @param result 固定为null
     * @param attachment 参考 socketChannel.connect的第二个入参
     */
    @Override
    public void completed(Void result, AIOClient attachment) {
//        countDownLatch.countDown();
        attachment.setStatus(AIOClient.CONNECTED);
        ThreadLogUtil.printMsg("result:"+result);
        ThreadLogUtil.printMsg("连接服务器成功！");
    }

    /**
     *
     * @param exc 异常
     * @param attachment 参考 socketChannel.connect的第二个入参
     */
    @Override
    public void failed(Throwable exc, AIOClient attachment) {
        ThreadLogUtil.printMsg(exc.getMessage());
        try {
            attachment.getSocketChannel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.netty.base.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

    /**
     * 1、创建Bootstrap 客户端
     * 2、绑定线程组
     * 3、绑定服务器地址端口
     * 4、设置channel类型
     * 5、添加handler
     * 6、调用connect方法
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap =new Bootstrap();
        EventLoopGroup group =new NioEventLoopGroup();
        bootstrap.remoteAddress("127.0.0.1",12121)
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelHandler(){

                    @Override
                    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                        System.out.println("已连接上服务器");
                    }

                    @Override
                    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                        System.out.println("已退出服务器");
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        System.out.println("exceptionCaught");
                    }
                });
        ChannelFuture f = bootstrap.connect().sync();
        f.channel().closeFuture().sync();
        group.shutdownGracefully().sync();
    }
}

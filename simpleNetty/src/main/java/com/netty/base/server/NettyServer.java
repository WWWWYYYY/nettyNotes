package com.netty.base.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {


    /**
     * 1、创建ServerBootstrap
     * 2、设置线程组
     * 3、设置通道为NIO模式
     * 4、绑定端口
     * 5、childHandler
     * 6、调用bind方法启动
     * 7、调用sync()使得主线程阻塞
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(12121).childHandler(new ChannelHandler() {
                @Override
                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                    System.out.println("客户端已连接");
                }

                @Override
                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                    System.out.println("客户端已断开");
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    System.out.println("exceptionCaught");
                }
            });
            ChannelFuture f = serverBootstrap.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}

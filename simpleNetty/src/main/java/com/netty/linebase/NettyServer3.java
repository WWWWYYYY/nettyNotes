package com.netty.linebase;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.CharsetUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyServer3 {

    /**
     * 数据包的分隔符：服务端客户端必须一致，并且服务端和客户端都要使用 LineBasedFrameDecoder进行处理
     *
     * 如果使用了系统的分割符对应的LineBasedFrameDecoder
     * 如果使用自定的分割附对应DelimiterBasedFrameDecoder
     */
//    public static String SEPARATOR =System.getProperty("line.separator");
    //自定义分隔符
    public static String MY_SEPARATOR ="!@#";

    private static class ChannelInitializerImpl extends ChannelInitializer<Channel> {
        private AtomicInteger count = new AtomicInteger(0);

        @Override
        protected void initChannel(Channel ch) throws Exception {
//            ch.pipeline().addLast(new LineBasedFrameDecoder(1024))
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,Unpooled.copiedBuffer(MY_SEPARATOR.getBytes())))
                    .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("客户端【" + ctx.channel().remoteAddress() + "】已连接");
                        }

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

                            System.out.println("接收到客户端【" + ctx.channel().remoteAddress() + "】信息" + count.getAndIncrement() + "：" + msg.toString(CharsetUtil.UTF_8));
//                            String msg2 = "nihao" + "，数字：" + count.get() + SEPARATOR;
                            String msg2 = "nihao" + "，数字：" + count.get() + MY_SEPARATOR;
                            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(msg2.getBytes()));
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                            //如果使用了分隔符传输数据不能在channelReadComplete方法里写数据回去。channelReadComplete方法中无法区分不同的数据包
//                            String msg2 ="nihao"+"，数字："+count+System.getProperty("line.separator");
//                            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(msg2.getBytes()));
                        }
                    });
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializerImpl())
                    .localAddress(12121);
            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }

    }
}

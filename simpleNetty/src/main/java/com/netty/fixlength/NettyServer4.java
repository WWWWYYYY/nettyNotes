package com.netty.fixlength;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.CharsetUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyServer4 {

    /**
     * 消息定长：对应的FixedLengthFrameDecoder
     * <p>
     * 1、如果消息不够长，则填充到对应的长度 ,在接收方接收到信息时需要去除填充数据；
     * （填充情况也比较多，处理也比较麻烦，因此尽量是做实际内容的长度固定的消息；例如MD5内容，RSA数据）
     * 2、如果超过长度则应该抛出异常
     */
    public static String CLIENT_CONTENT = "CLIENT_CONTENT";
    public static String SERVER_CONTENT = "SERVER_CONTENT";
    public static int CLIENT_LENGTH = CLIENT_CONTENT.getBytes().length;
    public static int SERVER_LENGTH = SERVER_CONTENT.getBytes().length;

    private static class ChannelInitializerImpl extends ChannelInitializer<Channel> {
        private AtomicInteger count = new AtomicInteger(0);

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new FixedLengthFrameDecoder(CLIENT_LENGTH))
                    .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("客户端【" + ctx.channel().remoteAddress() + "】已连接");
                        }

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                            System.out.println("接收到客户端【" + ctx.channel().remoteAddress() + "】信息" + count.getAndIncrement() + "：" + msg.toString(CharsetUtil.UTF_8));
                            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(SERVER_CONTENT.getBytes()));
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                            //如果使用了消息定长传输数据不能在channelReadComplete方法里写数据回去。channelReadComplete方法中无法区分不同的数据包
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

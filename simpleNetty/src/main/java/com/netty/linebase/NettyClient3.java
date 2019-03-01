package com.netty.linebase;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.CharsetUtil;

public class NettyClient3 {

    /**
     * 数据包的分隔符：服务端客户端必须一致，并且服务端和客户端都要使用 LineBasedFrameDecoder进行处理
     *
     * 如果使用了系统的分割符对应的LineBasedFrameDecoder
     * 如果使用自定的分割附对应DelimiterBasedFrameDecoder
     */
    public static String SEPARATOR =System.getProperty("line.separator");

    private static class ChannelHandlerInit extends ChannelInitializer<Channel>{

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new LineBasedFrameDecoder(1024))
                    .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            for (int i=0;i<10;i++){
                                String msg ="数字："+i+SEPARATOR;
                                System.out.println("连接到服务器["+ctx.channel().remoteAddress()+"]并发送信息："+msg);
                                ctx.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
                            }

                        }

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                            System.out.println("接收到服务端【"+ctx.channel().remoteAddress()+"】信息："+msg.toString(CharsetUtil.UTF_8));
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
        NioEventLoopGroup group =new NioEventLoopGroup();
        try {
            Bootstrap bootstrap =new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress("127.0.0.1",12121)
            .handler(new ChannelHandlerInit());
            ChannelFuture future = bootstrap.connect().sync();
            future.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}

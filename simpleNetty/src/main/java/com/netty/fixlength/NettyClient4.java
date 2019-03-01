package com.netty.fixlength;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.CharsetUtil;

public class NettyClient4 {

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
    private static class ChannelHandlerInit extends ChannelInitializer<Channel>{

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new FixedLengthFrameDecoder(SERVER_LENGTH))
                    .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            for (int i=0;i<10;i++){
                                System.out.println("连接到服务器["+ctx.channel().remoteAddress()+"]并发送信息："+CLIENT_CONTENT);
                                ctx.writeAndFlush(Unpooled.copiedBuffer(CLIENT_CONTENT.getBytes()));
                            }

                        }

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                            System.out.println("接收到服务端【"+ctx.channel().remoteAddress()+"】信息："+msg.toString(CharsetUtil.UTF_8));
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

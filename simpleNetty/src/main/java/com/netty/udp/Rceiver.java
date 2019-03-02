package com.netty.udp;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

/**
 * udp接收方
 * 1、创建接收方Bootstrap
 * 2、设置NIO线程组
 * 3、设置channel类型为NioDatagramChannel
 * 4、设置handler
 * 5、绑定端口
 */
public class Rceiver {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group =new NioEventLoopGroup();

        try {
            //1、创建接收方Bootstrap
            Bootstrap bootstrap =new Bootstrap();
            //2、设置NIO线程组
            bootstrap.group(group)
                    //3、设置channel类型为NioDatagramChannel
                    .channel(NioDatagramChannel.class)
                    //4、设置handler
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                            ByteBuf content = msg.content();
                            String command =content.toString(CharsetUtil.UTF_8);
                            System.out.println("接收到的信息："+command);
                            if (Command.COMMAND_MAKE_RESP.equals(command)){
                                //回复DatagramPacket报文
                                ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(Command.COMMAND_CLOSE,CharsetUtil.UTF_8),msg.sender()));
                            }
                        }
                    });
            //5、绑定端口
            ChannelFuture channelFuture = bootstrap.bind(12121).sync();
            System.out.println("应答服务已启动.....");
            channelFuture.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}

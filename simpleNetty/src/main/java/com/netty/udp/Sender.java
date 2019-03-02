package com.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
/**
 * udp发送方
 * 1、创建接收方Bootstrap
 * 2、设置NIO线程组
 * 3、设置channel类型为NioDatagramChannel
 * 4、设置handler
 * 5、绑定端口
 */
public class Sender {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group =new NioEventLoopGroup();
        try {
            //1、创建接收方Bootstrap
            Bootstrap bootstrap=new Bootstrap();
            //2、设置NIO线程组
            bootstrap.group(group)
                    //、设置channel类型为NioDatagramChannel
                    .channel(NioDatagramChannel.class)
                    //4、设置handler
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>(){
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                            ByteBuf content = msg.content();
                            String data =content.toString(CharsetUtil.UTF_8);
                            System.out.println("收到服务端回复信息:"+data);
                            if (Command.COMMAND_CLOSE.equals(data)){
                                ctx.close();
                            }
                        }
                    });
            //5、绑定端口
            Channel channel = bootstrap.bind(0).sync().channel();
            //6、发送DatagramPacket报文
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(Command.COMMAND_MAKE_RESP,CharsetUtil.UTF_8)
                    ,new InetSocketAddress("127.0.0.1",12121)));
            //不知道接收端能否收到报文，也不知道能否收到接收端的应答报文
            // 所以等待15秒后，不再等待，关闭通信
            if(!channel.closeFuture().await(15000)){
                System.out.println("查询超时！");
            }
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}

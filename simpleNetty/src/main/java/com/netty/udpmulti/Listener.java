package com.netty.udpmulti;

import com.netty.udp.Command;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * udp 订阅者
 */
public class Listener {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group =new NioEventLoopGroup();
        try {
            //1、创建接收方Bootstrap
            Bootstrap bootstrap=new Bootstrap();
            //2、设置NIO线程组
            bootstrap.group(group)
                    //、设置channel类型为NioDatagramChannel
                    .channel(NioDatagramChannel.class)
                    //设置套接字选项 SO_BROADCAST
                    .option(ChannelOption.SO_BROADCAST, true)
                    //允许端口重用
                    .option(ChannelOption.SO_REUSEADDR,true)
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
            bootstrap.localAddress(new InetSocketAddress(6789));
            //5、绑定端口
            Channel channel = bootstrap.bind().syncUninterruptibly().channel();
            System.out.println("UdpAnswerSide running");
            channel.closeFuture().sync();

            //不知道接收端能否收到报文，也不知道能否收到接收端的应答报文
            // 所以等待15秒后，不再等待，关闭通信
            if(!channel.closeFuture().await(150000)){
                System.out.println("查询超时！");
            }
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}

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
import java.net.SocketAddress;

/**
 * udp 广播 即类似事件发布者
 */
public class BroadCaster {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group =new NioEventLoopGroup();

        try {
            //1、创建接收方Bootstrap
            Bootstrap bootstrap =new Bootstrap();
            //255.255.255.255存在问题，发送不出去
            final InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1",6789);
            //2、设置NIO线程组
            bootstrap.group(group)
                    //3、设置channel类型为NioDatagramChannel
                    .channel(NioDatagramChannel.class)
                    //设置 SO_BROADCAST 套接字选项
                    .option(ChannelOption.SO_BROADCAST,true)
                    //4、设置handler
                    .handler(new ChannelOutboundHandlerAdapter() {
                    });

            //5、绑定端口
            Channel channel = bootstrap.bind(0).sync().channel();
            System.out.println("应答服务已启动.....");
            while (true){
                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(Command.COMMAND_CLOSE,CharsetUtil.UTF_8),inetSocketAddress));
                Thread.sleep(2000);
            }
//            channel.closeFuture().sync();//阻塞的
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}

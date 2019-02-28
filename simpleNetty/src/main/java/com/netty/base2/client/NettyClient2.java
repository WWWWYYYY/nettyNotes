package com.netty.base2.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

public class NettyClient2 {

    /**
     * 1、创建Bootstrap 客户端
     * 2、绑定线程组
     * 3、绑定服务器地址端口
     * 4、设置channel类型
     * 5、添加handler
     * 6、调用connect方法
     *
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress("127.0.0.1", 12121)
                    .group(group)
                    .channel(NioSocketChannel.class)
                    //服务端调用childHandler方法，客户端调用handler方法
                    //handler在初始化时就会执行，而childHandler会在客户端成功connect后才执行
                    //pipeline是伴随Channel的存在而存在的，交互信息通过它进行传递，我们可以addLast（或者addFirst）多个handler
                    .handler(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("连接上服务器啦");
                            ctx.writeAndFlush(Unpooled.copiedBuffer("hello nihao",CharsetUtil.UTF_8));
                        }
                        //客户端和服务端通信要指定信息的数据类型bytebuf，不然解析不了
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.out.println("接受到服务器数据："+((ByteBuf)msg).toString(CharsetUtil.UTF_8));
//                            ctx.writeAndFlush(Unpooled.copiedBuffer("hello nihao",CharsetUtil.UTF_8));
                        }
                    });
            ChannelFuture f = bootstrap.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}

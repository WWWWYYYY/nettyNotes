package com.netty.base2.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;

public class NettyServer2 {


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
                    .localAddress(12121)
                    //服务端调用childHandler方法，客户端调用handler方法
                    //handler在初始化时就会执行，而childHandler会在客户端成功connect后才执行
                    //pipeline是伴随Channel的存在而存在的，交互信息通过它进行传递，我们可以addLast（或者addFirst）多个handler
                    .childHandler(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("客户端已连接成功");
                        }

                        //客户端和服务端通信要指定信息的数据类型bytebuf，不然解析不了
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf byteBuf = (ByteBuf) msg;
                            Channel channel = ctx.channel();
                            //将buf转换成字符串
                            String str = byteBuf.toString(CharsetUtil.UTF_8);
                            System.out.println("接收到客户端【" + channel.remoteAddress() + "】信息:" + str);
                            //1、将数据传递到下一个channelHandler
//                            ctx.fireChannelRead(msg);
                            //2、将数据释放
//                            ReferenceCountUtil.release(msg);
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                            ByteBufAllocator alloc = ctx.alloc();//默认返回池化的
                            ByteBufAllocator alloc1 = ctx.channel().alloc();//默认返回池化的
                            ByteBuf buffer = alloc.buffer();//池化的buf
                            ByteBuf buffer2 = alloc1.buffer();//池化的buf
                            ByteBuf buffer1 = Unpooled.buffer();//每次都会在堆上创建新的缓存区 非池化的buf
                            ByteBuf byteBuf = Unpooled.directBuffer();//每次都会在直接内存上创建新的缓存区 非池化的buf

                            ctx.writeAndFlush(Unpooled.copiedBuffer(new Date().toString(), CharsetUtil.UTF_8));
//                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

                            /**
                             * 例子
                             * 1、2、4代表ChannelInboundHandler；3、5代表ChannelOutboundHandler
                             * 当前channelHandler表示4. 在4对应的channelhandler调用了write方法
                             *      ----------------------------------
                             *      <<<<<<<<<<<<<<<<<< 3 <<<<<<<< 5 <<出栈
                             *      ----------------------------------
                             *   入栈>>>>> 1 >>>>> 2 >>>>>> 4 >>>>>>>>>
                             *      ----------------------------------
                             *
                             *
                             *
                             * （1）Channel.write()//从出栈的第一个ChannelOutboundHandler开始写出如图所示从5开始写
                             * （2）PipeLine.write()//从出栈的第一个ChannelOutboundHandler开始写出如图所示从5开始写
                             * （3）ChannelHandlerContext.write()//向着出栈的方向距离 当前handler最近的一个channelouthandler开始写，如图所示加入当前handler=1或者2或者4，都会从3开始写
                             * ChannelHandlerContext.write 好处就是可以不经过不必要的outhandler。根据业务合理的设计，比如数据校验失败则应该直接返回，就没有必要通过过多的outhandler
                             */
                        }
                    });
            ChannelFuture f = serverBootstrap.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

}

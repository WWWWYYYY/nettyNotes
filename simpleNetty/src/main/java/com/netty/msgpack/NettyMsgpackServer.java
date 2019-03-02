package com.netty.msgpack;

import com.netty.protobuf.StudentProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class NettyMsgpackServer {

    private static class ChannelInitializerImpl extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipe = ch.pipeline();
//          1、解析数据包长度 参数1：表示包的最大字节长度，参数2：表示 表示长度的数据从哪个字节开始（不一定都是0开始的），参数3表示 表示长度的数据字节长度，
//              参数4表示数据包的长度调整的值，参数5表示真正的数据从哪个字节开始即参数2+参数3
//              （真正的数据长度=数据包总长度-参数2-参数3-参数4）
            pipe.addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
//          2、数据解码
            pipe.addLast(new MsgpackDecode());//
//          3、业务handler
            pipe.addLast(new ChannelInboundHandlerAdapter(){
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    User user = (User) msg;
                    System.out.println("收到客户端信息："+user.toString());
                    //服务器的应答
                    String resp = "I process user :"+user.getUsername()
                            + System.getProperty("line.separator");
                    ctx.writeAndFlush(Unpooled.copiedBuffer(resp,CharsetUtil.UTF_8));
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

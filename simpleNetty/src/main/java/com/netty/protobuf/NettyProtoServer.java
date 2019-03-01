package com.netty.protobuf;

import com.netty.linebase.NettyServer3;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

public class NettyProtoServer {

    private static class ChannelInitializerImpl extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipe = ch.pipeline();
//          1、解析数据包长度
            pipe.addLast(new ProtobufVarint32FrameDecoder());
//          2、数据解码成StudentProto.Student
            pipe.addLast(new ProtobufDecoder(StudentProto.Student.getDefaultInstance()));//
//          3、业务handler
            pipe.addLast(new ChannelInboundHandlerAdapter(){
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    StudentProto.Student student= (StudentProto.Student) msg;
                    System.out.println(student.getName());
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

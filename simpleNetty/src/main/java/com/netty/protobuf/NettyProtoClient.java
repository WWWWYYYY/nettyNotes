package com.netty.protobuf;

import com.netty.linebase.NettyClient3;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class NettyProtoClient {
    private static class ChannelInitializerImpl extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipe = ch.pipeline();
            //1、添加报文长度字段 解决粘包半包问题
            pipe.addLast(new ProtobufVarint32LengthFieldPrepender());
            //2、proto编码
            pipe.addLast(new ProtobufEncoder());
            //3、业务handler
            pipe.addLast(new ChannelInboundHandlerAdapter(){
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    StudentProto.Student.Builder builder = StudentProto.Student.newBuilder();
                    builder.setName("wang");
                    builder.setAge(11);
                    builder.setAddress("12345");
                    StudentProto.Student student = builder.build();
                    System.out.println("向服务端发送信息:"+student.toString());
                    ctx.writeAndFlush(student);
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
                    .handler(new ChannelInitializerImpl());
            ChannelFuture future = bootstrap.connect().sync();
            future.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully().sync();
        }
    }
}

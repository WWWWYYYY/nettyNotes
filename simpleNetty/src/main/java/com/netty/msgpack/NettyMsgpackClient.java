package com.netty.msgpack;

import com.netty.protobuf.StudentProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.CharsetUtil;

public class NettyMsgpackClient {
    private static class ChannelInitializerImpl extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipe = ch.pipeline();
            //1、添加报文长度字段 解决粘包半包问题   //2表示 2两个字节所表示的的最大值即65536 ；表示发送的信息的字节长度 且不能超过65536
            pipe.addLast("frameEncoder",new LengthFieldPrepender(2));
            //2、msgpack 序列化 发送到服务端之前数据序列化
            pipe.addLast(new MsgpackEncode());
            //3、服务端返回的是字符串  因此使用了系统分隔符分割数据包
            pipe.addLast(new LineBasedFrameDecoder(1024));
            //4、业务handler
            pipe.addLast(new ChannelInboundHandlerAdapter(){
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    User[] us =makeUsers();
                    for (User u : us) {
                        ctx.write(u);
                    }
                    ctx.flush();
                }

                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    ByteBuf byteBuf = (ByteBuf) msg;
                    System.out.println(byteBuf.toString(CharsetUtil.UTF_8));
                }

                private User[] makeUsers(){
                    User[] users=new User[5];
                    User user =null;
                    for(int i=0;i<5;i++){
                        user=new User();
                        user.setAge("12");
                        String userName = "ABCDEFG --->"+i;
                        user.setUsername(userName);
                        users[i]=user;
                    }
                    return users;
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

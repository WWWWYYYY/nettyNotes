package com.netty.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;

public class NettyHttpClient {
    private static class ChannelHandlerInit extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
//            pipeline.addLast("encode", new HttpClientCodec());//对发出请求前 编码成httprequest格式 HttpClientCodec的功能等于 HttpRequestEncoder + HttpResponseDecoder
            pipeline.addLast("encode", new HttpRequestEncoder());//对发出请求前 编码成httprequest格式
            pipeline.addLast("decode", new HttpResponseDecoder());//接收到服务器响应时 解码
            //对请求体做聚合操作，可能body太大要分好几次传输，而聚合操作让开发人员不要再去关系body的组装。
            pipeline.addLast("aggre", new HttpObjectAggregator(1024 * 1024 * 10));
            pipeline.addLast("decompress", new HttpContentDecompressor());
            pipeline.addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    FullHttpResponse response = (FullHttpResponse) msg;
                    HttpHeaders headers = response.headers();
                    System.out.println("heads:"+headers.toString());
                    ByteBuf content = response.content();
                    System.out.println("body:"+content.toString(CharsetUtil.UTF_8));
                    response.release();//接收完后释放
                }
            });
        }
    }
    public static void sendMsg(Channel channel,String msg) throws URISyntaxException, InterruptedException {

        URI uri =new URI("/test");
        DefaultFullHttpRequest request =new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.GET,
                uri.toASCIIString(),Unpooled.copiedBuffer(msg,CharsetUtil.UTF_8));
        request.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH,msg.getBytes().length);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH,msg.getBytes().length);
        request.headers().set(HttpHeaderNames.HOST,"127.0.0.1");
        channel.writeAndFlush(request);
        channel.closeFuture().sync();
    }

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group =new NioEventLoopGroup();
        try {
            Bootstrap bootstrap =new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress("127.0.0.1",12121)
                    .handler(new ChannelHandlerInit());
            ChannelFuture future = bootstrap.connect().sync();
//            future.channel().closeFuture().sync();
            sendMsg(future.channel(),"nihao");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

}

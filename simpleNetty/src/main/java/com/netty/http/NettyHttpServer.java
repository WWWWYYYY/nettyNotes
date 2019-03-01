package com.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.CharsetUtil;

/**
 * 浏览器访问路径：localhost:12121/test
 * 非法路径：localhost:12121/test
 */
public class NettyHttpServer {

    private static class ChannelInitializerImpl extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            /*
            //netty为我们提供的ssl加密，缺省
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(),
                    ssc.privateKey()).build();
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));*/

            pipeline.addLast("decode", new HttpRequestDecoder());//对请求解码
            pipeline.addLast("encode", new HttpResponseEncoder());//对相应编码
            //对请求体做聚合操作，可能body太大要分好几次传输，而聚合操作让开发人员不要再去关系body的组装。
            pipeline.addLast("aggre", new HttpObjectAggregator(1024 * 1024 * 10));
            pipeline.addLast("compress", new HttpContentCompressor());
            //添加业务handler
            pipeline.addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    FullHttpRequest request = (FullHttpRequest) msg;
                    String uri = request.uri();
                    String body = request.content().toString(CharsetUtil.UTF_8);
                    HttpMethod method = request.method();
                    System.out.println("client:"+ctx.channel().remoteAddress());
                    System.out.println("uri:"+uri);
                    System.out.println("heads:"+request.headers());
                    System.out.println("body:"+body.toString());
                    try {
                        if (!"/test".equals(uri)) {
                            send(ctx, "非法请求", HttpResponseStatus.BAD_REQUEST);
                            return;
                        }
                        if (HttpMethod.GET.equals(method)) {
                            send(ctx, "abcd", HttpResponseStatus.OK);
                            return;
                        }
                        if (HttpMethod.POST.equals(method)) {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("请求处理失败！！");
                    } finally {
                        request.release();//请求处理完释放资源
                    }
                }

                private void send(ChannelHandlerContext ctx, String msg, HttpResponseStatus status) {
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
                    response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=utf-8");//设置响应头
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);//http请求，写完后关闭客户端channel
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

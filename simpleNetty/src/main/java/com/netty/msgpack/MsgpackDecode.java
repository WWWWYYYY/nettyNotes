package com.netty.msgpack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * 数据编码：将对象序列化
 */
public class MsgpackDecode extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int length = msg.readableBytes();
        byte[] bs =new byte[length];
        msg.getBytes(msg.readerIndex(),bs,0,length);
        MessagePack messagePack =new MessagePack();
        out.add(messagePack.read(bs,User.class));
    }
}

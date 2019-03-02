package com.netty.msgpack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * 数据编码：将对象序列化
 */
public class MsgpackEncode extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        MessagePack messagePack =new MessagePack();
        byte[] raw =messagePack.write(msg);
        out.writeBytes(raw);
    }
}

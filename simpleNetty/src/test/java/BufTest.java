import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ByteProcessor;
import io.netty.util.ReferenceCountUtil;

import java.util.Arrays;

public class BufTest {


    public static void main(String[] args) {
        test5();
    }

    /**
     * byteBuf 基本信息
     */
    private static void test(){
        ByteBuf buf = Unpooled.buffer(10);
        System.out.println(buf.toString());//ridx: 0, widx: 0, cap: 10
        System.out.println("isReadable():"+buf.isReadable());//如果至少有一个字节可供读取，则返回true
        System.out.println("isReadable():"+buf.isWritable());//如果至少有一个字节可被写入，则返回true
        System.out.println("writerIndex:"+buf.writerIndex());
        System.out.println("readerIndex:"+buf.readerIndex());
        System.out.println("readableBytes()):"+buf.readableBytes());//返回可被读取的字节数
        System.out.println("writableBytes()):"+buf.writableBytes());//返回可被写入的字节数
        System.out.println("hasArray()):"+buf.hasArray());//如果ByteBuf 由一个字节数组支撑，则返回true
        System.out.println("array()):"+buf.array());//如果 ByteBuf 由一个字节数组支撑则返回该数组；否则，它将抛出一个UnsupportedOperationException 异常
    }

    /**
     * buf 简单的读写
     * 写时根据写入的数据类型占用几个自己来调整 写索引
     * 读时根据读出时的数据类型占用几个自己来调整 读索引
     * 如果不够写或者不够读则抛出异常
     */
    private static void test2(){
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeLong(1l);//写入8个字节
        buf.writeByte(0);//写入一个字节
        System.out.println(buf.toString()+"arrays:"+Arrays.toString(buf.array()));//(ridx: 0, widx: 8, cap: 10)arrays:[0, 0, 0, 0, 0, 0, 0, 1, 0, 0]
        int a = buf.readInt();//读取四个字节
        System.out.println("a="+a);
        printBufInfo(buf);

        a = buf.readByte();//读取一个字节
        a = buf.readInt();//读取四个字节
        System.out.println("a="+a);
        printBufInfo(buf);
    }

    /**
     * buf 中 set 和get 根据数据类型 read 或者 write 相应的数据类型长度
     * set和 get不改变writerIndex 、readerIndex
     */
    private static void test3(){
        ByteBuf buf = Unpooled.buffer(10);
        buf.setInt(1,1);//从下标为1开始写4个字节
        printBufInfo( buf);
        buf.setInt(3,1);//从小标为3开始写四个字节
        printBufInfo( buf);
        buf.getByte(1);//读取下标为1的字节
        buf.getUnsignedByte(2);//读取下标为2的字节
        printBufInfo( buf);
    }

    /**
     * 读写逻辑上的关系：
     * 可读字节长度=ridx-widx
     * 可写字节长度=Capacity-ridx
     * 可丢弃字节长度=ridx
     *
     * discardReadBytes：清除已经读过得字节。将未读过的字节从第一个字节开始放
     *
     * clear 仅仅是将ridx设置为0，widx设置为0
     */
    private static void test4(){
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeInt(21122121);//写入4个字节 ridx: 0, widx: 4, cap: 10 readableBytes()):4 writableBytes()):6
        printBufInfo( buf);
        buf.readByte();//读取一个字节 ridx: 1, widx: 4, cap: 10 readableBytes()):3 writableBytes()):6
        printBufInfo( buf);
        buf.discardReadBytes();//清除已经读过得字节。将未读过的字节从第一个字节开始放；这个操作虽然让buf持续可用，但是底层使用了数组复制是一个消耗性能的操作。使用情况在内存比较宝贵的情况
        printBufInfo( buf);

        buf.clear();
        printBufInfo( buf);

    }

    /**
     * 查找Buf中的内容
     */
    private static void test5(){
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeInt(21122121);//写入4个字节 (ridx: 0, widx: 4, cap: 10)arrays:[1, 66, 76, 73, 0, 0, 0, 0, 0, 0]
        printBufInfo( buf);
        int i = buf.indexOf(0, buf.capacity(), (byte) 66);
        System.out.println("66 的下标为:"+i);
        //如果想要查找复杂的数据，例如超过"ab"
        int i1 = buf.forEachByte(new ByteProcessor() {
            @Override
            public boolean process(byte value) throws Exception {
                if (value == (byte)73) return true;
                return false;
            }
        });

//        buf.forEachByteDesc(new ByteProcessor(){});//倒序查找

        int i2 = buf.forEachByte(ByteProcessor.FIND_NUL);//ByteProcessor类中已经有了一些常用的工具
        System.out.println(i2);

    }

    /**
     * 派生buf 和copy buf 和 引用计数
     * 派生buf 和原buf使用的是同一个内存空间
     *
     * copy buf 和原buf 是不同的内存空间
     *
     * buf引用计数
     */
    private static void test6(){
        ByteBuf buf = Unpooled.buffer(10);
        ByteBuf duplicate = buf.slice(0,5);//派生buf ，只映射0~4
        ByteBuf copy = buf.copy(3,6);//重新复制一份长度为6

        ReferenceCountUtil.release(buf);//引用计数-1，并不是真正的释放

    }

    /**
     * buf工具类 ByteBufUtil
     */
    private static void test7(){
        ByteBuf buf = Unpooled.buffer(10);
        buf.writeInt(21122121);//写入4个字节 (ridx: 0, widx: 4, cap: 10)arrays:[1, 66, 76, 73, 0, 0, 0, 0, 0, 0]
        printBufInfo( buf);

    }
    /**
     * 打印基本信息 测试用
     * @param buf
     */
    private static void printBufInfo(ByteBuf buf){
        System.out.println("**************buf info*********************");
        System.out.println(buf.toString()+"arrays:"+Arrays.toString(buf.array()));
        System.out.println("writerIndex:"+buf.writerIndex());
        System.out.println("readerIndex:"+buf.readerIndex());
        System.out.println("readableBytes()):"+buf.readableBytes());//返回可被读取的字节数
        System.out.println("writableBytes()):"+buf.writableBytes());//返回可被写入的字节数
        System.out.println("----------------------------------");
    }
}

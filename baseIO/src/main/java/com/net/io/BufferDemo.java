package com.net.io;

import java.nio.ByteBuffer;

/**
 * Buffer 子类有很多常用ByteBuffer
 * 程序不能直接读取channel中的数据 必须通过Buffer来读取。
 *
 * Buffer可以在堆上申请内存、申请直接内存
 * 堆上分配：占用堆空间，回收快
 * 直接内存分配：网络读写更快，不影响虚拟机剩余可用空间，但是回收慢，不是必要情况不要用，一遍在网络通信的时候用。
 *
 * Buffer中重要的三个属性
 * capacity：最大容量 读模式和写模式都不会变话
 * position：游标位置
 * limit：读写的最大位置，
 *
 * 在写模式时，position=写出的内容长度，limit=capacity，
 * 在读模式时limit=positon，position=0
 * 通过Buffer.flip方法从写模式切换到读模式。
 *
 * 往buffer中写数据的两种方式：
 * 1、从channel中读出并写入到buffer中：channel.read(buffer)
 * 2、直接往buffer放数据:buffer.put(bs)
 *
 * 从buffer中读数据的两种方式：
 *  * 1、从channel写入并读出到buffer中：channel.write(buffer)
 *  * 2、直接往buffer读数据:buffer.get(bs)
 */
public class BufferDemo {

    public static void main(String[] args) {
        test9();

    }

    /**
     * JVM参数：-Xms5m -Xmx5m 堆大小5m
     * 堆上申请空间
     */
    public static void test(){
        System.out.println("heap size："+Runtime.getRuntime().freeMemory());
        ByteBuffer buffer =ByteBuffer.allocate(1024000);//在堆上申请了1m的空间
        System.out.println("heap size："+Runtime.getRuntime().freeMemory());
    }

    /**
     * 申请直接内存
     * 并不影响堆空间
     */
    public static void test2(){
        System.out.println("heap size："+Runtime.getRuntime().freeMemory());
        ByteBuffer buffer =ByteBuffer.allocateDirect(1024000);//在堆上申请了1m的空间
        System.out.println("heap size："+Runtime.getRuntime().freeMemory());
    }

    /**
     *  buffer 读、写 和模式切换
     */
    public static void test3(){
        ByteBuffer buffer =ByteBuffer.allocate(32);
        System.out.println("申请堆内存后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put((byte) 'a');
        buffer.put((byte) 'b');
        buffer.put((byte) 'c');
        System.out.println("写入数据后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=3 lim=32 cap=32]
        buffer.flip();//写模式切换到读模式
        System.out.println("写模式切换到读模式后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=3 cap=32]
        System.out.println(buffer.get());
        System.out.println("读模式下获取了一个元素后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=1 lim=3 cap=32]
        buffer.flip();//读模式切换到写模式时
        System.out.println("读模式切换到写模式后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=1 cap=32]
    }

    /**
     * buffer.get
     */
    public static void test4(){
        ByteBuffer buffer =ByteBuffer.allocate(32);
        System.out.println("申请堆内存后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put((byte) 'a');
        buffer.put((byte) 'b');
        buffer.put((byte) 'c');
        System.out.println("写入数据后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=3 lim=32 cap=32]
        buffer.flip();//写模式切换到读模式
        System.out.println("写模式切换到读模式后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=3 cap=32]
        System.out.println(buffer.get(1));//pos游标不会发生变化
        System.out.println("buffer.get(1))后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=3 cap=32]
        byte[] bytes =new  byte[10];
        buffer.get(bytes,0,2);//pos=2游标会发生变化
        System.out.println("buffer.get(bytes,0,2)后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=2 lim=3 cap=32]
    }
    /**
     * buffer.put
     */
    public static void test5(){
        ByteBuffer buffer =ByteBuffer.allocate(32);
        System.out.println("申请堆内存后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put(2,(byte)'z');//带索引的情况 pos游标不会改变
        System.out.println("buffer.put(2,(byte)'z');后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put((byte) 'a');
        buffer.put((byte) 'b');
        buffer.put((byte) 'c');
        System.out.println("写入3个数据后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=3 lim=32 cap=32]
        byte[] bs=buffer.array();//游标pos不会变化
        System.out.println("buffer.array();后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=3 lim=32 cap=32]
    }
    /**
     * buffer其他重要的方法
     * mark()：标记当前的pos位置
     * reset()：将游标pos重置为上一次调用mark()方法的位置
     */
    public static void test6(){
        ByteBuffer buffer =ByteBuffer.allocate(32);
        System.out.println("申请堆内存后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put((byte) 'a');
        System.out.println("写入1个数据后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=1 lim=32 cap=32]
        buffer.mark();//标记当前的pos位置
        System.out.println("buffer.mark()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=1 lim=32 cap=32]
        buffer.put((byte) 'b');
        buffer.put((byte) 'c');
        System.out.println("写入2个数据后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=3 lim=32 cap=32]
        buffer.reset();//将游标pos重置为上一次调用mark()方法的位置。
        System.out.println("buffer.reset()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=1 lim=32 cap=32]

    }
    /**
     * buffer其他重要的方法2
     * position():获取当前pos位置
     * position（int pos）:设置当前pos位置
     * limit():获取当前lim位置
     * limit(int lim):设置当前lim位置
     * rewind():重置pos=0，limit不变
     */
    public static void test7(){
        ByteBuffer buffer =ByteBuffer.allocate(32);
        System.out.println("申请堆内存后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put((byte) 'a');
        System.out.println("写入1个数据后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=1 lim=32 cap=32]
        buffer.clear();//重置所有游标
        System.out.println("buffer.clear()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.position(5);//更改pos位置
        buffer.limit(10);//更改limit位置
        System.out.println("buffer.position(5),buffer.limit(10)后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=5 lim=10 cap=32]
        buffer.rewind();//重置pos=0，limit不变
        System.out.println("buffer.rewind()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=10 cap=32]
    }

    /**
     * buffer其他重要的方法3
     * flip():读写模式切换
     * compact():方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。
     */
    public static void test8(){
        ByteBuffer buffer =ByteBuffer.allocate(32);
        System.out.println("申请堆内存后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put("abcd".getBytes());
        System.out.println("buffer.put(\"abcd\".getBytes())后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=4 lim=32 cap=32]
        buffer.flip();
        System.out.println("buffer.flip()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=4 cap=32]
        buffer.get();
        System.out.println("buffer.get()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=1 lim=32 cap=32]
        System.out.println(new String(buffer.array()));
        buffer.compact();//compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。
        System.out.println("buffer.compact()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=3 lim=32 cap=32]
    }

    /**
     * buffer其他重要的方法4
     * remaining():return limit - position;返回limit和position之间相对位置差
     * hasRemaining():return position < limit返回是否还有未读内容
     */
    public static void test9(){
        ByteBuffer buffer =ByteBuffer.allocate(32);
        System.out.println("申请堆内存后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=32 cap=32]
        buffer.put("abcd".getBytes());
        System.out.println("buffer.put(\"abcd\".getBytes())后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=4 lim=32 cap=32]
        buffer.flip();
        System.out.println("buffer.flip()后的游标情况："+buffer);//java.nio.HeapByteBuffer[pos=0 lim=4 cap=32]
        buffer.get();
        System.out.println("hasRemaining():"+buffer.hasRemaining());//是否还有未读内容
        System.out.println("remaining():"+buffer.remaining());//返回limit和position之间相对位置差
    }
}

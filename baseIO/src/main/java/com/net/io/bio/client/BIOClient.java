package com.net.io.bio.client;

import com.net.tool.ThreadLogUtil;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * 1、创建socket设置服务器IP和端口（创建过程中就包含了连接的步骤）
 * 2、创建一个线程run方法中while循环调用socket读 使得线程阻塞，直到服务端返回数据客户端接收到数据并处理进入下一次循环
 * 3、客户端发送信息到服务器
 *
 * ps：客户端/服务器传递数据都是通过socket的inputStream、outputStream
 * 第二个步骤必须先执行，如果第三个步骤先执行可能前几次数据上传后不能及时的处理返回的数据。
 *
 *  服务端先关闭socket通道：
 * 发送了exit命令到服务端,服务端先关闭了socket，之后reader.readLine()返回值为null，然后关闭客户端相关的io流资源，以及释放客户端的socket资源。
 * 如果服务端调用了socket.close，只是代表socket通信通道关闭了，不代表客户端socket会自动释放资源。
 */
public class BIOClient {
    private Socket socket;
    private PrintWriter pw;
    private SocketThread t;
    private class SocketThread extends Thread {
        private Socket socket;
        private BufferedReader reader;
        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("port:"+socket.getLocalPort());
            try {
                InputStream ios = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(ios));
//                ios.close();//socket io关闭将导致socket通信通道关闭
//                socket.getOutputStream().close();//socket io的输入/输出流的关闭将导致socket通信通道关系，在javaio流中任意的io包装对象的关闭都会让最基础的io也同时关闭。
                //虽然通道关闭了，为了让socket对象能够被回收，应该调用socket.close。
                //总结：socket.close方法和关闭任意socket io的输入/输出流 都可以实现socket通道关闭。但是socket.close方法除了关闭通道还有释放资源对象的意思。
                String res = null;
                while ((res = reader.readLine()) != null) {//io流并不能监听到socket关闭的事件。因此在关闭socket之前要关闭所有的io流，避免io对象的残留
                    ThreadLogUtil.printMsg(res);
                }
                pw.close();//调用close方法的作用：清理资源方便jvm回收对象
                reader.close();
                socket.close();
                ThreadLogUtil.printMsg("当前socket已经关闭！");
                System.out.println(pw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public BIOClient(Socket socket) throws IOException {
        this.socket = socket;
        t= new SocketThread(socket);
        t.start();
        this.pw = new PrintWriter(socket.getOutputStream());
    }

    public void sendMsg(String msg) throws IOException {
//        BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw.println(msg);//pw.println能追加换行符，并被reader.readLine方法判断为一行结束
        pw.flush();
    }

    public static void main(String[] args) throws IOException {
        BIOClient client =new BIOClient(new Socket("127.0.0.1",12121));
        Scanner sc =new Scanner(System.in);
        String command =null;
        while (true){
            command=sc.next();
            client.sendMsg(command);
            if ("exit".equals(command))break;
        }


    }



}

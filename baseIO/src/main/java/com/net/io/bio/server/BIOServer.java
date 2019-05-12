package com.net.io.bio.server;

import com.net.tool.ThreadLogUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 1、创建ServerSocket并绑定端口
 * 2、ServerSocket实例开始接受客户端并创建对应的Socket，把客户端socket包装成runnable 丢给线程池执行
 * 3、线程run方法里while循环调用客户端socket读 使得线程阻塞，直到客户端向服务器发送数据，服务端接收到数据以后，
 * 线程状态变为running，处理数据并返回信息给客户端，此时服务器可以是关闭socket或者是进入下一次循环。
 */
public class BIOServer {
    private final String IP = "127.0.0.1";
    private final int PORT = 12121;
    private Executor executor = Executors.newFixedThreadPool(10);
    private ServerSocket server;

    private class SocketThread extends Thread {
        private Socket socket;

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream ios = socket.getInputStream();
                String ip = socket.getInetAddress().getHostAddress();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ios));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
//                BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String command = null;
                while ((command = reader.readLine()) != null) {//当客户端异常关闭socket时，command==null
                    if ("exit".equals(command)) {
                        break;
                    }
                    ThreadLogUtil.printMsg(ip + "[" + command + "]");
                    pw.println(new Date().toString());//pw.println能追加换行符，并被reader.readLine方法判断为一行结束
//                    bw.write(new Date().toString());
//                    bw.write("\n");
                    pw.flush();
                }
                ThreadLogUtil.printMsg("客户端["+socket.getInetAddress().toString()+":"+socket.getPort()+"]已关闭");
                pw.close();
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 创建一个ServerSocket并绑定地址和端口
     * @throws IOException
     */
    public BIOServer() throws IOException {
        server = new ServerSocket();
        server.bind(new InetSocketAddress(IP, PORT));
    }

    public void start() throws IOException {
        ThreadLogUtil.printMsg("server start");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                //阻塞接收客户端
                Socket client = server.accept();
                ThreadLogUtil.printMsg("client[" + client.getInetAddress()+":"+client.getPort() + "]已连接");
                //将socket打包成 runnable 丢进线程池
                executor.execute(new SocketThread(client));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        server.close();
    }


    public static void main(String[] args) throws IOException {
        BIOServer server = new BIOServer();
        server.start();
    }
}

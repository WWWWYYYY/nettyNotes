package com.net.io.bio.client;

import com.net.tool.ThreadLogUtil;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 *  客户端主动发起关闭socket连接
 *
 *  1、先关闭线程的io阻塞，使得线程run方法结束
 *  2、调用socket相关或者包装的io流对象的close方法
 *  3、关闭socket
 */
public class BIOClient2 {
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
            System.out.println("本地客户端端口:"+socket.getLocalPort());
            ThreadLogUtil.printMsg("服务端[" + socket.getRemoteSocketAddress()+"]已连接");
            try {
                InputStream ios = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(ios));
                String res = null;
                    while (!this.isInterrupted()&&(res = reader.readLine()) != null) {
                    ThreadLogUtil.printMsg(res);
                }
                reader.close();
                ThreadLogUtil.printMsg("当前socket已经关闭！");
            } catch (IOException e) {
                ThreadLogUtil.printMsg("客户端主动发起关闭socket连接，因此抛出 socket closed异常");
                e.printStackTrace();
            }
        }

    }

    public BIOClient2(Socket socket) throws IOException {
        this.socket = socket;
        t= new SocketThread(socket);
        t.start();
        this.pw = new PrintWriter(socket.getOutputStream());
    }

    public void sendMsg(String msg) throws IOException {
        pw.println(msg);//pw.println能追加换行符，并被reader.readLine方法判断为一行结束
        pw.flush();
    }

    public static void main(String[] args) throws IOException {
        BIOClient2 client =new BIOClient2(new Socket("127.0.0.1",12121));
        Scanner sc =new Scanner(System.in);
        String command =null;
        while (true){
            command=sc.next();
            if ("exit".equals(command)){//主动关闭socket
                client.close();
                break;
            }
            client.sendMsg(command);

        }
//        client.pw.close();

    }

    private void close() throws IOException {
//        t.interrupt(); //处于io阻塞的线程再调用中断方法已经没有效果了。唯一的办法就是关闭io流,因此在线程中需要捕获 socket closed异常
        pw.close();//调用close方法的作用：清理资源方便jvm回收对象
        socket.close();
    }


}

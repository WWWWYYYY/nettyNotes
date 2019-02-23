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
 */
public class BIOClient {
    private Socket socket;
    private PrintWriter pw;

    private class SocketThread extends Thread {
        private Socket socket;

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream ios = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ios));
                String res = null;
                while ((res = reader.readLine()) != null) {
                    ThreadLogUtil.printMsg(res);
                }
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BIOClient(Socket socket) throws IOException {
        this.socket = socket;
        new SocketThread(socket).start();
        this.pw = new PrintWriter(socket.getOutputStream());
    }

    public void sendMsg(String msg) throws IOException {
//        BufferedWriter bw =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw.println(msg);//pw.println能追加换行符，并被reader.readLine方法判断为一行结束
        pw.flush();
    }
    public void close() throws IOException {
        pw.close();
        socket.close();
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
        client.close();
    }



}

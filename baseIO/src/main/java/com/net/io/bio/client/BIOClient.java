package com.net.io.bio.client;

import com.net.tool.ThreadLogUtil;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

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

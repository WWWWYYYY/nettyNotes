package com.net.tool;

public class ThreadLogUtil {

    public static void printMsg(String msg){
        System.out.println(Thread.currentThread().getName()+": "+msg);
    }
}

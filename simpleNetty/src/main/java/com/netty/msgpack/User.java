package com.netty.msgpack;

import org.msgpack.annotation.Message;

@Message
public class User {
    private String username;
    private String age;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}

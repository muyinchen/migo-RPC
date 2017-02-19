package com.nia.rpc.example.service;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
public class HelloWorldImpl implements HelloWorld {
    @Override
    public String say(String hello) {
        return "server: "+hello;
    }

    @Override
    public int sum(int a, int b) {
        return a+b;
    }

    @Override
    public int max(Integer a, Integer b) {
        return a <= b ? b : a;
    }
}

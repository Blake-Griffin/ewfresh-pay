package com.ewfresh.pay.worker;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wangziyuan on 2018/5/6.
 */
public class Start {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("classpath:spring/spring-*.xml");
    }
}

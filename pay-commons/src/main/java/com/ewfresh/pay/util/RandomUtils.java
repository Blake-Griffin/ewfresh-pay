package com.ewfresh.pay.util;

import java.util.Random;
import java.util.UUID;

/**/
public class RandomUtils {

	public static String getUUID() {
		String id = UUID.randomUUID().toString();
		
		String UUID = id.replaceAll("-", "");
		
		return UUID;
		
	}
	//获取随机六位数字的方法
	public static Integer getRandomNum(){
		Random random = new Random();
        return random.nextInt(899999)+100000;

	}

	public static String getUUID64() {
		return getUUID()+getUUID();
	}

    public static void main(String[] args) {
        System.out.println(RandomUtils.getRandomNum());
    }
}

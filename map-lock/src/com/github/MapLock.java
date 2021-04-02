package com.github;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/3
 */
public class MapLock {

    public static void main(String[] args){
//        Map<String, String> map = new HashMap<>();
//        Map<String, String> map = new Hashtable<>();
//        Map<String, String> map = Collections.synchronizedMap(new HashMap<>());
        Map<String, String> map = new ConcurrentHashMap<>();

        for (int i = 0; i < 50; i++) {
            new Thread(() -> {
                map.put(Thread.currentThread().getName(), UUID.randomUUID().toString().substring(0, 4));
                System.out.println(map);
            }).start();
        }
    }
}

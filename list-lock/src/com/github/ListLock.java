package com.github;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/2
 */
public class ListLock {

    public static void main(String[] args){
//        List<String> list = new ArrayList<>();
//        List<String> list = new Vector<>();
//        List<String> list = Collections.synchronizedList(new ArrayList<>());
        List<String> list = new CopyOnWriteArrayList<>();

        for (int i = 0; i < 30; i++) {
            new Thread(() -> {
                list.add(UUID.randomUUID().toString().substring(0, 5));
                System.out.println(list);
            }).start();
        }
    }
}

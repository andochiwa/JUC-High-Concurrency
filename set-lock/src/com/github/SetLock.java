package com.github;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/2
 */
public class SetLock {

    public static void main(String[] args){
//        Set<String> hashSet = new HashSet<>();
//        Set<String> hashSet = Collections.synchronizedSet(new HashSet<>());
        Set<String> hashSet = new CopyOnWriteArraySet<>();

        for (int i = 0; i < 70; i++) {
            new Thread(() -> {
                hashSet.add(UUID.randomUUID().toString().substring(0, 4));
                System.out.println(hashSet);
            }).start();
        }
    }
}

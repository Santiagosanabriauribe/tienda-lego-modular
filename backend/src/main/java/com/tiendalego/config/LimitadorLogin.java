package com.tiendalego.config;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class LimitadorLogin {

    private final Map<String, Deque<Long>> intentos = new ConcurrentHashMap<>();

    public boolean permitido(String ip) {
        long ahora = System.currentTimeMillis();
        Deque<Long> q = intentos.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        while (!q.isEmpty() && ahora - q.peekFirst() > 15 * 60 * 1000) q.pollFirst();
        if (q.size() >= 5) return false;
        q.addLast(ahora);
        return true;
    }
}

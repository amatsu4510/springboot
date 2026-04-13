package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class BatchJob {

    @Scheduled(fixedRate = 5000)
    public void run() {
        System.out.println("＠Scheduledで5秒ごとに実行");
    }
}
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @SpringBootApplication: コンポーネントスキャン・自動設定・設定を有効化するアノテーション
@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        // 組み込みTomcatサーバーを起動し、Springコンテキストを初期化する
        SpringApplication.run(DemoApplication.class, args);
    }
}

package com.example.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController: このクラスをREST APIコントローラーとしてマークする
// @CrossOrigin: 異なるオリジン（フロントエンドコンテナ）からのリクエストを許可する
@CrossOrigin(origins = "*")
@RestController
public class HelloController {

    // HTTP GET /hello をこのメソッドにマッピングする
    @GetMapping("/hello")
    public String hello() {
        // プレーンテキストのレスポンスを返す
        return "Hello Spring Boot";
    }
}

package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RequestMapping("/users"): このコントローラーの全エンドポイントに /users をプレフィックスする
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // GET /users → 全ユーザーを取得する
    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    // GET /users/{id} → ID を指定して 1 件取得する
    // ResponseEntity: レスポンスのステータスコードも制御できるラッパー
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)                    // 見つかれば 200 OK
                .orElse(ResponseEntity.notFound().build()); // なければ 404 Not Found
    }

    // POST /users → 新規ユーザーを作成する
    // @RequestBody: リクエストの JSON ボディを User オブジェクトに変換する
    @PostMapping
    public User create(@RequestBody User user) {
        return userService.save(user);
    }

    // DELETE /users/{id} → 指定した ID のユーザーを削除する
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}

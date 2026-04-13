package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<エンティティ型, 主キーの型> を継承するだけで
// findAll / findById / save / deleteById などの CRUD メソッドが自動生成される
public interface UserRepository extends JpaRepository<User, Long> {
    // 基本的な CRUD は JpaRepository が提供するため、ここには何も書かなくてよい
    // 独自の検索が必要な場合はメソッド名規則で追加できる
    // 例: List<User> findByName(String name);
}

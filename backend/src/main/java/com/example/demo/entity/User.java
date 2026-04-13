package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// @Entity: このクラスが DB のテーブルと対応することを宣言する
// @Table: 対応するテーブル名を指定する（省略すると클래스名がテーブル名になる）
@Entity
@Table(name = "users")
public class User {

    // @Id: このフィールドが主キーであることを示す
    // @GeneratedValue: 主キーの値を DB に自動採番させる（AUTO_INCREMENT 相当）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    // JPA はデフォルトコンストラクタが必須
    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // ─── Getter / Setter ────────────────────────────────────────────────────
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

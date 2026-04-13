package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Service: このクラスがビジネスロジック層であることを示す Spring コンポーネント
// @Transactional: クラス全体のメソッドをトランザクション管理下に置く
@Service
@Transactional
public class UserService {

    // @Autowired: Spring が UserRepository の実装を自動的に注入する
    @Autowired
    private UserRepository userRepository;

    // 全ユーザーを取得する
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ID を指定して 1 件取得する（存在しない場合は空の Optional を返す）
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // ユーザーを新規作成 / 更新する
    // save() は id が null なら INSERT、id があれば UPDATE を実行する
    public User save(User user) {
        return userRepository.save(user);
    }

    // ID を指定してユーザーを削除する
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}

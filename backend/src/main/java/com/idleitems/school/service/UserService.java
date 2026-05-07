package com.idleitems.school.service;

import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public void enrichItemWithSellerInfo(Item item, int sellerItemCount) {
        if (item == null) {
            return;
        }

        if (item.getUserId() == null) {
            return;
        }

        User user = userRepository.findById(item.getUserId()).orElse(null);
        if (user != null) {
            item.setSellerNickname(
                user.getNickname() != null && !user.getNickname().isEmpty()
                    ? user.getNickname()
                    : user.getUsername()
            );
            item.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
            item.setSellerRating(5.0);
            item.setSellerItemsCount(sellerItemCount);
        }
    }
}
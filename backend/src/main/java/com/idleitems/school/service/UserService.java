package com.idleitems.school.service;

import com.idleitems.school.dto.UserStatsDTO;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
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

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    public User createUser(String username, String email, String password, String phone, 
                          User.Role role, User.UserStatus status, String nickname, String studentId) {
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        if (phone != null && existsByPhone(phone)) {
            throw new IllegalArgumentException("手机号已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role != null ? role : User.Role.STUDENT);
        user.setStatus(status != null ? status : User.UserStatus.ACTIVE);
        user.setNickname(nickname != null ? nickname : username);
        user.setStudentId(studentId);
        user.setVerified(false);
        user.setCreditScore(100);
        user.setTotalTransactions(0);
        user.setTotalSales(0);
        user.setTotalPurchases(0);
        user.setLoginCount(0);
        user.setIsDeleted(false);

        return userRepository.save(user);
    }

    public User updateUserAdmin(Long userId, String email, String phone, User.Role role, 
                               User.UserStatus status, String nickname, String studentId,
                               Integer gender, String bio, String schoolName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (email != null && !email.equals(user.getEmail())) {
            if (existsByEmail(email)) {
                throw new IllegalArgumentException("邮箱已存在");
            }
            user.setEmail(email);
        }

        if (phone != null && !phone.equals(user.getPhone())) {
            if (existsByPhone(phone)) {
                throw new IllegalArgumentException("手机号已存在");
            }
            user.setPhone(phone);
        }

        if (role != null) {
            user.setRole(role);
        }
        if (status != null) {
            user.setStatus(status);
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (studentId != null) {
            user.setStudentId(studentId);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (schoolName != null) {
            user.setSchoolName(schoolName);
        }

        return userRepository.save(user);
    }

    public List<User> getUsersForExport(String keyword, User.Role role, User.UserStatus status) {
        if (role != null && status != null) {
            return userRepository.findByRoleAndStatus(role, status, Pageable.unpaged()).getContent();
        } else if (role != null) {
            return userRepository.findByRole(role, Pageable.unpaged()).getContent();
        } else if (status != null) {
            return userRepository.findByStatus(status, Pageable.unpaged()).getContent();
        } else {
            return userRepository.findAll(Pageable.unpaged()).getContent();
        }
    }

    public void deleteUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
            if (user.getRole() == User.Role.ADMIN) {
                throw new IllegalArgumentException("不能删除管理员用户");
            }
            userRepository.deleteById(userId);
        }
    }

    public User updateUser(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (updates.containsKey("nickname")) {
            user.setNickname((String) updates.get("nickname"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("avatar")) {
            user.setAvatar((String) updates.get("avatar"));
        }
        if (updates.containsKey("studentId")) {
            user.setStudentId((String) updates.get("studentId"));
        }
        if (updates.containsKey("gender")) {
            user.setGender((Integer) updates.get("gender"));
        }
        if (updates.containsKey("birthday")) {
            String birthdayStr = (String) updates.get("birthday");
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                user.setBirthday(java.time.LocalDate.parse(birthdayStr));
            } else {
                user.setBirthday(null);
            }
        }
        if (updates.containsKey("bio")) {
            user.setBio((String) updates.get("bio"));
        }
        if (updates.containsKey("schoolName")) {
            user.setSchoolName((String) updates.get("schoolName"));
        }

        return userRepository.save(user);
    }

    public UserStatsDTO getUserStats(Long userId) {
        User user = getUserById(userId);
        long totalItems = itemRepository.countByUserId(userId);
        long soldItems = orderRepository.countBySellerIdAndStatus(userId, com.idleitems.school.entity.Order.OrderStatus.COMPLETED);
        long completedDeals = user.getTotalTransactions() != null ? user.getTotalTransactions() : 0;
        int creditScore = user.getCreditScore() != null ? user.getCreditScore() : 100;
        double rating = Optional.ofNullable(reviewRepository.getAverageRatingByUserId(userId))
                .orElse(BigDecimal.valueOf(creditScore)).doubleValue();

        return UserStatsDTO.builder()
                .totalItems(totalItems)
                .soldItems(soldItems)
                .completedDeals(completedDeals)
                .rating(rating)
                .build();
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
            
            // 从评价表计算卖家真实评分
            BigDecimal averageRating = reviewRepository.getAverageRatingByUserId(item.getUserId());
            item.setSellerRating(averageRating != null ? averageRating.doubleValue() : 0.0);
            
            item.setSellerItemsCount(sellerItemCount);
        }
    }
}
package com.idleitems.school.module.user.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.user.dto.SellerProfileDTO;
import com.idleitems.school.module.user.dto.UpdateProfileRequest;
import com.idleitems.school.module.user.dto.UserStatsDTO;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.repository.ReviewRepository;
import com.idleitems.school.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== 查询方法 ==========

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
    }

    public User findById(Long id) {
        return getUserById(id);
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

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // ========== 写入方法 ==========

    public User save(User user) {
        return userRepository.save(user);
    }

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            if (!request.getPhone().equals(user.getPhone()) && existsByPhone(request.getPhone())) {
                throw new BusinessException(ErrorCode.CONFLICT, "手机号已被使用");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getStudentId() != null) {
            user.setStudentId(request.getStudentId());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null && !request.getBirthday().isEmpty()) {
            user.setBirthday(java.time.LocalDate.parse(request.getBirthday()));
        } else if (request.getBirthday() != null) {
            user.setBirthday(null);
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getSchoolName() != null) {
            user.setSchoolName(request.getSchoolName());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }
        if (request.getGrade() != null) {
            user.setGrade(request.getGrade());
        }

        return userRepository.save(user);
    }

    // ========== 管理员方法 ==========

    public User createUser(String username, String email, String password, String phone,
                          User.Role role, User.UserStatus status, String nickname, String studentId) {
        if (existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已存在");
        }
        if (phone != null && existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已存在");
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
        user.setLoginCount(0);
        user.setIsDeleted(false);

        return userRepository.save(user);
    }

    public User updateUserAdmin(Long userId, String email, String phone, User.Role role,
                               User.UserStatus status, String nickname, String studentId,
                               Integer gender, String bio, String schoolName,
                               String department, String major, String grade) {
        User user = getUserById(userId);

        if (email != null && !email.equals(user.getEmail())) {
            if (existsByEmail(email)) {
                throw new BusinessException(ErrorCode.CONFLICT, "邮箱已存在");
            }
            user.setEmail(email);
        }

        if (phone != null && !phone.equals(user.getPhone())) {
            if (existsByPhone(phone)) {
                throw new BusinessException(ErrorCode.CONFLICT, "手机号已存在");
            }
            user.setPhone(phone);
        }

        if (role != null) user.setRole(role);
        if (status != null) user.setStatus(status);
        if (nickname != null) user.setNickname(nickname);
        if (studentId != null) user.setStudentId(studentId);
        if (gender != null) user.setGender(gender);
        if (bio != null) user.setBio(bio);
        if (schoolName != null) user.setSchoolName(schoolName);
        if (department != null) user.setDepartment(department);
        if (major != null) user.setMajor(major);
        if (grade != null) user.setGrade(grade);

        return userRepository.save(user);
    }

    public void deleteUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在: " + userId));
            if (user.getRole() == User.Role.ADMIN) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "不能删除管理员用户");
            }
            userRepository.deleteById(userId);
        }
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

    // ========== 卖家公开信息 ==========

    public SellerProfileDTO getSellerProfile(Long userId) {
        User user = getUserById(userId);
        long totalItems = itemRepository.countByUserIdAndStatus(userId, com.idleitems.school.module.item.entity.Item.ItemStatus.ON_SALE);
        long soldItems = Optional.ofNullable(orderRepository.countBySellerIdAndStatus(userId, com.idleitems.school.module.order.entity.Order.OrderStatus.COMPLETED)).orElse(0L);
        long completedDeals = Optional.ofNullable(orderRepository.countByBuyerIdAndStatus(userId, com.idleitems.school.module.order.entity.Order.OrderStatus.COMPLETED)).orElse(0L)
                + soldItems;
        double rating = Optional.ofNullable(reviewRepository.getAverageRatingByUserId(userId))
                .orElse(BigDecimal.ZERO).doubleValue();
        long reviewCount = Optional.ofNullable(reviewRepository.countByReviewedUserId(userId)).orElse(0L);

        return SellerProfileDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname() != null ? user.getNickname() : user.getUsername())
                .avatar(user.getAvatar())
                .schoolName(user.getSchoolName())
                .department(user.getDepartment())
                .major(user.getMajor())
                .grade(user.getGrade())
                .bio(user.getBio())
                .verified(user.getVerified() != null ? user.getVerified() : false)
                .creditScore(user.getCreditScore() != null ? user.getCreditScore() : 100)
                .memberSince(user.getCreatedAt())
                .totalItems(totalItems)
                .soldItems(soldItems)
                .completedDeals(completedDeals)
                .rating(rating)
                .reviewCount(reviewCount)
                .build();
    }

    // ========== 统计方法 ==========

    public UserStatsDTO getUserStats(Long userId) {
        User user = getUserById(userId);
        long totalItems = itemRepository.countByUserId(userId);
        long soldItems = orderRepository.countBySellerIdAndStatus(userId, com.idleitems.school.module.order.entity.Order.OrderStatus.COMPLETED);
        long completedDeals = orderRepository.countByBuyerIdAndStatus(userId, com.idleitems.school.module.order.entity.Order.OrderStatus.COMPLETED)
                + orderRepository.countBySellerIdAndStatus(userId, com.idleitems.school.module.order.entity.Order.OrderStatus.COMPLETED);
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

    // ========== 关联方法 ==========

    public void enrichItemWithSellerInfo(Item item, int sellerItemCount) {
        if (item == null || item.getUserId() == null) {
            return;
        }

        userRepository.findById(item.getUserId()).ifPresent(user -> {
            item.setSellerNickname(
                user.getNickname() != null && !user.getNickname().isEmpty()
                    ? user.getNickname()
                    : user.getUsername()
            );
            item.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);

            BigDecimal averageRating = reviewRepository.getAverageRatingByUserId(item.getUserId());
            item.setSellerRating(averageRating != null ? averageRating.doubleValue() : 0.0);

            item.setSellerItemsCount(sellerItemCount);
        });
    }
}
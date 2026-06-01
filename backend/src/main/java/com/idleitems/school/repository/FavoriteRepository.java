package com.idleitems.school.repository;

import com.idleitems.school.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    Optional<Favorite> findByUserIdAndItemId(Long userId, Long itemId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.userId = :userId AND f.itemId = :itemId")
    int deleteByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

    long countByUserId(Long userId);

    /**
     * 批量查询用户的收藏记录
     */
    List<Favorite> findByUserIdAndItemIdIn(Long userId, List<Long> itemIds);

    /**
     * 统计物品被收藏的次数
     */
    long countByItemId(Long itemId);
}

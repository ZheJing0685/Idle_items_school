package com.idleitems.school.module.system.repository;

import com.idleitems.school.module.system.entity.DictItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictItemRepository extends JpaRepository<DictItem, Long> {

    List<DictItem> findByTypeCodeAndStatusTrue(String typeCode);

    List<DictItem> findByTypeCodeOrderBySortOrderAsc(String typeCode);

    Optional<DictItem> findByTypeCodeAndItemValue(String typeCode, String itemValue);

    @Query("SELECT d FROM DictItem d WHERE d.typeCode = :typeCode AND d.status = true ORDER BY d.sortOrder ASC")
    List<DictItem> findActiveItemsByTypeCode(@Param("typeCode") String typeCode);

    @Query("SELECT d.itemLabel FROM DictItem d WHERE d.typeCode = :typeCode AND d.itemValue = :itemValue AND d.status = true")
    String findItemLabelByTypeCodeAndValue(@Param("typeCode") String typeCode, @Param("itemValue") String itemValue);

    boolean existsByTypeCodeAndItemValue(String typeCode, String itemValue);

    long countByTypeCode(String typeCode);
}
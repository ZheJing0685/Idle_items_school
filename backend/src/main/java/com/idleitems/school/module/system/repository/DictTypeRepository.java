package com.idleitems.school.module.system.repository;

import com.idleitems.school.module.system.entity.DictType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictTypeRepository extends JpaRepository<DictType, Long> {

    Optional<DictType> findByTypeCode(String typeCode);

    List<DictType> findByStatusTrue();

    @Query("SELECT d FROM DictType d WHERE d.isSystem = true")
    List<DictType> findSystemDictTypes();

    boolean existsByTypeCode(String typeCode);
}
package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.ZomatoSalt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ZomatoSaltRepository extends JpaRepository<ZomatoSalt, Long> {

    @Query("SELECT zs from ZomatoSalt zs where zs.createdAt between :startDate and :endDate and zs.saltDiscount<>0 and zs.freebieItem is null")
    List<ZomatoSalt> getAllWhereFreebieItemNotSet(LocalDateTime startDate, LocalDateTime endDate);
}

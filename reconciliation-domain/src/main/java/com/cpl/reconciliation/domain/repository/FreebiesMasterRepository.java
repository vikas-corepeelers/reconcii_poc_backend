package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.FreebiesMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FreebiesMasterRepository extends JpaRepository<FreebiesMasterEntity, Long> {

    @Query("SELECT f FROM FreebiesMasterEntity f where f.day=:day and :orderDate between f.startDate and f.endDate and f.tenderName=:tender")
    List<FreebiesMasterEntity> getFreebieList(DayOfWeek day, LocalDate orderDate, String tender);
}

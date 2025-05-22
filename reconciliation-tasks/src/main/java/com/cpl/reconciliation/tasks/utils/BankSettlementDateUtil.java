//package com.cpl.reconciliation.tasks.utils;
//
//import com.cpl.reconciliation.domain.dao.BankHolidaysDao;
//import com.cpl.reconciliation.domain.entity.BankHolidays;
//import com.cpl.reconciliation.core.enums.Bank;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Service
//@Slf4j
//@Data
//public class BankSettlementDateUtil {
//    private final BankHolidaysDao bankHolidaysDao;
//    private Set<LocalDate> set = new HashSet<>();
//
//    @PostConstruct
//    private void postConstruct() {
//        List<BankHolidays> holidaysList = bankHolidaysDao.getAll();
//        set.addAll(holidaysList.stream().map(date -> date.getDate().toLocalDate()).toList());
//    }
//
//    public LocalDateTime getExpectedSettlementDate(Bank bank, LocalDateTime settlementDate) {
//        LocalDateTime expectedDate = getExpectedDate(bank, settlementDate);
//        while (isHoliday(expectedDate) || isWeekend(expectedDate)) {
//            expectedDate = expectedDate.plusDays(1);
//        }
//        return expectedDate;
//    }
//
//    private boolean isWeekend(LocalDateTime expectedDate) {
//        return expectedDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)
//                || expectedDate.getDayOfWeek().equals(DayOfWeek.SUNDAY);
//    }
//
//    private LocalDateTime getExpectedDate(Bank bank, LocalDateTime settlementDate) {
//        if (bank.equals(Bank.HDFC)) {
//            return settlementDate;
//        }
//        return settlementDate.plusDays(1);
//    }
//
//    public boolean isHoliday(LocalDateTime date) {
//        return set.contains(date.toLocalDate());
//    }
//}

package com.cpl.reconciliation.web.service.impl;

import com.cpl.reconciliation.core.request.SaveRecoLogicRequest;
import com.cpl.reconciliation.core.request.UpdateRecoLogicRequest;
import com.cpl.reconciliation.domain.entity.RecoLogicsEntity;
import com.cpl.reconciliation.domain.repository.RecoLogicRepository;
import com.cpl.reconciliation.web.service.RecoLogicService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @author Abhishek N
 */
@Data
@Slf4j
@Service
public class RecoLogicServiceImpl implements RecoLogicService {

    @Autowired
    RecoLogicRepository recoRepository;

    @Override
    public String saveRecoLogic(SaveRecoLogicRequest recoBean) {
        String tendersCommaSeparated;
        if (recoBean.getTenders().size() == 1) {
            tendersCommaSeparated = recoBean.getTenders().get(0);
        } else {
            tendersCommaSeparated = recoBean.getTenders().stream().sorted().collect(Collectors.joining(","));
        }
        String effectiveFrom = recoBean.getEffectiveFrom();
        String effectiveTo = recoBean.getEffectiveTo().trim().isEmpty() ? "2099-12-31" : recoBean.getEffectiveTo();
        log.info("effectiveFrom {}, effectiveTo {}", effectiveFrom, effectiveTo);
        List<RecoLogicsEntity> logics = recoRepository.findByTenderAndDateRange(tendersCommaSeparated, effectiveFrom, effectiveTo);
        log.info("logics size {}", logics.size());
        if (logics.isEmpty()) {
            RecoLogicsEntity recoEntity = new RecoLogicsEntity();
            recoEntity.setTender(tendersCommaSeparated);
            recoEntity.setCreatedBy(recoBean.getCreatedBy() == null ? "" : recoBean.getCreatedBy());
            recoEntity.setEffectiveFrom(effectiveFrom);
            recoEntity.setEffectiveTo(effectiveTo);
            recoEntity.setEffectiveType(recoBean.getEffectiveType() == null ? "" : recoBean.getEffectiveType());
            recoEntity.setRecologic(recoBean.getRecoData().toPrettyString());
            recoEntity.setAdded(LocalDateTime.now());
            recoEntity.setUpdated(LocalDateTime.now());
            recoEntity.setStatus("SAVED");
            recoEntity.setRemarks("SAVED");
            RecoLogicsEntity entity = recoRepository.save(recoEntity);
            return String.valueOf(entity.getId());
        } else {
            return "RecoData is already available for mentioned DateRange";
        }
    }

    @Override
    public String updateRecoLogic(UpdateRecoLogicRequest recoBean) {
        String tendersCommaSeparated;
        if (recoBean.getTenders().size() == 1) {
            tendersCommaSeparated = recoBean.getTenders().get(0);
        } else {
            tendersCommaSeparated = recoBean.getTenders().stream().sorted().collect(Collectors.joining(","));
        }
        String effectiveFrom = recoBean.getEffectiveFrom();
        String effectiveTo = recoBean.getEffectiveTo().trim().isEmpty() ? "2099-12-31" : recoBean.getEffectiveTo();
        List<RecoLogicsEntity> logics = recoRepository.findByTenderAndDateRangeUpdate(recoBean.getId(), tendersCommaSeparated, effectiveFrom, effectiveTo);
        if (logics.isEmpty()) {
            RecoLogicsEntity recoEntity = new RecoLogicsEntity();
            recoEntity.setId(recoBean.getId());
            recoEntity.setTender(tendersCommaSeparated);
            recoEntity.setCreatedBy(recoBean.getCreatedBy() == null ? "" : recoBean.getCreatedBy());
            recoEntity.setEffectiveFrom(effectiveFrom);
            recoEntity.setEffectiveTo(effectiveTo);
            recoEntity.setEffectiveType(recoBean.getEffectiveType() == null ? "" : recoBean.getEffectiveType());
            recoEntity.setRecologic(recoBean.getRecoData().toPrettyString());
            recoEntity.setUpdated(LocalDateTime.now());
            recoEntity.setStatus("UPDATED");
            recoEntity.setRemarks("UPDATED");
            recoRepository.save(recoEntity);
            return "RecoData is updated successfully";
        } else {
            return "RecoData is already available for mentioned DateRange. It can't be updated.";
        }
    }

    @Override
    public List<RecoLogicsEntity> findAllLogics() {
        List<RecoLogicsEntity> logics = recoRepository.findAll();
        return logics;
    }

    @Override
    public List<RecoLogicsEntity> findLogicsByTender(List<String> tenders) {
        String tendersCommaSeparated;
        if (tenders.size() == 1) {
            tendersCommaSeparated = tenders.get(0);
        } else {
            tendersCommaSeparated = tenders.stream().sorted().collect(Collectors.joining(","));
        }
        List<RecoLogicsEntity> logics = recoRepository.findByTender(tendersCommaSeparated);
        return logics;
    }

    @Override
    public String findOldestEffectiveDate() {
        String effectiveFrom = recoRepository.findOldestEffectiveDate();
        return effectiveFrom;
    }
}

package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.core.request.SaveRecoLogicRequest;
import com.cpl.reconciliation.core.request.UpdateRecoLogicRequest;
import com.cpl.reconciliation.domain.entity.RecoLogicsEntity;
import java.util.List;

/*
 * @author Abhishek N
 */
public interface RecoLogicService {

    String saveRecoLogic(SaveRecoLogicRequest recoData);

    String updateRecoLogic(UpdateRecoLogicRequest recoBean);

    List<RecoLogicsEntity> findAllLogics();

    List<RecoLogicsEntity> findLogicsByTender(List<String> tender);

    String findOldestEffectiveDate();
}

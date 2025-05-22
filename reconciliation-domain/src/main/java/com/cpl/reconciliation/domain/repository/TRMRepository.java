package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.core.response.instore.MissingTID;
import com.cpl.reconciliation.domain.entity.TRMEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TRMRepository extends JpaRepository<TRMEntity, Long> {

    @Query(value = "SELECT new com.cpl.reconciliation.core.response.instore.MissingTID(t.paymentType as tender,t.acquirerBank as bank,count(distinct(t.tid)) as value) FROM TRMEntity t where t.storeId is null " +
            "group by t.paymentType ,t.acquirerBank")
    List<MissingTID> getMissingTIDCountTenderAndBank();

    @Query(value = "SELECT new com.cpl.reconciliation.core.response.instore.MissingTID( t.paymentType as tender,t.acquirerBank as bank,count(distinct(t.tid)) as value) FROM TRMEntity t " +
            "group by t.paymentType ,t.acquirerBank")
    List<MissingTID> getTIDCountTenderAndBank();

    @Query(value = "SELECT distinct(tid) FROM TRMEntity t where t.storeId is null and t.paymentType=:tender and t.acquirerBank=:bank ")
    List<String> getMissingTIDTenderAndBank(PaymentType tender, String bank);

    @Transactional
    @Modifying
    @Query(value = "UPDATE trm\n" +
            "JOIN store_tid_mapping ON trm.tid = store_tid_mapping.tid\n" +
            "SET trm.store_id = store_tid_mapping.store_code where trm.store_id is null;",nativeQuery = true)
    void updateStoreMapping();
}

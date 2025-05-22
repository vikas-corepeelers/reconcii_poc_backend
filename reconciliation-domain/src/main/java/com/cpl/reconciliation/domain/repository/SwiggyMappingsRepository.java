package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.SwiggyMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SwiggyMappingsRepository extends JpaRepository<SwiggyMappings, Long> {

    @Query(value = "SELECT s.storeCode FROM SwiggyMappings s WHERE s.swiggyStoreCode=:swiggyStoreCode")
    String findStoreCodeBySwiggyStoreCode(@Param("swiggyStoreCode") String swiggyStoreCode);

    SwiggyMappings findBySwiggyStoreCode(String swiggyStoreCode);

    @Transactional
    @Modifying
    @Query(value = "UPDATE swiggy\n" +
            "JOIN swiggy_mappings ON swiggy.restaurant_id = swiggy_mappings.swiggy_store_code\n" +
            "SET \n" +
            "    swiggy.store_code = swiggy_mappings.store_code,\n" +
            "    swiggy.actual_packaging_charge = CASE\n" +
            "                                        WHEN swiggy.item_total != 0 THEN swiggy_mappings.packaging_charge\n" +
            "                                        ELSE 0\n" +
            "                                    END\n" +
            "WHERE \n" +
            "    swiggy.store_code IS NULL;", nativeQuery = true)
    void updateMappings();

}

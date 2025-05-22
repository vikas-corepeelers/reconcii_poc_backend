package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.ZomatoMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ZomatoMappingsRepository extends JpaRepository<ZomatoMappings, Long> {

    @Query(value = "SELECT z.storeCode FROM ZomatoMappings z WHERE z.zomatoStoreCode=:zomatoStoreCode")
    String findStoreCodeByZomatoStoreCode(@Param("zomatoStoreCode") String zomatoStoreCode);

    ZomatoMappings findByZomatoStoreCode(String zomatoStoreCode);

    @Transactional
    @Modifying
    @Query(value = "UPDATE zomato\n" +
            "JOIN zomato_mappings ON zomato.res_id = zomato_mappings.zomato_store_code\n" +
            "SET \n" +
            "    zomato.store_code = zomato_mappings.store_code,\n" +
            "    zomato.actual_packaging_charge = CASE\n" +
            "                                        WHEN zomato.bill_subtotal != 0 THEN zomato_mappings.packaging_charge\n" +
            "                                        ELSE zomato.actual_packaging_charge\n" +
            "                                    END\n" +
            "WHERE \n" +
            "    zomato.store_code IS NULL \n" +
            "    OR zomato.actual_packaging_charge IS NULL;\n", nativeQuery = true)
    void updateMappings();

    @Transactional
    @Modifying
    @Query(value = "UPDATE zomato_salt s\n" +
            "JOIN zomato_mappings z ON s.res_id = z.zomato_store_code\n" +
            "SET\n" +
            "    s.store_code = z.store_code\n" +
            "WHERE\n" +
            "    s.store_code IS NULL;", nativeQuery = true)
    void updateSaltMappings();

    @Transactional
    @Modifying
    @Query(value = "UPDATE zomato_promo s\n" +
            "JOIN zomato_mappings z ON s.res_id = z.zomato_store_code\n" +
            "SET\n" +
            "    s.store_code = z.store_code\n" +
            "WHERE\n" +
            "    s.store_code IS NULL;", nativeQuery = true)
    void updatePromoMappings();

}

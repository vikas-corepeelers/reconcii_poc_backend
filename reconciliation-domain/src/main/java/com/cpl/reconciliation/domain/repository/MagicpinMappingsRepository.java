//package com.cpl.reconciliation.domain.repository;
//
//import com.cpl.reconciliation.domain.entity.MagicpinMappings;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//@Repository
//public interface MagicpinMappingsRepository extends JpaRepository<MagicpinMappings, Long> {
//
//    @Query(value = "SELECT m.storeCode FROM MagicpinMappings m WHERE m.mid=:mid")
//    String findStoreCodeByMid(@Param("mid") String mid);
//
//    MagicpinMappings findByMid(String mid);
//
//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE magicpin\n" +
//            "JOIN magicpin_mappings ON magicpin.mid = magicpin_mappings.mid\n" +
//            "SET \n" +
//            "    magicpin.store_code = magicpin_mappings.store_code,\n" +
//            "    magicpin.actual_packaging_charge = CASE\n" +
//            "                                        WHEN magicpin.item_amount != 0 THEN magicpin_mappings.packaging_charge\n" +
//            "                                        ELSE 0\n" +
//            "                                    END\n" +
//            "WHERE \n" +
//            "    magicpin.store_code IS NULL;", nativeQuery = true)
//    void updateMappings();
//}

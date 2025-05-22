package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.domain.entity.DataEntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DataEntryLogRepository extends JpaRepository<DataEntryLog, Long> {

    @Query(value = "SELECT * FROM data_entry_log WHERE data_source = :dataSource and (business_date between :startDate and :endDate OR end_date between :startDate and :endDate) and created_by<>'SYSTEM_GENERATED'", nativeQuery = true)
    List<DataEntryLog> findDataByDateAndDataSource(@Param("dataSource") String dataSource, @Param("startDate") LocalDate currentDate,@Param("endDate") LocalDate endDate);

    //    @Query("SELECT d FROM DataEntryLog d WHERE (d.dataSource, d.businessDate) IN " +
//            "(SELECT d2.dataSource, MAX(d2.businessDate) FROM DataEntryLog d2 WHERE d2.dataSource IN :dataSources GROUP BY d2.dataSource)")
    @Query(value = "WITH RankedData AS (\n" +
            "    SELECT\n" +
            "        d.*,\n" +
            "        ROW_NUMBER() OVER (PARTITION BY d.data_source ORDER BY d.business_date DESC) AS row_num\n" +
            "    FROM data_entry_log d\n" +
            "    WHERE d.data_source IN :dataSources\n" +
            ")\n" +
            "SELECT *\n" +
            "FROM RankedData\n" +
            "WHERE row_num = 1;", nativeQuery = true)
    List<DataEntryLog> findLatestRecordsByDataSources(@Param("dataSources") List<String> dataSources);

    @Query(value="select * from data_entry_log d where d.data_source=:dataSource order by d.id DESC limit 1",nativeQuery = true)
    DataEntryLog findByDatasource(@Param("dataSource") String dataSource);
}

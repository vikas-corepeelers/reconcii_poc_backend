package com.cpl.reconciliation.domain;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableConfigurationProperties
@EntityScan("com.cpl.reconciliation.domain.entity")
@EnableJpaRepositories(basePackages = "com.cpl.reconciliation.domain.repository")
public class MySQLConfig {

    @Primary
    @Bean("dataSourceProperties")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean("dataSource")
    public DataSource dataSource() {
        DataSourceProperties properties = dataSourceProperties();
        return createDataSource(properties);
    }

    private DataSource createDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean("entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("dataSource") DataSource dataSource,
            JpaProperties jpaProperties,
            HibernateProperties hibernateProperties,
            EntityManagerFactoryBuilder entityManagerFactoryBuilder) {
     
        Map<String, Object> jpaProps = hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
        // Set ddlAuto to none to prevent Hibernate from automatically updating the schema
//        jpaProps.put("hibernate.hbm2ddl.auto", "none");

        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages("com.cpl.reconciliation.domain.entity")
                .persistenceUnit("entityManagerFactory")
                .properties(jpaProps)
                .build();
    }

    @Primary
    @Bean("jpaVendorAdapter")
    public HibernateJpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Primary
    @Bean("transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

package com.yh.qa.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="entityManagerFactoryAutomationtest",
        transactionManagerRef="transactionManagerAutomationtest",
        basePackages= { "com.yh.qa.repository" }) //设置Repository所在位置
public class RepositoryQa3Config {

    @Autowired
    @Qualifier("automationtestDataSource")
    private DataSource automationtestDataSource;

    @Primary
    @Bean(name = "entityManagerAutomationtest")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryAutomationtest(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactoryAutomationtest")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryAutomationtest (EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(automationtestDataSource)
                .properties(getVendorProperties(automationtestDataSource))
                .packages("com.yh.qa.entity") //设置实体类所在位置
                .persistenceUnit("automationtestPersistenceUnit")
                .build();
    }

    @Autowired
    private JpaProperties jpaProperties;

    private Map<String, String> getVendorProperties(DataSource dataSource) {
        return jpaProperties.getHibernateProperties(dataSource);
    }

    @Primary
    @Bean(name = "transactionManagerAutomationtest")
    public PlatformTransactionManager transactionManagerPrimary(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryAutomationtest(builder).getObject());
    }

}

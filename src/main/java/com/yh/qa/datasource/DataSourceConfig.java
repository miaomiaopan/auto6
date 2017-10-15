package com.yh.qa.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author 许崇英
 * @since 2017/9/2
 */
@Configuration
public class DataSourceConfig {

    @Bean(name = "accountCenterDataSource")
    @Qualifier("accountCenterDataSource")
    @ConfigurationProperties(prefix="spring.datasource.account_center")
    public DataSource accountCenterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.ACCOUNT_CENTER)
    public JdbcTemplate accountCenterJdbcTemplate(@Qualifier("accountCenterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "afterSalesDBDataSource")
    @Qualifier("afterSalesDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.after_sales_db")
    @Primary
    public DataSource afterSalesDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.AFTER_SALES_DB)
    @Primary
    public JdbcTemplate afterSalesDBJdbcTemplate(@Qualifier("afterSalesDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean(name = "creditCenterDataSource")
    @Qualifier("creditCenterDataSource")
    @ConfigurationProperties(prefix="spring.datasource.credit_center")
    public DataSource creditCenterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.CREDIT_CENTER)
    public JdbcTemplate creditCenterJdbcTemplate(@Qualifier("creditCenterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "crmCenterDataSource")
    @Qualifier("crmCenterDataSource")
    @ConfigurationProperties(prefix="spring.datasource.crm_center")
    public DataSource crmCenterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.CRM_CENTER)
    public JdbcTemplate crmCenterJdbcTemplate(@Qualifier("crmCenterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean(name = "memberCenterDataSource")
    @Qualifier("memberCenterDataSource")
    @ConfigurationProperties(prefix="spring.datasource.member_center")
    public DataSource memberCenterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.MEMBER_CENTER)
    public JdbcTemplate memberCenterJdbcTemplate(@Qualifier("memberCenterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "openTradeDBDataSource")
    @Qualifier("openTradeDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.open_trade_db")
    public DataSource openTradeDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.OPEN_TRADE_DB)
    public JdbcTemplate openTradeDBJdbcTemplate(@Qualifier("openTradeDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean(name = "orderDBDataSource")
    @Qualifier("orderDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.order_db")
    public DataSource orderDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.ORDER_DB)
    public JdbcTemplate orderDBJdbcTemplate(@Qualifier("orderDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean(name = "partnerDBDataSource")
    @Qualifier("partnerDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.partner_db")
    public DataSource partnerDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.PARTNER_DB)
    public JdbcTemplate partnerDBJdbcTemplate(@Qualifier("partnerDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "paymentDBDataSource")
    @Qualifier("paymentDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.payment_db")
    public DataSource paymentDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.PAYMENT_DB)
    public JdbcTemplate paymentDBJdbcTemplate(@Qualifier("paymentDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "picklistDBDataSource")
    @Qualifier("picklistDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.picklist_db")
    public DataSource picklistDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.PICKLIST_DB)
    public JdbcTemplate picklistDBJdbcTemplate(@Qualifier("picklistDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "productCenterDataSource")
    @Qualifier("productCenterDataSource")
    @ConfigurationProperties(prefix="spring.datasource.product_center")
    public DataSource productCenterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.PRODUCT_CENTER)
    public JdbcTemplate productCenterJdbcTemplate(@Qualifier("productCenterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "promotionDBDataSource")
    @Qualifier("promotionDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.promotion_db")
    public DataSource promotionDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.PROMOTION_DB)
    public JdbcTemplate promotionDBJdbcTemplate(@Qualifier("promotionDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "promotionCenterDataSource")
    @Qualifier("promotionCenterDataSource")
    @ConfigurationProperties(prefix="spring.datasource.promotion_center")
    public DataSource promotionCenterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.PROMOTION_CENTER)
    public JdbcTemplate promotionCenterJdbcTemplate(@Qualifier("promotionCenterDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "superSpeciesProcessDBDataSource")
    @Qualifier("superSpeciesProcessDBDataSource")
    @ConfigurationProperties(prefix="spring.datasource.super_species_process_db")
    public DataSource superSpeciesProcessDBDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceTemplete.SUPER_SPECIES_PROCESS_DB)
    public JdbcTemplate superSpeciesProcessDBJdbcTemplate(@Qualifier("superSpeciesProcessDBDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean(name = "automationtestDataSource")
    @Qualifier("automationtestDataSource")
    @ConfigurationProperties(prefix="spring.datasource.automationtest")
    public DataSource automationtestDataSource() {
        return DataSourceBuilder.create().build();
    }
}

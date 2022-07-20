package com.example.springdatabaseisolation.configuration;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ApplicationConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "message")
    public DataSource message() {
        return dataSourceMem("message");
    }

    @Bean
    public DataSourceInitializer bInit(DataSource message) {
        return init(message, "message");
    }

    private DataSourceInitializer init(DataSource dataSource, String name) {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(
                new ResourceDatabasePopulator(
                        new ClassPathResource(name + ".sql")
                )
        );
        return dataSourceInitializer;
    }

    private DataSource dataSource(String fileName) {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        //For dirty read
//        jdbcDataSource.setURL("jdbc:h2:./" + fileName + ";LOCK_MODE=0");
        jdbcDataSource.setURL("jdbc:h2:./" + fileName );
        jdbcDataSource.setUser("sa");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private DataSource dataSourceMem(String fileName) {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        //For dirty read
//        jdbcDataSource.setURL("jdbc:h2:./" + fileName + ";LOCK_MODE=0");
        jdbcDataSource.setURL("jdbc:h2:mem:" + fileName + ";DB_CLOSE_DELAY=-1;LOCK_MODE=0");
        jdbcDataSource.setUser("sa");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }
}

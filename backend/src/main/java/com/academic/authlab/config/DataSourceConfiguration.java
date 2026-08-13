package com.academic.authlab.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSourceConfiguration {
    @Bean("primaryJdbcTemplate")
    JdbcTemplate primaryJdbcTemplate(
            @Value("${auth.database.primary.url}") String url,
            @Value("${auth.database.username}") String username,
            @Value("${auth.database.password}") String password) {
        return new JdbcTemplate(dataSource(url, username, password));
    }

    @Bean("secondaryJdbcTemplate")
    JdbcTemplate secondaryJdbcTemplate(
            @Value("${auth.database.secondary.url}") String url,
            @Value("${auth.database.username}") String username,
            @Value("${auth.database.password}") String password) {
        return new JdbcTemplate(dataSource(url, username, password));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private DataSource dataSource(String url, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}

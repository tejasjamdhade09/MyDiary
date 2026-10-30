package com.diary.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Production DataSource. Accepts ANY of the usual Render ways of passing the database:
 *  - DB_URL / DB_USER / DB_PASSWORD      (jdbc:postgresql://... or Render's postgres://... URL)
 *  - DB_HOST / DB_PORT / DB_NAME / ...   (render.yaml)
 *  - DATABASE_URL                        (postgres://user:pass@host/db)
 *  - SPRING_DATASOURCE_URL / _USERNAME / _PASSWORD
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties props, Environment env) {
        String url = props.getUrl();
        String user = props.getUsername();
        String pass = props.getPassword();

        boolean explicit = has(env, "DB_URL") || has(env, "DB_HOST") || has(env, "SPRING_DATASOURCE_URL");
        if (!explicit && has(env, "DATABASE_URL")) {
            url = env.getProperty("DATABASE_URL");
        }
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            URI uri = URI.create(url);
            String info = uri.getUserInfo();
            if (info != null) {
                int i = info.indexOf(':');
                if (user == null || user.isBlank()) user = i < 0 ? info : info.substring(0, i);
                if ((pass == null || pass.isBlank()) && i >= 0) pass = info.substring(i + 1);
            }
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            url = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath()
                    + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
        }
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            throw new IllegalStateException("Database URL is missing or invalid: '" + url
                    + "'. Set DB_URL (jdbc:postgresql://HOST:5432/DBNAME) or DATABASE_URL.");
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(url);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setMaximumPoolSize(5);
        ds.setConnectionTimeout(30000);
        return ds;
    }

    private static boolean has(Environment env, String key) {
        String v = env.getProperty(key);
        return v != null && !v.isBlank();
    }
}

package com.morrowshop.config;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

public final class MyBatisProvider {
    private static final SqlSessionFactory FACTORY = buildFactory();
    private MyBatisProvider() {}

    public static SqlSessionFactory getFactory() { return FACTORY; }

    private static SqlSessionFactory buildFactory() {
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            Map<String, String> env = System.getenv();
            String url = env.containsKey("DB_URL") ? env.get("DB_URL") : "jdbc:mysql://localhost:3306/morrow_shop?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
            String username = env.containsKey("DB_USERNAME") ? env.get("DB_USERNAME") : "morrow";
            String password = env.containsKey("DB_PASSWORD") ? env.get("DB_PASSWORD") : "";
            PooledDataSource dataSource = new PooledDataSource("com.mysql.cj.jdbc.Driver", url, username, password);
            Environment environment = new Environment("development", new JdbcTransactionFactory(), dataSource);
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
            factory.getConfiguration().setEnvironment(environment);
            return factory;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

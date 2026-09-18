package com.myshop.service.support;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public final class SupportTestDatabase implements AutoCloseable {
    public final SqlSessionFactory factory;
    private final PooledDataSource dataSource;
    public SupportTestDatabase() throws Exception {
        try(Reader reader=Resources.getResourceAsReader("mybatis-config.xml")) {
            factory=new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource=new PooledDataSource("org.h2.Driver","jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE","sa","");
        factory.getConfiguration().setEnvironment(new Environment("test",new JdbcTransactionFactory(),dataSource));
        try(Connection c=dataSource.getConnection(); Statement s=c.createStatement()) {
            s.execute("CREATE TABLE users(id INT PRIMARY KEY,email VARCHAR(190),password_hash VARCHAR(100),name VARCHAR(50),role VARCHAR(20),created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            s.execute("CREATE TABLE products(id INT PRIMARY KEY,sku VARCHAR(40),name VARCHAR(120),description TEXT,price DECIMAL(12,2),stock INT,category VARCHAR(50),image_url VARCHAR(500),featured BOOLEAN DEFAULT FALSE,active BOOLEAN DEFAULT TRUE,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            s.execute("CREATE TABLE orders(id INT PRIMARY KEY,user_id INT,order_number VARCHAR(40),status VARCHAR(20),total_amount DECIMAL(12,2),created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            s.execute("CREATE TABLE order_items(id INT PRIMARY KEY,order_id INT,product_id INT,product_name VARCHAR(120),quantity INT)");
            s.execute("INSERT INTO users(id,email,name,role) VALUES(1,'one@example.test','고객 하나','CUSTOMER'),(2,'two@example.test','고객 둘','CUSTOMER'),(3,'admin@example.test','관리자','ADMIN')");
            s.execute("INSERT INTO products(id,sku,name,description,price,stock,category) VALUES(1,'MUG','머그 세트','도자기 머그 두 개',42000,30,'주방'),(2,'BOOK','노트','기록용 노트',18000,10,'문구')");
            s.execute("INSERT INTO orders(id,user_id,order_number,status,total_amount) VALUES(1,1,'ORDER-ONE','PAID',42000),(2,2,'ORDER-TWO','PAID',18000)");
            s.execute("INSERT INTO order_items VALUES(1,1,1,'머그 세트',1),(2,2,2,'노트',1)");
            try(InputStream in=Resources.getResourceAsStream("db/support.sql")) {
                for(String sql:new String(in.readAllBytes(),StandardCharsets.UTF_8).split(";")) if(!sql.isBlank()) s.execute(sql);
            }
        }
    }
    public void execute(String sql) throws Exception {try(Connection c=dataSource.getConnection();Statement s=c.createStatement()){s.execute(sql);}}
    public void close(){dataSource.forceCloseAll();}
}

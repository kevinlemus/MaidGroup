package com.maidgroup.maidgroup.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class ConnectionFactory {
    private static final ConnectionFactory connectionFactory = new ConnectionFactory(); //eager instantiate
    private Properties properties = new Properties();
    private static String url;
    private static String password;
    private static String username;

    private ConnectionFactory(){

    }

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Connection getConnection(){
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Value("${spring.datasource.url}")
    public void setUrl(String dbUrl) {
        url = dbUrl;
    }
    @Value("${spring.datasource.password}")
    public void setPassword(String dbPassword) {
        password = dbPassword;
    }
    @Value("${spring.datasource.username}")
    public void setUsername(String dbUsername) {
        username = dbUsername;
    }
}

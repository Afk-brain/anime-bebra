package org.mo.bots.data;

import java.sql.*;

public class MySql {

    static Connection connection;
    static Statement statement;

    static {
        try {
            System.out.println("Database connection...");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bot", "root", "&Root1399");
            statement = connection.createStatement();
            System.out.println("Database connected!");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public static ResultSet execute(String SQL) {
        try {
            return statement.executeQuery(SQL);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int update(String SQL) {
        try {
            return statement.executeUpdate(SQL);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private MySql() {}

}

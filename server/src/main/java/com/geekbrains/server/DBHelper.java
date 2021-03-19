package com.geekbrains.server;

import java.sql.*;

public class DBHelper implements AutoCloseable{

    private static DBHelper instance;
    private static Connection connection;

    private static PreparedStatement findByLoginAndPassword;
    private static PreparedStatement changeNick;

    private DBHelper() { }

    public static DBHelper getInstance() {
        if (instance == null) {
            loadDriverAndOpenConnection();
            createPreparedStatements();

            instance = new DBHelper();
        }
        return instance;
    }

    private static void loadDriverAndOpenConnection() {
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Ошибка открытия соединения с базой данных!");
            e.printStackTrace();
        }
    }

    private static void createPreparedStatements() {
        try {
            findByLoginAndPassword = connection.prepareStatement("SELECT * FROM user WHERE LOWER(login)=LOWER(?)" +
                    " AND LOWER(password)=LOWER(?)");
            changeNick = connection.prepareStatement("UPDATE user SET nickname=? WHERE nickname=?");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String findByLoginAndPassword(String login, String password) {
        ResultSet resultSet = null;

        try {
            findByLoginAndPassword.setString(1,login);
            findByLoginAndPassword.setString(2,password);

            resultSet = findByLoginAndPassword.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(resultSet);
        }

        return null;
    }
    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public int updateNickname(String oldNickname, String newNickname) {
        try {
            changeNick.setString(1,newNickname);
            changeNick.setString(2,oldNickname);

            return changeNick.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void close() {
        try {
            findByLoginAndPassword.close();
            changeNick.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

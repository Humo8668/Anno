package uz.app.Anno.util;

import java.sql.Connection;
import java.sql.SQLException;

public interface PoolConnection {

    public Connection getConnection() throws SQLException;

    public void close(Connection connection) throws SQLException;
}

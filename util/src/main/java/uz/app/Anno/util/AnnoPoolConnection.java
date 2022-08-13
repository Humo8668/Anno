package uz.app.Anno.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

public class AnnoPoolConnection implements PoolConnection {

    public static final int DEFAULT_CONNECTION_RETRY_MS = 50;
    public static final int DEFAULT_POOL_SIZE = 10;

    private String DB_URL, DB_LOGIN, DB_PASSWORD;
    private int POOL_SIZE = DEFAULT_POOL_SIZE;  // Default: 10
    private int CONN_RETRY_MILLISECONDS = DEFAULT_CONNECTION_RETRY_MS;   // Time in milliseconds for delay per connection retry. Default: 50ms
    private boolean IS_AUTO_COMMIT = true;
    private ArrayBlockingQueue<Connection> availConnections = null;
    private ConcurrentSet<Connection> usingConnections = null;

    public AnnoPoolConnection(String DB_URL, String DB_LOGIN, String DB_PASSWORD, int POOL_SIZE, int CONN_RETRY_MILLISECONDS) throws IllegalArgumentException
    {
        if(DB_URL == null || DB_URL.length() <= 0)
            throw new IllegalArgumentException("Invalid URL for connection");

        if(DB_LOGIN == null || DB_LOGIN.length() <= 0)
            throw new IllegalArgumentException("Invalid login for connection");

        if(DB_PASSWORD == null || DB_PASSWORD.length() <= 0)
            throw new IllegalArgumentException("Invalid password for connection");

        if(CONN_RETRY_MILLISECONDS < 0)
            CONN_RETRY_MILLISECONDS = DEFAULT_CONNECTION_RETRY_MS;
        
        if(POOL_SIZE < 0)
            POOL_SIZE = DEFAULT_POOL_SIZE;

        this.DB_URL = DB_URL;
        this.DB_LOGIN = DB_LOGIN;
        this.DB_PASSWORD = DB_PASSWORD;
        this.POOL_SIZE = POOL_SIZE;
        this.CONN_RETRY_MILLISECONDS = CONN_RETRY_MILLISECONDS;
        
        availConnections = new ArrayBlockingQueue<Connection>(Math.max(1, POOL_SIZE));
        usingConnections = new ConcurrentSet<Connection>();

        Connection connection = null;
        for(int i = 0; i < this.POOL_SIZE; i++)
        {
            try {
                connection = this.newConnection();
            } catch (SQLException ex){
                ex.printStackTrace();
                continue;
            }
            availConnections.add(connection);
        }
    }

    public AnnoPoolConnection(String DB_URL, String DB_LOGIN, String DB_PASSWORD, int POOL_SIZE) throws IllegalArgumentException
    {
        this(DB_URL, DB_LOGIN, DB_PASSWORD, POOL_SIZE, DEFAULT_CONNECTION_RETRY_MS);
    }

    public AnnoPoolConnection(String DB_URL, String DB_LOGIN, String DB_PASSWORD) throws IllegalArgumentException
    {
        this(DB_URL, DB_LOGIN, DB_PASSWORD, DEFAULT_POOL_SIZE);
    }

    protected Connection newConnection() throws SQLException
    {
        Connection connection = null;
        connection = DriverManager.getConnection(DB_URL, DB_LOGIN, DB_PASSWORD);
        
        connection.setAutoCommit(IS_AUTO_COMMIT);
        return connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = null;

        connection = availConnections.poll();
        if(connection==null)
            connection = newConnection();
        else
            usingConnections.add(connection);
        return connection;
    }

    @Override
    public void close(Connection connection) throws SQLException {
        synchronized (usingConnections)
        {
            if(usingConnections.hasElement(connection)) {
                usingConnections.removeElement(connection);
                availConnections.add(connection);
            } else {
                if(!connection.isClosed())
                    connection.close();
            }
        }
    }
    
}

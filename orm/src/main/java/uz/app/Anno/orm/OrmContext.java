package uz.app.Anno.orm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import uz.app.Anno.util.AnnoPoolConnection;
import uz.app.Anno.util.PoolConnection;

public class OrmContext {

    boolean contextInitialized = false;
    PoolConnection poolConnection;
    Metadata ormMetadata;
    Hashtable<Object, Object> properties;

    public OrmContext(String fileUri) throws FileNotFoundException, IOException {
        InitContext(fileUri);
    }

    public void InitContext(String fileUri) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        prop.load(loader.getResourceAsStream(fileUri));
        properties = prop;
        contextInitialized = true;
    }

    /*static {
        poolConnection = new AnnoPoolConnection(DB_URL, DB_LOGIN, DB_PASSWORD);
    }*/

    private String getProperty(String propName) {
        if(properties.containsKey(propName))
            return properties.get(propName).toString();
        else 
            return null;
    }

    private String getDatabaseUrl() {
        return getProperty("DB_URL");
    }

    private String getDatabaseUser() {
        return getProperty("DB_USER");
    }

    private String getDatabasePassword() {
        return getProperty("DB_PASSWORD");
    }

    public String getDatabaseDefaultSchema() {
        return getProperty("DB_DEFAULT_SCHEMA");
    }

    private int getConnectionPoolSize() {
        String str = getProperty("DB_CONN_POOL_SIZE");
        int connPoolSize = -1;
        try {
            connPoolSize = Integer.parseInt(str);
        } catch(NumberFormatException ex) {
            connPoolSize = -1;
            ex.printStackTrace();
        }

        return connPoolSize;
    }

    public PoolConnection getPoolConnection() throws SQLException {
        if(!contextInitialized)
            throw new RuntimeException("ORM context is not initialized!");
        if(poolConnection == null) {
            poolConnection = new AnnoPoolConnection(getDatabaseUrl(), getDatabaseUser(), getDatabasePassword(), getConnectionPoolSize());
        }
        return poolConnection;
    }

    public Metadata getMetadata() {
        if(!contextInitialized)
            throw new RuntimeException("ORM context is not initialized!");
        if(ormMetadata == null) {
            ormMetadata = new Metadata(this);
        }
        return ormMetadata;
    }
}

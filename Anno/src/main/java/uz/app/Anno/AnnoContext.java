package uz.app.Anno;

import java.util.Enumeration;

import javax.servlet.ServletContext;

import uz.app.AnnoDBC.PoolConnection;

public class AnnoContext {
    protected static final String SETTING_FILE_URI = "";
    protected static final String KEY_DEBUG_MODE = "DEBUG_MODE";
    protected static final String KEY_DB_URL = "DB_URL";
    protected static final String KEY_DB_LOGIN = "DB_USERNAME";
    protected static final String KEY_DB_PASSWORD = "DB_PASSWORD";
    protected static final String KEY_CONN_POOL_SIZE = "DB_CONN_POOL_SIZE";
    protected static boolean isInitialized = false;

    static PoolConnection poolConnection;
    
    public static void Init(ServletContext ctx) {
        if(isInitialized)
            return;
        isInitialized = true;

        Enumeration<String> ctxEnumeration = ctx.getInitParameterNames();
        while(ctxEnumeration.hasMoreElements()) {
            String key = ctxEnumeration.nextElement();
            System.setProperty(key.toUpperCase(), ctx.getInitParameter(key));
        }

        if(System.getProperty(KEY_DEBUG_MODE) == null) {
            System.setProperty(KEY_DEBUG_MODE, "false");
        }
    }

    public static void setValue(String key, String value) {
        System.setProperty(key, value);
    }

    public static String getValue(String key) {
        return System.getProperty(key);
    }

    public static boolean isDebug(){
        return "TRUE".equals(System.getProperty(KEY_DEBUG_MODE).toUpperCase());
    }

    public static PoolConnection getPoolConnection() {
        if(poolConnection == null)
            poolConnection = new PoolConnection(
                System.getProperty(KEY_DB_URL), 
                System.getProperty(KEY_DB_LOGIN), 
                System.getProperty(KEY_DB_PASSWORD), 
                Integer.parseInt(System.getProperty(KEY_CONN_POOL_SIZE))
            );

        return poolConnection;
    }
    public static void setPoolConnection(PoolConnection poolConnection) throws NullPointerException {
        if(poolConnection == null){
            throw new NullPointerException("PoolConnection is null");
        }
        AnnoContext.poolConnection = poolConnection;
    }

    
}

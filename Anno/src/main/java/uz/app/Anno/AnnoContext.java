package uz.app.Anno;

import java.util.Enumeration;

import javax.servlet.ServletContext;

import uz.app.AnnoDBC.PoolConnection;

public class AnnoContext {
    protected static final String SETTING_FILE_URI = "";
    protected static final String KEY_DEBUG_MODE = "DEBUG_MODE";
    protected static final String KEY_DB_URL = "DB_URL";
    protected static final String KEY_DB_LOGIN = "DB_LOGIN";
    protected static final String KEY_DB_PASSWORD = "DB_PASSWORD";
    protected static final String KEY_CONN_POOL_SIZE = "DB_CONN_POOL_SIZE";
    protected static boolean isInitialized = false;

    public static PoolConnection poolConnection;
    
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
            
        poolConnection = new PoolConnection(getDBurl(), getDBlogin(), getDBpassword(), getConnPoolSize());
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

    public static String getDBurl() {
        return System.getProperty(KEY_DB_URL);
    }
    public static String getDBlogin() {
        return System.getProperty(KEY_DB_URL);
    }
    public static String getDBpassword() {
        return System.getProperty(KEY_DB_URL);
    }
    public static PoolConnection getPoolConnection() {
        return poolConnection;
    }
    public static int getConnPoolSize() {
        return Integer.parseInt(System.getProperty(KEY_CONN_POOL_SIZE));
    }
}

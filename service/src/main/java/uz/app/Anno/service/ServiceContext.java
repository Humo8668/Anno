package uz.app.Anno.service;

import java.util.Enumeration;
import javax.servlet.ServletContext;

public class ServiceContext {
    private static final String SETTING_FILE_URI = "";
    private static final String KEY_DEBUG_MODE = "DEBUG_MODE";

    private static boolean isInitialized = false;

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
}

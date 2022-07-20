package uz.app.Anno;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import uz.app.Anno.classes.CustomEventListener;
import uz.app.Anno.service.AnnoEventListener;

public class AnnoEventListenerTest  {

    @Test
    public void TestEventListener() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        assertTrue(!CustomEventListener.afterAnnoInitialized);
        assertTrue(!CustomEventListener.afterServicesInitialized);
        assertTrue(!CustomEventListener.beforeServicesInitializing);

        Method collectListeners = AnnoEventListener.class.getDeclaredMethod("collectListeners");
        collectListeners.setAccessible(true);
        collectListeners.invoke("");

        Method AfterAnnoInitialized = AnnoEventListener.class.getDeclaredMethod("triggerAfterAnnoInitialized");
        AfterAnnoInitialized.setAccessible(true);
        AfterAnnoInitialized.invoke("");
        assertTrue(CustomEventListener.afterAnnoInitialized);

        Method BeforeServicesInitializing = AnnoEventListener.class.getDeclaredMethod("triggerBeforeServicesInitializing");
        BeforeServicesInitializing.setAccessible(true);
        BeforeServicesInitializing.invoke("");
        assertTrue(CustomEventListener.beforeServicesInitializing);

        Method AfterServicesInitialized = AnnoEventListener.class.getDeclaredMethod("triggerAfterServicesInitialized");
        AfterServicesInitialized.setAccessible(true);
        AfterServicesInitialized.invoke("");
        assertTrue(CustomEventListener.afterServicesInitialized);
        
    }
    
}

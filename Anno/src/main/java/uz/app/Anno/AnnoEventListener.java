package uz.app.Anno;

import java.util.HashMap;
import java.util.Set;

import org.reflections.*;
import org.reflections.scanners.SubTypesScanner;

public abstract class AnnoEventListener {
    private static Set<Class<? extends AnnoEventListener>> listeners;
    private static HashMap<Class<? extends AnnoEventListener>, AnnoEventListener> listenerInstances;

    static void collectListeners() {
        Reflections reflections = new Reflections("", new SubTypesScanner());
        listeners = reflections.getSubTypesOf(AnnoEventListener.class);
        listenerInstances = new HashMap<Class<? extends AnnoEventListener>, AnnoEventListener>();
        for (Class<? extends AnnoEventListener> listener : listeners) {
            try {
                AnnoEventListener listenerObject = listener.getConstructor().newInstance();
                listenerInstances.put(listener, listenerObject);
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    static void triggerAfterAnnoInitialized() {
        for (Class<? extends AnnoEventListener> listenerClass : listenerInstances.keySet()) {
            listenerInstances.get(listenerClass).AfterAnnoInitialized();
        }
    }
    static void triggerBeforeServicesInitializing() {
        for (Class<? extends AnnoEventListener> listenerClass : listenerInstances.keySet()) {
            listenerInstances.get(listenerClass).BeforeServicesInitializing();
        }
    }
    static void triggerAfterServicesInitialized() {
        for (Class<? extends AnnoEventListener> listenerClass : listenerInstances.keySet()) {
            listenerInstances.get(listenerClass).AfterServicesInitialized();
        }
    }

    protected abstract void AfterAnnoInitialized();
    protected abstract void BeforeServicesInitializing();
    protected abstract void AfterServicesInitialized();
}

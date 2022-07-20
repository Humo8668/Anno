package uz.app.Anno.classes;

import uz.app.Anno.service.AnnoEventListener;


public class CustomEventListener extends AnnoEventListener{
    public static boolean afterAnnoInitialized = false;
    public static boolean beforeServicesInitializing = false;
    public static boolean afterServicesInitialized = false;

    @Override
    protected void AfterAnnoInitialized() {
        afterAnnoInitialized = true;
    }

    @Override
    protected void BeforeServicesInitializing() {
        beforeServicesInitializing = true;
    }

    @Override
    protected void AfterServicesInitialized() {
        afterServicesInitialized = true;
    }
}

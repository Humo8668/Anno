package uz.app.Anno.test;

import uz.app.Anno.AnnoContext;
import uz.app.Anno.AnnoEventListener;
import uz.app.AnnoDBC.PoolConnection;

public class EventListener extends AnnoEventListener {
    @Override
    protected void AfterAnnoInitialized() {
        

    }

    @Override
    protected void BeforeServicesInitializing() {
        try {
            Class.forName("org.postgresql.Driver");
            AnnoContext.setPoolConnection(new PoolConnection(
                System.getProperty("DB_URL"), 
                System.getProperty("DB_USERNAME"), 
                System.getProperty("DB_PASSWORD"), 
                Integer.parseInt(System.getProperty("DB_CONN_POOL_SIZE"))));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void AfterServicesInitialized() {
        
    }
}

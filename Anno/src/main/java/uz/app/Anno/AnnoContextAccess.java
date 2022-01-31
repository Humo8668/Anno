package uz.app.Anno;

import java.util.LinkedList;

public class AnnoContextAccess extends SecurityManager {
    private LinkedList<String> hiddenProps = new LinkedList<String>();
    AnnoContextAccess() {
        hiddenProps.add(AnnoContext.KEY_DB_URL);
        hiddenProps.add(AnnoContext.KEY_DB_LOGIN);
        hiddenProps.add(AnnoContext.KEY_DB_PASSWORD);
    }
    @Override
    public void checkPropertyAccess(String key) {
        if(hiddenProps.contains(key))
            super.checkPropertyAccess(key);
        return;
    }
}

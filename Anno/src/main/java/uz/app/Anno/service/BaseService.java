package uz.app.Anno.service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

public abstract class BaseService {
    protected String serviceName;

    public BaseService()
    {
        try{
            RouteManager.AddService(this);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        init();
    }

    public void init()
    {
        System.out.println("Service <" + this.getClass().getName() +  "> initialized");
        return;
    }

    public String getReqBody(HttpServletRequest req) throws IOException
    {
        return req.getReader().lines().collect(Collectors.joining());
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode();
    }
}

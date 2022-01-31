package uz.app.Anno.service;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

public abstract class BaseService {
    protected String serviceName;

    public BaseService()
    {
        try{
            RouteProcessingService.AddService(this);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        init();
    }

    public void init()
    {
        System.out.println("Service <" + this.serviceName +  "> initialized");
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

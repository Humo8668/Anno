package uz.app.Anno.service;

import uz.app.Anno.service.annotations.Route;
import uz.app.Anno.service.annotations.Service;
import uz.app.Anno.util.HttpMethod;
import uz.app.Anno.util.Pair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class RouteManager {

    protected static HashMap<Pair<String, HttpMethod>, Pair<BaseService, Method>> RouteMapping =
            new HashMap<Pair<String, HttpMethod>, Pair<BaseService, Method>>();

    public static void AddService(BaseService service) throws Exception
    {
        Class<? extends BaseService> serviceClass = service.getClass();
        Annotation serviceAnno = serviceClass.getAnnotation(Service.class);
        String serviceName = "";
        if(serviceAnno != null)
            serviceName = ((Service) serviceAnno).value();

        serviceName = serviceName.trim();
        serviceName = serviceName.toLowerCase();
        if(!serviceName.startsWith("/"))
            serviceName = "/" + serviceName;
        if(serviceName.endsWith("/"))
            serviceName = serviceName.substring(0, serviceName.length()-1);

        Method[] methods = serviceClass.getDeclaredMethods();
        for (Method method: methods) {
            Annotation route = method.getAnnotation(Route.class);
            if(route == null)
                continue;
            Class<?>[] paramTypes = method.getParameterTypes();

            if(paramTypes.length != 2)
                throw new Exception("Method's arguments count is wrong.");
            if(!paramTypes[0].equals(HttpServletRequest.class) ||
                    !paramTypes[1].equals(HttpServletResponse.class))
                throw new Exception("Method's argument types are wrong.");

            
            HttpMethod httpMethod = ((Route) route).method();
            String path = ((Route) route).value();

            path = path.trim();
            path = path.toLowerCase();
            if(!path.startsWith("/"))
                path = "/" + path;
            if(path.endsWith("/"))
                path = path.substring(0, path.length()-1);

            String fullPath = serviceName + path;
            //System.out.println(httpMethod.toString() + " : " + fullPath);
            RouteMapping.put(
                    new Pair<String, HttpMethod>(fullPath, httpMethod),
                    new Pair<BaseService, Method>(service, method));
        }
        service.serviceName = serviceName;
    }

    public static void process(String reqPath, HttpMethod httpMethod, HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException
    {
        if(reqPath == null)
        {
            res.sendError(404);
            return;
        }
        reqPath = reqPath.trim().toLowerCase();
        Pair<String, HttpMethod> routeInfo = new Pair<String, HttpMethod>(reqPath, httpMethod);
        Pair<BaseService, Method> logicMethod;
        if(!RouteMapping.containsKey(routeInfo))
        {
            System.out.println("Route " + reqPath + " have not been caught by main servlet.");
            res.sendError(404);
            return;
        }

        logicMethod = RouteMapping.get(routeInfo);
        Method method = logicMethod.getValue();
        BaseService service = logicMethod.getKey();
        method.setAccessible(true);

        try {
            method.invoke(service, req, res);
        }catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if(cause == null)
                throw new ServletException(ex.getMessage());

            if(cause.getClass().equals(ServletException.class))
                throw new ServletException(ex.getMessage());
            else if(cause.getClass().equals(IOException.class))
                throw new IOException(ex.getMessage());
            else
                throw new ServletException(cause);
        }
    }
}

package uz.app.Anno.service;

import uz.app.Anno.service.annotations.Route;
import uz.app.Anno.service.annotations.Service;
import uz.app.Anno.service.exceptions.WrongUsageException;
import uz.app.Anno.util.HttpMethod;
import uz.app.Anno.util.Pair;
import uz.app.Anno.util.ReflectionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class RouteManager {

    protected static HashMap<Pair<String, HttpMethod>, Pair<AnnoService, Method>> RouteMapping =
            new HashMap<Pair<String, HttpMethod>, Pair<AnnoService, Method>>();

    private static Collection<Class<? extends AnnoService>> serviceClasses = new HashSet<Class<? extends AnnoService>>();
    private static Collection<AnnoService> serviceInstances = new HashSet<AnnoService>();
    public static void collectServices() throws WrongUsageException {
        serviceClasses = ReflectionUtil.getSubtypesOf(AnnoService.class);
        for (Class<? extends AnnoService> serviceClass : serviceClasses) {
            try {
                AnnoService service = serviceClass.getConstructor().newInstance();
                serviceInstances.add(service);
                analyzeService(service);
            } catch (InstantiationException e) {
                throw new WrongUsageException("Couldn't create instance of service " + serviceClass.getName(), e);
            } catch (IllegalAccessException e) {
                throw new WrongUsageException("Couldn't create instance of service " + serviceClass.getName() + " due to its access issue.", e);
            } catch (IllegalArgumentException e) {
                throw new WrongUsageException("A service class must declare constructor with no arguments. Service: " + serviceClass.getName(), e);
            } catch (InvocationTargetException e) {
                throw new WrongUsageException("Couldn't create instance of service " + serviceClass.getName(), e);
            } catch (NoSuchMethodException e) {
                throw new WrongUsageException("A service class must declare constructor with no arguments. Service: " + serviceClass.getName(), e);
            } catch (SecurityException e) {
                throw new WrongUsageException("Couldn't create instance of service " + serviceClass.getName() + " due to its security issue.", e);
            }
        }
    }


    static void analyzeService(AnnoService service) throws WrongUsageException
    {
        Class<? extends AnnoService> serviceClass = service.getClass();
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
                throw new WrongUsageException("Service method's arguments count is wrong. Service: " + service.getClass().getName() + "; Method: " + method.getName());
            if(!paramTypes[0].equals(HttpServletRequest.class) ||
                    !paramTypes[1].equals(HttpServletResponse.class))
                throw new WrongUsageException("Method's argument types are wrong. Service: " + service.getClass().getName() + "; Method: " + method.getName());
            
            HttpMethod httpMethod = ((Route) route).method();
            String path = ((Route) route).value();

            path = path.trim();
            path = path.toLowerCase();
            if(!path.startsWith("/"))
                path = "/" + path;
            if(path.endsWith("/"))
                path = path.substring(0, path.length()-1);

            String fullPath = serviceName + path;
            if(RouteMapping.containsKey(new Pair<String, HttpMethod>(fullPath, httpMethod))) {
                throw new WrongUsageException("Method for such route path is already registered. path: " + fullPath + "; method: " + httpMethod.toString());
            }
            //System.out.println(httpMethod.toString() + " : " + fullPath);
            RouteMapping.put(
                    new Pair<String, HttpMethod>(fullPath, httpMethod),
                    new Pair<AnnoService, Method>(service, method));
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
        Pair<AnnoService, Method> logicMethod;
        if(!RouteMapping.containsKey(routeInfo))
        {
            System.out.println("Route "+routeInfo.getValue()+" " + routeInfo.getKey() + " have not been caught by main servlet.");
            res.sendError(404);
            return;
        }

        logicMethod = RouteMapping.get(routeInfo);
        Method method = logicMethod.getValue();
        AnnoService service = logicMethod.getKey();
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

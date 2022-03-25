package uz.app.Anno.service;

import uz.app.Anno.Anno;
import uz.app.Anno.AnnoContext;
import uz.app.Anno.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="AnnoMainServlet", value = "/*")
public class MainServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        AnnoContext.Init(getServletContext()); // Reads all settings from web.xml file
        Anno.Init(); // Runs throught all classes that extend <BaseService.class> and creates instances for them all.
        System.out.println("Main_servlet initialized");
        //this.getClass().getClassLoader().toString();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod().trim().toUpperCase();
        System.out.println(HttpMethod.valueOf(HttpMethod.class, method) + " " + req.getRequestURI());
        String requestUri = MainServlet.getRequestURI(req);
        RouteManager.process(requestUri, HttpMethod.valueOf(HttpMethod.class, method), req, resp);
        resp.getWriter().flush();
    }

    public static String getRequestURI(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        String requestUri = req.getRequestURI();
        if(contextPath != null && contextPath.length() > 0)
            requestUri = requestUri.substring(contextPath.length(), requestUri.length());   // parse request path relative to application

        return requestUri;
    }
}

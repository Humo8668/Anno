package uz.app.Anno.service;

import uz.app.Anno.service.exceptions.WrongUsageException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(name="AnnoMainServlet", value = "/*")
public class MainServlet extends HttpServlet {
    static Logger log = LoggerFactory.getLogger(MainServlet.class);

    public void printASCIIArt() {
        StringBuilder sb = new StringBuilder();
        
        sb.append( "                                 \n");
        sb.append( "    /\\                          \n");
        sb.append( "   /  \\   _ __  _ __   ___      \n");
        sb.append( "  / /\\ \\ | '_ \\| '_ \\ / _ \\ \n");
        sb.append( " / ____ \\| | | | | | | (_) |    \n");
        sb.append( "/_/    \\_\\_| |_|_| |_|\\___/   \n");
        sb.append( "                                 \n");
        sb.append( "                                 \n");
        
        try {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            log.info(sb.toString());
        } catch (ClassNotFoundException e) {
            System.out.println(sb.toString());
        }
    }

    @Override
    public void init() throws ServletException {
        printASCIIArt();
        ServiceContext.Init(getServletContext()); // Reads all settings from web.xml file
        AnnoEventListener.collectListeners();
        AnnoEventListener.triggerBeforeServicesInitializing();
        try {
            RouteManager.collectServices(); // Runs throught all classes that extend <BaseService.class> and creates instances for them all.
        } catch (WrongUsageException e) {
            throw new ServletException(e);
        }
        AnnoEventListener.triggerAfterServicesInitialized();
        log.info("Anno initialized");
        AnnoEventListener.triggerAfterAnnoInitialized();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod().trim().toUpperCase();
        log.debug(HttpMethod.valueOf(HttpMethod.class, method) + " " + req.getRequestURI());
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

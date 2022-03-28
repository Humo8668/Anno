package uz.app.Anno.test;

import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import uz.app.Anno.orm.*;
import uz.app.Anno.service.BaseService;
import uz.app.Anno.util.HttpMethod;

public class EchoService extends BaseService {
    
    public EchoService(){}

    @Route(value = "/echo", method = HttpMethod.GET)
    void getAll(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setStatus(200);
        out.print("{\"message\": \"ECHO... echo... echo...\"");
        return;
    }
}

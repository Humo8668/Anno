package uz.app.Anno.test;

import javax.servlet.http.*;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;

import uz.app.Anno.orm.*;
import uz.app.Anno.service.BaseService;
import uz.app.Anno.util.HttpMethod;

@Service("")
public class EchoService extends BaseService {
    UserRepository userRepo;

    public EchoService(){
        userRepo = new UserRepository();
    }

    @Route(value = "/echo", method = HttpMethod.GET)
    void getAll(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setStatus(200);
        out.print("ECHO... echo... echo...");
        return;
    }
}

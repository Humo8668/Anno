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

@Service("User")
public class UserService extends BaseService {
    UserRepository userRepo;
    Gson gson;

    public UserService(){
        try {
            Class.forName("org.postgresql.Driver");
            userRepo = new UserRepository();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        gson = new Gson();
        
    }

    @Route(value = "/getall", method = HttpMethod.GET)
    void getAll(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();

        User[] users;
        try {
            users = userRepo.getAll();
        } catch (SQLException ex) {
            ex.printStackTrace();
            res.sendError(500, "Error occurred: " + ex.getMessage());
            return;
        }

        String json = gson.toJson(users);
        res.setContentType("application/json");
        res.setStatus(200);
        out.print(json);
        return;
    }

    @Route(value = "/getById", method = HttpMethod.GET)
    void getById(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();

        Object userIdParameter = req.getParameter("id");
        if(userIdParameter == null || "".equals(userIdParameter))
        {
            res.setContentType("application/json");
            res.setStatus(200);
            out.print("{}");
            return;
        }
        User user;
        int userId = Integer.parseInt(userIdParameter.toString());
        try {
            user = userRepo.getById(userId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            res.sendError(500, "Error occurred: " + ex.getMessage());
            return;
        }

        String json = "";
        if(user != null)
            json = gson.toJson(user);
        else
            json = "{}";
        res.setContentType("application/json");
        res.setStatus(200);
        out.print(json);
        return;
    }
}

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

    public UserService(){
        userRepo = new UserRepository();
    }

    @Route(value = "/getall", method = HttpMethod.GET)
    void getAll(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();

        Gson gson = new Gson();
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
}

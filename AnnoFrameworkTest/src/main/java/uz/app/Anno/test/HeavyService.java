package uz.app.Anno.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uz.app.Anno.orm.Route;
import uz.app.Anno.orm.Service;
import uz.app.Anno.service.BaseService;
import uz.app.Anno.util.HttpMethod;

@Service("heavy")
public class HeavyService extends BaseService {
    
    public HeavyService(){}

    @Route(value="/sleeping", method = HttpMethod.GET)
    public void sleeping(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
        int sleepTime = 5;
        PrintWriter out = res.getWriter();
        try {
            Thread.sleep(sleepTime * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        res.setContentType("application/json");
        res.setStatus(200);
        out.print("{ \"sleeptime\": \""+sleepTime+"\", \"message\": \"Immitated heavy method throuth threat sleeping.\"}");
        return;
    }
}

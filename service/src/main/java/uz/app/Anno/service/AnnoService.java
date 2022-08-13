package uz.app.Anno.service;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;

public abstract class AnnoService {
    static Logger log = LoggerFactory.getLogger(AnnoService.class);
    protected String serviceName = "";

    public AnnoService()
    {
        init();
    }

    public void init()
    {
        log.info("Service <" + this.getClass().getName() +  "> initialized");
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

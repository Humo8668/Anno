<!--<style>
    code {
        padding: 2px;
        background-color: hsl(210,8%,95%);
    }

    pre>code{
        display: block;
        /*width: fit-content;*/
    }
</style>-->

<h1>AnnoFramework</h1>

Includes ORM functionality for PostgreSQL DBMS (<a href="/orm">ORM-module</a>) and easy url-mapping service (<a href="/service">Service-module</a>) instead of Servlets(one servlet per one path). 

Requirements: 
<ul>
<li>Java 11 or higher </li>
<li>Tomcat 7 or newer, the webapp's web.xml has to be declared conform Servlet 3.0 spec in order to get Tomcat to scan and process the annotations.
    <code>@WebServlet</code> annotation is used in framework.
</li>
<li>If you want to use ORM, then load some Postgres JDBC-driver class</li>
</ul>

<h2>Usage</h2>
Minimap:
<ul>
    <li><a href="#service"> Url-mapping Services</a></li>
    <li><a href="#orm"> ORM</a></li>
</ul>

<h3 name="service">Service</h3>
Sample of Service (url-mapping): <br>
Create some class extending <code>AnnoService</code> abstract class. Declare default constructor without arguments so Anno-framework could instantiate your service object. <br>
Then declare some method with arguments <code> HttpServletRequest req, HttpServletResponse res</code> in order given here. <br>
The method may return <code>void</code>, doesn't have sense. <br>
Annotate the method with <code>@Route</code> in which pass the route, corresponding to this method throught <code>value</code> and http-method throught <code>method</code>.<br>
By default http-method is <code>GET</code> mtehod.

<pre><code>
package uz.app.Anno.test;

import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import uz.app.Anno.service.AnnoService;
import uz.app.Anno.service.annotations.Route;
import uz.app.Anno.util.HttpMethod;

public class EchoService extends AnnoService {
    

    @Route(value = "/echo", method = HttpMethod.GET)
    void getAll(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setStatus(200);
        out.print("{\"message\": \"ECHO... echo... echo...\" }");
        return;
    }
}

</code></pre><br>
If you send Http-request <code>GET /echo </code>, you will get json-response: <br>
<pre><code>
{
    "message" : "ECHO... echo... echo..."
}
</code></pre>

All the values printed to <code> out </code> stream will be responsed to client. (as in servlets).<br>
Also you can annotate service class. This will help you group bunch of routes into one prefixed route:
<pre><code>
@Service("sample")
public class EchoService extends AnnoService {
    @Route(value = "/echo", method = HttpMethod.GET)
    void getAll(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setStatus(200);
        out.print("{\"message\": \"ECHO... echo... echo...\" }");
        return;
    }
}
</code></pre><br>
If you send Http-request <code>GET /sample/echo </code>, you will get json-response: <br>
<pre><code>
{
    "message" : "ECHO... echo... echo..."
}
</code></pre>

<h3 name="orm">ORM</h3>

First, you should create all entities to which data will be mapped:
Sample of <b>entity</b> binded to table by annotations:<br>
<pre><code>
package uz.app.Anno;

import uz.app.Anno.orm.IEntity;
import uz.app.Anno.orm.annotations.*;
import uz.app.Anno.orm.exceptions.ValidationException;
import uz.app.Anno.util.Rgx;

@Schema("public")
@Table("Users")
public class User implements IEntity {
    @Id
    @Generated
    @Column("id")
    private long id;
    @Column("login")
    private String login;
    @Column("fullName")
    private String fullName;
    @Column("email")
    private String email;
    @Column("passwordHash")
    private String passwordHash;
    @Column("state")
    private String state;

    private String age;

    public User() { }

    public User(int id, String login, String fullName, String email, String passwordHash, String state) {
        this.id = id;
        this.login = login;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.state = state;
    }

    @Override
    public boolean isValid() {
        if(!Rgx.isEmail(this.email))
            return false;

        if(this.fullName == null || this.fullName.length() == 0)
            return false;

        if(this.login == null || this.login.length() == 0)
            return false;

        if(this.passwordHash == null || this.passwordHash.length() == 0)
            return false;

        if(this.state == null || this.state.length() == 0)
            this.state = "A";

        return true;
    }

    @Override
    public void validate() throws ValidationException {
        if(!Rgx.isEmail(this.email))
            throw new ValidationException("Invalid email", "email");

        if(this.fullName == null || this.fullName.length() == 0)
            throw new ValidationException("Invalid full name: empty", "fullName");

        if(this.login == null || this.login.length() == 0)
            throw new ValidationException("Invalid login: empty", "login");

        if(this.passwordHash == null || this.passwordHash.length() == 0)
            throw new ValidationException("Invalid password hash: empty", "passwordHash");

        if(this.state == null || this.state.length() == 0)
            this.state = "A";
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
</code></pre>

All operations on entities are performed throught <b>Repositories</b>.<br>
<b>Repositories</b> are instantiated through static class <code>RepositoryFactory</code>.<br>
<code>RepositoryFactory</code> must have context <code>OrmContext</code>.<br>
<code>OrmContext</code> can be instantiated by constructor with one argument, consuming the string URI of config file.

Sample:
<pre><code>
OrmContext context = new OrmContext("test.application.context");
RepositoryFactory.setContext(context);
userRepo = RepositoryFactory.getRepository(User.class);
</code></pre>

Configuration file sample:

<b>test.application.context</b>:
<pre><code>
uz.app.Anno.orm.DB_URL=jdbc:postgresql://localhost:5432/postgres
uz.app.Anno.orm.DB_USER=postgres
uz.app.Anno.orm.DB_PASSWORD=123
uz.app.Anno.orm.DB_DEFAULT_SCHEMA=public
uz.app.Anno.orm.DB_CONN_POOL_SIZE = 10
</code></pre>

<h4>Examples of using repositories:</h4>

<h5>Getting all entities:</h5>
<pre><code>
    UserRepository userRepo = new UserRepository();
    User[] users;
    try {
        users = userRepo.getAll();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
</code></pre>

<h5>Getting by Id of entity:</h5>
<pre><code>
    User user;
    try {
        user = userRepo.getById(userId);
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
</code></pre>

<h5>Getting entities by some property:</h5>
<pre><code>
    String login = "foo";
    User[] users;
    try {
        users = userRepo.where("login").like(login).get();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
</code></pre>

<h5>Saving entity to DB:</h5>
<pre><code>
    User user = new User();
    user.login = "foo";
    user.fullName = "Steve Robertson";
    user.state = "A";
    user.email = "steve_robinson@gmail.com";
    try {
        userRepo.save(user);
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
</code></pre>

<h5>Updating entity:</h5>
<pre><code>
    userId = 1;
    User user;
    try {
        user = userRepo.getById(userId);
        user.fullName = "Timotee Schalame";
        userRepo.update(user);
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
</code></pre>

<h5>Deleting entity:</h5>
<pre><code>
    userId = 1;
    try {
        userRepo.delete(userId);
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
</code></pre>

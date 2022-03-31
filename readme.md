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

Includes ORM functionality for PostgreSQL DBMS and easy url-mapping instead of Servlets(one servlet per one path). 

Requirements: 
<ul>
<li>Java 11 or higher </li>
<li>Tomcat 7 or newer, the webapp's web.xml has to be declared conform Servlet 3.0 spec in order to get Tomcat to scan and process the annotations.
    <code>@WebServlet</code> annotation is used in framework.
</li>
<li>If you want to use ORM, then load some Postgres JDBC-driver class in constructor of your service. (Your services load firstly)</li>
<li>Depends on https://github.com/Humo8668/AnnoDBC</li>
</ul>

<h2>Usage</h2>
Minimap:
<ul>
    <li><a href="#services"> Url-mapping services</a></li>
    <li><a href="#orm"> ORM</a></li>
</ul>

<h3 name="service">Services</h3>
Sample of Service (url-mapping): <br>
Create some class extending <code>BaseService</code>. Declare default constructor without arguments. <br>
And then declare some method with arguments <code> HttpServletRequest req, HttpServletResponse res</code> in this order. <br>
The method may return <code>void</code>, doesn't have sense. <br>
Annotate the method with <code>@Route</code> in which pass the route, corresponding to this method throught <code>value</code> and http-method throught <code>method</code>.<br>
By default http-method is <code>GET</code> mtehod.

<pre><code>
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
</code></pre><br>
If you send Http-request <code>GET /sample/echo </code>, you will get json-response: <br>
<pre><code>
{
    "message" : "ECHO... echo... echo..."
}
</code></pre>

<h3 name="orm">ORM</h3>

Database connection parameters will be set throught context parameters in <code>web.xml</code> file:

<pre><code>
&lt;context-param&gt;
    &lt;param-name&gt;DB_USERNAME&lt;/param-name&gt;
    &lt;param-value&gt;postgres&lt;/param-value&gt;
&lt;/context-param&gt;
&lt;context-param&gt;
    &lt;param-name&gt;DB_PASSWORD&lt;/param-name&gt;
    &lt;param-value&gt;123&lt;/param-value&gt;
&lt;/context-param&gt;
&lt;context-param&gt;
    &lt;param-name&gt;DB_URL&lt;/param-name&gt;
    &lt;param-value&gt;jdbc:postgresql://localhost:5432/postgres&lt;/param-value&gt;
&lt;/context-param&gt;
&lt;context-param&gt;
    &lt;param-name&gt;DB_CONN_POOL_SIZE&lt;/param-name&gt;
    &lt;param-value&gt;5&lt;/param-value&gt;
&lt;/context-param&gt;
</code></pre>

Sample of <b>entity</b> binded to table by annotations:<br>
<pre><code>
import uz.app.Anno.orm.*;
import uz.app.Anno.util.*;

@Schema("public")
@Table("Users")
public class User extends BaseEntity {
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
        // your validation logic
        return true;
    }

    @Override
    public void validate() throws AnnoValidationException {
        // your validation logic
        return;
    }
}
</code></pre>

All operations on entities are performed throught <b>Repositories</b>.<br>
Each entity should have their repository. There's sample of repository class: <br>
<pre><code>
public class UserRepository extends Repository<User> {
    public UserRepository() throws Exception{
        /*ATTENTION! Do not remove it. 
        This is necessary due to specification of java regarding generic types*/
        SetTargetEntity(User.class);
    }
}
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

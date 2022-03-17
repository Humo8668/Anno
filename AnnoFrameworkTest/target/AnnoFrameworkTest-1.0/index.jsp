<% 
String userName = "Steve";
String appVersion = "1.0";
%>

<html>
    <body>
        Hello, <%=userName%>.
        <br>
        App version: <%=appVersion%>;
        <br>
        JSP class loader: <%= this.getClass().getClassLoader().toString() %>
    </body>
</html>
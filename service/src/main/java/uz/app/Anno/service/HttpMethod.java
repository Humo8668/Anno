package uz.app.Anno.service;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;

    /*public static HttpMethod valueOf(String str)
    {
        str = str.trim().toLowerCase();

        switch(str)
        {
            case "get":
                return HttpMethod.GET;
            case "post":
                return HttpMethod.POST;
            case "put":
                return HttpMethod.PUT;
            case "delete":
                return HttpMethod.DELETE;
            default:
                return HttpMethod.GET;
        }
    }*/
}

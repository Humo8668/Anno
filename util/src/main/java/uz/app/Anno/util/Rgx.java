package uz.app.Anno.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rgx {
    public static boolean isEmail(String str)
    {
        Pattern pattern = Pattern.compile("^[.\\w]*[@][.\\w]*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isPath(String str)
    {
        Pattern pattern = Pattern.compile("^([\\/]?[\\w]+)*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}

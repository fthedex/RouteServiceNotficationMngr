package com.example.route.routeservicenotficationmngr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mohammed on 11/5/2016.
 */

public class PasswordValidator {
    PasswordValidator(){

    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }
}

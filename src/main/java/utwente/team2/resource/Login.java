package utwente.team2.resource;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import utwente.team2.dao.UserDao;
import utwente.team2.model.User;
import utwente.team2.settings.ApplicationSettings;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;


@Path("/login")
public class Login {

    public static Cookie getCookie(HttpServletRequest request, String find) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(find)) {
                return cookie;
            }
        }

        return null;
    }

    public static Jws<Claims> getTokenClaims(String token) {
        return Jwts.parser().setSigningKey(ApplicationSettings.APP_KEY).parseClaimsJws(token);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showLoginPage(@Context HttpServletResponse response, @Context HttpServletRequest request,
                                     @QueryParam("error") String error, @QueryParam("message") String message) throws IOException {
        if (error != null) {
            if (error.equals("not_authorized") ||
                error.equals("not_activated") ||
                error.equals("reset_token_invalid")) {
                response.addHeader("error", error);
            }
        }

        if (message != null) {
            if (message.equals("reset_success") ||
                message.equals("reset_request_success") ||
                message.equals("registration_success")) {
                response.addHeader("message", message);
            }
        }

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/login.html");

        System.out.println("Req received.");

        return inputStream;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void login(@FormParam("username") String usernameOrEmail, @FormParam("password") String password, @Context HttpServletResponse servletResponse,
                      @Context HttpServletRequest servletRequest) throws IOException {

        System.out.println("Checking credentials: " + usernameOrEmail + " & " + password);

        String username = null;
        User user;
        if (usernameOrEmail.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            username = UserDao.instance.getUserDetailsWithEmail(usernameOrEmail);
            user = UserDao.instance.getUserWithPassword(username, password);
        } else {
            user = UserDao.instance.getUserWithPassword(usernameOrEmail, password);
            username = usernameOrEmail;
        }


        if (!UserDao.instance.isActivated(username) && user != null) {
            System.out.println("account is not activated");
            servletResponse.sendError(402, "account is not activated");
            return;
        }


        if (user != null) {

            ZoneId zoneId = ZoneId.systemDefault();

            Map<String, Object> claims = new HashMap<>();
            claims.put("iss", "runner");
            claims.put("sub", username);
            claims.put("exp", String.valueOf(LocalDateTime.now().plusMinutes(60).atZone(zoneId).toEpochSecond()));
            claims.put("iat", String.valueOf(LocalDateTime.now().atZone(zoneId)));
            claims.put("key", (UserDao.instance.getUsersPassword(username)).substring(0, 5));

            String jws = Jwts.builder().setClaims(claims).signWith(ApplicationSettings.APP_KEY).compact();

            System.out.println(jws);

            Cookie existing = getCookie(servletRequest, "token");

            if (existing != null) {
                System.out.println("Cookie exists.");
                existing.setValue(jws);
                existing.setPath("/");
                existing.setMaxAge(3600000);
                servletResponse.addCookie(existing);
            } else {
                Cookie cookie = new Cookie("token", jws);
                cookie.setPath("/");
                cookie.setMaxAge(3600000);
                servletResponse.addCookie(cookie);
            }

            System.out.println("Redirecting to " + username);
            servletResponse.sendRedirect("profiles/" + username);

        } else {
            System.out.println("User " + username + " does not exist.");
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "not authorised");
        }
    }
}
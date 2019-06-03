package utwente.team2.resource;

import utwente.team2.dao.UserDao;
import utwente.team2.model.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Base64;

@Path("/login")
public class Login {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showLoginPage() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/login.html");

        return inputStream;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void login(@FormParam("username") String username, @FormParam("password") String password, @Context HttpServletResponse servletResponse,
                      @Context HttpServletRequest servletRequest) throws IOException {

        System.out.println("Checking credentials: " + username + " & " + password);

        User user = UserDao.instance.getUserWithPassword(username, password);

        if (user != null) {
                HttpSession session = servletRequest.getSession();
                session.setAttribute("username", username);
                session.setMaxInactiveInterval(600);

                // replace or create a cookie
                Cookie existing = getCookie(servletRequest, "username");

                if(existing != null){
                    System.out.println("Cookie exists.");
                    existing.setValue(username);
                    existing.setPath("/");
                    existing.setMaxAge(600);
                    servletResponse.addCookie(existing);
                } else {
                    Cookie cookie = new Cookie("username", username);
                    cookie.setPath("/");
                    cookie.setMaxAge(600);
                    servletResponse.addCookie(cookie);
                }

                servletResponse.sendRedirect("profiles/" + username);
        } else {
            System.out.println("User " + username + " does not exist.");
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "not authorised");
        }
    }


    public static Cookie getCookie(HttpServletRequest request, String find) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(find)){
                return cookie;
            }
        }

        return null;
    }


    public static String generateRandomBase64Token(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token); //base64 encoding
    }
}
package utwente.team2.resource;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import utwente.team2.dao.UserDao;
import utwente.team2.model.User;

import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Path("/login")
public class Login {

    // TODO insecure!!
    private static final String SECRET = "LZ_FzX6IB-sSeEScwB0XjhQaetivpLf91QzsQAAYnVI";
    private static final byte[] SECRET_BYTES = SECRET.getBytes();
    public static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_BYTES);


    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showLoginPage(@Context HttpServletResponse response, @Context HttpServletRequest request,
                                     @QueryParam("error") String error, @QueryParam("message") String message) throws IOException {

        if (error != null) {
            response.addHeader("error", error);
        }

        if (message != null) {
            response.addHeader("message", message);
        }

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/login.html");

        System.out.println("Req received.");

        return inputStream;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void login(@FormParam("username") String username, @FormParam("password") String password, @Context HttpServletResponse servletResponse,
                      @Context HttpServletRequest servletRequest) throws IOException {

        System.out.println("Checking credentials: " + username + " & " + password);

        User user = UserDao.instance.getUserWithPassword(username, password);

        if (!UserDao.instance.isActivated(username) && user != null) {
            System.out.println("account is not activated");
//            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "account is not activated");
            servletResponse.sendError(402, "account is not activated");
            return;
        }


        if (user != null) {




            // default timezone
            ZoneId zoneId = ZoneId.systemDefault();

            Map<String,Object> claims = new HashMap<>();
            claims.put("iss", "runner");
            claims.put("sub", username);
            claims.put("exp", String.valueOf(LocalDateTime.now().plusMinutes(10).atZone(zoneId).toEpochSecond()));
            claims.put("iat", String.valueOf(LocalDateTime.now().atZone(zoneId)));


            String jws = Jwts.builder().setClaims(claims).signWith(KEY).compact();

            System.out.println(jws);

//             replace or create a cookie
            Cookie existing = getCookie(servletRequest, "token");

                if(existing != null){
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
            // FORBIDDEN TODO 403

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

    public static Jws<Claims> getTokenClaims(String token) {
        return Jwts.parser().setSigningKey(Login.KEY).parseClaimsJws(token);
    }


    public static String generateRandomBase64Token(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token); //base64 encoding
    }
}
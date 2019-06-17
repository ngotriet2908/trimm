package utwente.team2.resource;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import utwente.team2.dao.UserDao;
import utwente.team2.mail.MailAPI;
import utwente.team2.mail.ResetTemplate;
import utwente.team2.model.User;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Path("/password")
public class PasswordReset {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @Context
    SecurityContext securityContext;

    // show password reset page
    @Path("/reset")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showResetRequestPage() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/password_reset_request.html");
        return inputStream;
    }

    @Path("/reset")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void sendResetToken(@FormParam("username") String username, @Context HttpServletResponse servletResponse,
                                  @Context HttpServletRequest servletRequest)  throws AddressException, MessagingException, IOException, NoSuchMethodException, NoSuchMethodError {

        User user = UserDao.instance.getUserDetails(username);

        if (user != null) {
//            String token = UserDao.instance.generateTokenForPasswordReset(username);

            // default timezone
            ZoneId zoneId = ZoneId.systemDefault();

            Map<String, Object> claims = new HashMap<>();
            claims.put("iss", "runner");
            claims.put("sub", username);
            claims.put("exp", String.valueOf(LocalDateTime.now().plusMinutes(2).atZone(zoneId).toEpochSecond())); // TODO 15 min
            claims.put("purpose", "password_reset");
            claims.put("key", (UserDao.instance.getUsersPassword(username)).substring(0, 5));

            String token = Jwts.builder().setClaims(claims).signWith(Login.KEY).compact();

            MailAPI.generateAndSendEmail(ResetTemplate.createResetEmail(username, token), "Reset your password - Runner", user.getEmail());
        }

        // redirect to success page (even if the user does not exist - nobody should know that) TODO later
        servletResponse.sendRedirect("/");
    }


    @Path("/reset/enter") // TODO if the token is invalid, immediately show warning
    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showResetEnterPage() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/password_reset_enter.html");
        return inputStream;
    }


    @Path("/reset/enter")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void resetEnter(@FormParam("token") String token,  @FormParam("password") String password,
                        @Context HttpServletResponse servletResponse,
                        @Context HttpServletRequest servletRequest) throws Exception {

        try {
            String username = Login.getTokenClaims(token).getBody().getSubject();

            String passswordHash = UserDao.instance.getUsersPassword(username);

            if (passswordHash != null) {
                Jws<Claims> jws = Jwts.parser().require("purpose", "password_reset").require("key", passswordHash.substring(0, 5))
                        .setSigningKey(Login.KEY).parseClaimsJws(token);
                System.out.println("Password reset JWT is valid.");

                UserDao.instance.updatePassword(username, password);

                servletResponse.sendRedirect("/runner/login?message=reset_success"); // TODO later also redirect to some success page "password changed. now sign in"
            } else {
                System.out.println("JWT exception.");
                servletResponse.sendError(404, "Invalid token."); // TODO
            }
        } catch (JwtException e) {
            System.out.println("JWT exception.");
            servletResponse.sendError(404, "Invalid token."); // TODO
        }
    }
}

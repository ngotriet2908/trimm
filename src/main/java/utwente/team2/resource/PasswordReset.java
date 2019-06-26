package utwente.team2.resource;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import utwente.team2.dao.UserDao;
import utwente.team2.mail.EmailHtmlTemplate;
import utwente.team2.mail.MailAPI;
import utwente.team2.model.User;
import utwente.team2.settings.ApplicationSettings;

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
                               @Context HttpServletRequest servletRequest) throws MessagingException, IOException, NoSuchMethodException, NoSuchMethodError {

        User user = UserDao.instance.getUserDetails(username);

        if (user != null) {
            ZoneId zoneId = ZoneId.systemDefault();

            Map<String, Object> claims = new HashMap<>();
            claims.put("iss", "runner");
            claims.put("sub", username);
            claims.put("exp", String.valueOf(LocalDateTime.now().plusMinutes(15).atZone(zoneId).toEpochSecond()));
            claims.put("purpose", "password_reset");
            claims.put("key", (UserDao.instance.getUsersPassword(username)).substring(0, 5));

            String token = Jwts.builder().setClaims(claims).signWith(ApplicationSettings.APP_KEY).compact();

            MailAPI.generateAndSendEmail(EmailHtmlTemplate.createEmailHtml(username, token,
                    "You're receiving this email because you requested a password reset for your user account on Runner. If you didn't request a password change, you can just ignore this message.",
                    "RESET YOUR PASSWORD",
                    ApplicationSettings.DOMAIN + "/runner/password/reset/enter?token="), "Reset your password - Runner", user.getEmail());
        }

        servletResponse.sendRedirect("/");
    }


    @Path("/reset/enter") // TODO if the token is invalid, immediately show warning
    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showResetEnterPage() {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream("../../html/password_reset_enter.html");
    }


    @Path("/reset/enter")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void resetEnter(@FormParam("token") String token, @FormParam("password") String password,
                           @Context HttpServletResponse servletResponse,
                           @Context HttpServletRequest servletRequest) throws Exception {

        try {
            String username = Login.getTokenClaims(token).getBody().getSubject();

            String passwordHash = UserDao.instance.getUsersPassword(username);

            if (passwordHash != null) {
                Jws<Claims> jws = Jwts.parser().require("purpose", "password_reset").require("key", passwordHash.substring(0, 5))
                        .setSigningKey(ApplicationSettings.APP_KEY).parseClaimsJws(token);
                System.out.println("Password reset JWT is valid.");

                UserDao.instance.updatePassword(username, password);

                servletResponse.sendRedirect("/runner/login?message=reset_success");
            } else {
                System.out.println("JWT exception.");
                servletResponse.sendError(404, "Invalid token.");
            }
        } catch (JwtException e) {
            System.out.println("JWT exception.");
            servletResponse.sendError(404, "Invalid token.");
        }
    }
}

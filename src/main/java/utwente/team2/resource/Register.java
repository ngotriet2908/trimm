package utwente.team2.resource;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import utwente.team2.dao.UserDao;
import utwente.team2.mail.EmailHtmlTemplate;
import utwente.team2.mail.MailAPI;
import utwente.team2.model.Username;
import utwente.team2.settings.ApplicationSettings;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Path("/register")
public class Register {


    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showLoginPage() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/register.html");

        return inputStream;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void register(@FormParam("username") String username, @FormParam("password") String password,
                         @FormParam("first_name") String firstName, @FormParam("last_name") String lastName,
                         @FormParam("email") String email, @Context HttpServletResponse servletResponse,
                         @Context HttpServletRequest servletRequest) throws IOException, MessagingException {

        // verifying/sanitizing input
        // check if the user already exists
        // email regex from TODO
        if (username.matches("[a-zA-Z_]{2,}") &&
                firstName.matches("[\\p{L}\\s\\-]+") &&
                lastName.matches("[\\p{L}\\s\\-]+") &&
                email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])") &&
                password.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}") &&
                UserDao.instance.getUser(username, "", false) == null) {

            if (UserDao.instance.register(username, firstName, lastName, email, password)) {
                ZoneId zoneId = ZoneId.systemDefault();

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "runner");
                claims.put("sub", username);
                claims.put("exp", String.valueOf(LocalDateTime.now().plusMinutes(1440).atZone(zoneId).toEpochSecond())); // TODO 15 min
                claims.put("purpose", "activate");

                String token = Jwts.builder().setClaims(claims).signWith(ApplicationSettings.APP_KEY).compact();

                MailAPI.generateAndSendEmail(EmailHtmlTemplate.createEmailHtml(username, token,
                        "You're receiving this email because you register an account on Runner.",
                        "ACTIVATE YOUR ACCOUNT",
                        ApplicationSettings.DOMAIN + "/runner/register/activate?token="),
                        "Activate your account", email);

                servletResponse.sendRedirect("/");
            } else {
                // cannot register the user
                System.out.println("400: Invalid data supplied. 1");
                servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid data supplied.");
            }
        } else {
            // if some of the checks fails, respond with failure
            System.out.println("400: Invalid data supplied. 2");
            servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid data supplied.");
        }
    }

    @Path("/username")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Username checkUsernameAvailability(@QueryParam("username") String usernameToCheck, @Context HttpServletResponse servletResponse) {
        if (usernameToCheck == null) {
            return null; // TODO
        }

        Username username = new Username(usernameToCheck);
        username.setExists(UserDao.instance.getUser(username.getUsername(), "", false) != null);

        return username;
    }

    @Path("/activate")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public void showResetRequestPage(@QueryParam("token") String token, @Context HttpServletResponse servletResponse) throws IOException {

        Jws<Claims> jws = Jwts.parser().require("purpose", "activate")
                .setSigningKey(ApplicationSettings.APP_KEY).parseClaimsJws(token);
        System.out.println("Account activation JWT is valid.");
        String username = Login.getTokenClaims(token).getBody().getSubject();


        UserDao.instance.activateAccount(username);

        servletResponse.sendRedirect("/runner/login?message=activate_success");
    }
}

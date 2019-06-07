package utwente.team2.resource;


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
            String token = UserDao.instance.generateTokenForPasswordReset(username);
            MailAPI.generateAndSendEmail(ResetTemplate.createResetEmail(username, token), "Reset your password - Runner", user.getEmail());
        }

        // redirect to success page (even if the user does not exist - nobody should know that) TODO later

        servletResponse.sendRedirect("/");
    }


    @Path("/reset/enter")
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

        if (!UserDao.instance.changePassword(password,token)) {
            servletResponse.sendError(404, "Invalid token."); // TODO
            return;
        }

        servletResponse.sendRedirect("/runner/login"); // TODO later also redirect to some success page "password changed. now sign in"
    }
}

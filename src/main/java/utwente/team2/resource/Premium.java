package utwente.team2.resource;

import utwente.team2.dao.UserDao;
import utwente.team2.filter.Secured;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

@Path("/premium")
@Secured
public class Premium {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @Context
    SecurityContext securityContext;

    @Path("/join")
    @GET
    @Produces(MediaType.TEXT_HTML)
    // save a new profile picture and respond with 200
    public InputStream showSettingsPage(@Context HttpServletResponse servletResponse,
                                        @Context HttpServletRequest servletRequest) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/upgrade.html");

        return inputStream;
    }

    @Path("/join")
    @POST
    public void upgradeToPremium(@Context HttpServletResponse servletResponse,
                                 @Context HttpServletRequest servletRequest) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        // check if user is already a premium user

        boolean upgraded = UserDao.instance.upgradeToPremium(tokenUsername);
        servletResponse.setStatus(204);

        try {
            servletResponse.sendRedirect("/runner/profiles");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}

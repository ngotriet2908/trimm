package utwente.team2.resource;

import utwente.team2.dao.RunDao;
import utwente.team2.dao.UserDao;
import utwente.team2.filter.Secured;
import utwente.team2.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;


@Path("/profiles")
@Secured
public class Profiles {

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest servletRequest;

    @Context
    SecurityContext securityContext;

    @Path("/{username}/picture")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    // save a new profile picture and respond with 200
    public void savePicture(@PathParam("username") String username,
                            @FormParam("picture") String picture, @Context HttpServletResponse servletResponse,
                            @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!tokenUsername.equals(username)) {
            servletResponse.sendRedirect("/runner/login");
        }

        // extract image

        // save to userdao/database

        // respond 200/204

    }


    @Path("/{username}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showProfilePage(@PathParam("username") String username, @Context HttpServletResponse servletResponse) throws IOException {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!tokenUsername.equals(username)) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/profile.html");

        servletResponse.setHeader("Content-Type", "text/html");
        System.out.println("Profile for " + username + " requested.");

        return inputStream;
    }

    @Path("/{username}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public User login(@PathParam("username") String username, @Context HttpServletResponse servletResponse,
                      @Context HttpHeaders headers) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!tokenUsername.equals(username)) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        if (UserDao.instance.getUser(username, null, false) == null) {
            servletResponse.sendError(404, "Page not found.");
            return null;
        } else {
            User user = new User();
            UserDao.instance.getUserDetails(username, user);
            RunDao.instance.getUserTotalStats(username, user);
            RunDao.instance.getUserRunsOverview(username, user);

            System.out.println("Sent profile info back to " + username);

            servletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            servletResponse.setHeader("Pragma", "no-cache");
            servletResponse.setDateHeader("Expires", 0);
            servletResponse.setHeader("Content-Type", "application/json");

            return user;
        }
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    public void profileRedirect(@Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest) throws IOException {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        servletResponse.sendRedirect("profiles/" +tokenUsername);
        // TODO what if the token expires during the session?
    }
}

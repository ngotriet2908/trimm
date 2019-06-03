package utwente.team2.resource;

import utwente.team2.dao.UserDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;

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


    // registers the user and logs in
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void register(@FormParam("username") String username, @FormParam("password") String password,
                         @FormParam("first_name") String firstName, @FormParam("last_name") String lastName,
                         @FormParam("email") String email, @Context HttpServletResponse servletResponse,
                      @Context HttpServletRequest servletRequest) throws IOException {

        // verification/sanitizing input TODO
        // check if the user already exists

        if (true) { // if all the checks are passed, then register
            if (UserDao.instance.register(username, firstName, lastName, email, password)) {

                servletResponse.sendRedirect("/");

                // show message "registered!" in client? TODO
            } else {
                // cannot register the user
                System.out.println("400: Invalid data supplied.");
                servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid data supplied.");
            }
        } else {
            // if some of the checks fails, respond with failure
            System.out.println("400: Invalid data supplied.");
            servletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid data supplied.");
        }
    }
}

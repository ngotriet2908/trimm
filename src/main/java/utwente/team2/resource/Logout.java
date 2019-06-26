package utwente.team2.resource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/logout")
public class Logout {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public void logout(@Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest) throws IOException {
        Cookie[] cookies = servletRequest.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    System.out.println("cookie: " + cookie.getValue());

                    cookie.setMaxAge(0);
                    cookie.setValue(null);
                    cookie.setPath("/");
                    servletResponse.addCookie(cookie);
                }
            }
        }

        servletResponse.sendRedirect("/");
    }
}

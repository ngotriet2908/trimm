package utwente.team2.resource;

import utwente.team2.dao.RunDao;
import utwente.team2.dao.UserDao;
import utwente.team2.filter.Secured;
import utwente.team2.model.Run;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;

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

        boolean upgraded = UserDao.instance.upgradeToPremium(tokenUsername);

        if (upgraded) {
            UserDao.instance.insertFavoriteLayout(tokenUsername);

            List<Run> runs = RunDao.instance.getUserRunsList(tokenUsername);

            for(int i = 0; i < runs.size(); i++) {
                RunDao.instance.insertLayout(runs.get(i).getId());
            }
        }


        System.out.println("Upgrade to pre: " + upgraded);

        servletResponse.setStatus(204);

        try {
            servletResponse.sendRedirect("/runner/profiles");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}

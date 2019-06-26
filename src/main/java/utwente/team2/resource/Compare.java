package utwente.team2.resource;


import utwente.team2.dao.RunDao;
import utwente.team2.dao.StepDao;
import utwente.team2.dao.UserDao;
import utwente.team2.filter.Secured;
import utwente.team2.model.CompareGraph;
import utwente.team2.model.GraphPoints;
import utwente.team2.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Path("/compare")
@Secured
public class Compare {
    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest servletRequest;

    @Context
    HttpServletResponse servletResponse;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showCompare(@Context HttpServletResponse response, @Context HttpServletRequest request) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (UserDao.instance.isPremiumUser(tokenUsername)) {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("../../html/compare.html");
            return inputStream;
        } else {
            premiumFeatureAccessForbidden();
            return null;
        }
    }

    public void premiumFeatureAccessForbidden() {
        try {
            servletResponse.sendError(403, "This feature is only accessible for premium users.");
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    @Path("/graph/{lines}/{indicator}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CompareGraph> showCompareGraph(@Context HttpServletResponse response,
                                               @Context HttpServletRequest request,
                                               @PathParam("lines") String lines,
                                               @PathParam("indicator") String indicator) throws IOException {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!UserDao.instance.isPremiumUser(tokenUsername)) {
            premiumFeatureAccessForbidden();
            return null;
        }

        String[] linesArray = lines.split("x");

        List<CompareGraph> compareGraphs = new ArrayList<>();

        for (String s : linesArray) {
            GraphPoints gp = StepDao.instance.getStepsWithNumberOfSteps(s, 100, indicator);

            if (gp != null) {
                compareGraphs.add(new CompareGraph(s,
                        gp.getLeft()));
            }
        }

        return compareGraphs;
    }

    @Path("/select")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User getLayoutName(@Context HttpServletResponse servletResponse,
                              @Context HttpServletRequest servletRequest) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!UserDao.instance.isPremiumUser(tokenUsername)) {
            premiumFeatureAccessForbidden();
            return null;
        }

        User user = new User();
        RunDao.instance.getUserRunsList(tokenUsername, user);
        return user;
    }

}

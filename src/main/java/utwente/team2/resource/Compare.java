package utwente.team2.resource;


import utwente.team2.dao.RunDao;
import utwente.team2.dao.StepDao;
import utwente.team2.filter.Secured;
import utwente.team2.model.CompareGraph;
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
    public InputStream showCompare(@Context HttpServletResponse response, @Context HttpServletRequest request) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/compare.html");

        System.out.println("show compare");
        return inputStream;
    }

    @Path("/graph/{lines}/{indicator}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CompareGraph> showCompareGraph(@Context HttpServletResponse response,
                                                     @Context HttpServletRequest request,
                                                     @PathParam("lines") String lines,
                                                     @PathParam("indicator") String indicator) throws IOException {

        String[] linesArray = lines.split("x");

        List<CompareGraph> compareGraphs = new ArrayList<>();

        for(int i = 0; i < linesArray.length; i++) {
            compareGraphs.add(new CompareGraph(linesArray[i],
                    StepDao.instance.getStepsWithNumberOfSteps(linesArray[i], 100, indicator).getLeft()));
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

        User user = new User();

        RunDao.instance.getUserRunsList(tokenUsername, user);

        return user;
    }

}

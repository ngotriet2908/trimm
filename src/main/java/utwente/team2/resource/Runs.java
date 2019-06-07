package utwente.team2.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utwente.team2.dao.RunDao;
import utwente.team2.dao.StepDao;
import utwente.team2.filter.Secured;
import utwente.team2.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.List;


@Secured
@Path("/runs")
public class Runs {

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest servletRequest;

    @Context
    HttpServletResponse servletResponse;

    @Context
    SecurityContext securityContext;

    @Path("/{run_id}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showRunPage(@PathParam("run_id") String run_id) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!RunDao.instance.isUsersRun(tokenUsername, Integer.parseInt(run_id))) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/dashboard.html");

        return inputStream;
    }

    @Path("/{run_id}/layout")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveLayout(@PathParam("run_id") String run_id, @Context HttpServletResponse servletResponse,
                      @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!RunDao.instance.isUsersRun(tokenUsername, Integer.parseInt(run_id))) {
            servletResponse.sendRedirect("/runner/login");
            return;
        }

        BufferedReader br = null;

        try {
            InputStreamReader reader = new InputStreamReader(
                    servletRequest.getInputStream());
            br = new BufferedReader(reader);

            String layout = br.readLine();

            RunDao.instance.saveLayout(Integer.valueOf(run_id), layout);

            servletResponse.setStatus(204);
        } catch (IOException ex) {
//            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
//                    Logger.getLogger(Utils.class.getName()).log(Level.WARNING, null, ex);
                    ex.printStackTrace();
                }
            }
        }
    }


    // returns layout with data
    @Path("/{run_id}/layout")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LayoutData getLayout(@PathParam("run_id") String run_id, @Context HttpServletResponse servletResponse,
                      @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!RunDao.instance.isUsersRun(tokenUsername, Integer.parseInt(run_id))) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        String layout = RunDao.instance.getLayout(Integer.valueOf(run_id));

        ObjectMapper mapper = new ObjectMapper();

        if (layout == null) {
            return new LayoutData();
        } else {
            JsonNode jsonNode = mapper.readTree(layout);
            LayoutData ld = new LayoutData();


            int count = Integer.valueOf(jsonNode.get("count").toString());

            for (int i = 0; i < count; i++) {

                String typeName = jsonNode.get("layout").get(i).get("typeName").textValue();

                System.out.println(typeName.equals("individual"));

                if (typeName.equals("individual")) {
                    Indicator indicator = StepDao.instance.getFunction(jsonNode.get("layout").get(i).get("indicatorName").textValue(), Integer.valueOf(run_id));
                    ld.getCards().add(indicator);
                } else if (typeName.equals("graph")) {
                    GraphPoints gp = StepDao.instance.getStepsWithPara(run_id, 50, jsonNode.get("layout").get(i).get("indicatorName").textValue());
                    ld.getCards().add(gp);
                }
            }

            return ld;
        }
    }


    @Path("/{run_id}/indicator/{variable}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Indicator showRunPage(@PathParam("run_id") String run_id, @PathParam("variable") String variable) {
        Indicator res = StepDao.instance.getFunction(variable,Integer.parseInt(run_id));
        return res;
    }

    @Path("/{run_id}/graph/{numberOfStep}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Step> getListOfStep(@PathParam("run_id") String run_id, @PathParam("numberOfStep") String numberOfStep) {
        List<Step> res = StepDao.instance.getSteps(run_id, Integer.parseInt(numberOfStep));
        return res;
    }

    @Path("/{run_id}/graph/{numberOfStep}/{indicator}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GraphPoints getListOfStepWithPara(@PathParam("run_id") String run_id, @PathParam("numberOfStep") String numberOfStep, @PathParam("indicator") String indicator) {
        GraphPoints res = StepDao.instance.getStepsWithPara(run_id, Integer.parseInt(numberOfStep), indicator);
        return res;
    }

    @Path("/{run_id}/info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Run showRunInfo(@PathParam("run_id") String run_id) {
        return RunDao.instance.getRunsOverviewByID(Integer.parseInt(run_id));
    }
}

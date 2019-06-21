package utwente.team2.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utwente.team2.dao.RunDao;
import utwente.team2.dao.StepDao;
import utwente.team2.dao.UserDao;
import utwente.team2.filter.Secured;
import utwente.team2.infographic.InfographicImageGenerator;
import utwente.team2.infographic.InfographicTemplate;
import utwente.team2.mail.MailAPI;
import utwente.team2.model.*;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
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
import java.math.BigDecimal;
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
        InputStream inputStream;

        if (UserDao.instance.isPremiumUser(tokenUsername)) {
            inputStream = classLoader.getResourceAsStream("../../html/dashboard_premium.html");
        } else {
            inputStream = classLoader.getResourceAsStream("../../html/dashboard.html");
        }

        return inputStream;
    }


    //TODO change to post
    @Path("/{run_id}/layout/reset")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LayoutData setDefaultLayout(@PathParam("run_id") String run_id, @Context HttpServletResponse servletResponse,
                                @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!RunDao.instance.isUsersRun(tokenUsername, Integer.parseInt(run_id))) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        String defaultlayout = RunDao.instance.getDefaultLayout(Integer.valueOf(run_id));

        RunDao.instance.saveLayout(Integer.parseInt(run_id), defaultlayout);
        return null;
    }

    @Path("/{run_id}/rename_layout/{layout_id}/{name}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void RenameLayout(@PathParam("run_id") String run_id,
                             @PathParam("layout_id") String layout_id,
                             @PathParam("name") String name,
                             @Context HttpServletResponse servletResponse,
                             @Context HttpServletRequest servletRequest) throws IOException {

        if (!RunDao.instance.saveLayoutName(Integer.parseInt(run_id), Integer.parseInt(layout_id), name)){
            //TODO put something when replace with the same name
        }
    }


    @Path("/{run_id}/layout/name")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LayoutOptions getLayoutName(@PathParam("run_id") String run_id) {
        return RunDao.instance.getLayoutName(Integer.parseInt(run_id));
    }

    @Path("/{run_id}/current_layout/{current_layout}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public void updateCurrentLayout(@PathParam("run_id") String run_id, @PathParam("current_layout") String current_layout) {

        if (!RunDao.instance.saveCurrentLayout(Integer.parseInt(run_id), Integer.parseInt(current_layout))) {
            //TODO do something, only error when the update with the same thing as before
        }
    }

    @Path("/{run_id}/save_favorite/{layout_id}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveFavoriteLayout(@PathParam("layout_id") String layout_id,
                                   @PathParam("run_id") String run_id,
                                   @Context HttpServletResponse servletResponse,
                                   @Context HttpServletRequest servletRequest) throws IOException {
        if (!UserDao.instance.saveFavoriteLayout(Integer.parseInt(layout_id),Integer.parseInt(run_id))){
            //TODO put something when replace with the same name
        }
    }
    @Path("/{run_id}/load_favorite/{layout_id}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void loadFavoriteLayout(
                                   @PathParam("layout_id") String layout_id,
                                   @PathParam("run_id") String run_id,
                                   @Context HttpServletResponse servletResponse,
                                   @Context HttpServletRequest servletRequest) throws IOException {
        if (!UserDao.instance.loadFavoriteLayout(Integer.parseInt(layout_id), Integer.parseInt(run_id))){
            //TODO put something when replace with the same name
        }
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
            ex.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
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
                    String indicatorName = jsonNode.get("layout").get(i).get("indicatorName").textValue();

                    GraphPoints gp;

                    if (indicatorName.equals("speed")) {
                        BigDecimal stepDistance = RunDao.instance.getSpeed(Integer.parseInt(run_id));
                        gp = StepDao.instance.getTime(run_id, 51);

                        if (gp != null) {
                            gp.reprocessSpeed(stepDistance);
                        }
                    } else {
                        gp = StepDao.instance.getStepsWithPara(run_id, 50, indicatorName);
                    }
                    ld.getCards().add(gp);
                } else if (typeName.equals("distribution")) {
                    String indicatorName = jsonNode.get("layout").get(i).get("indicatorName").textValue();
                    GraphPoints gp;

                    if (indicatorName.equals("speed")) {
                        BigDecimal stepDistance = RunDao.instance.getSpeed(Integer.parseInt(run_id));
                        gp = StepDao.instance.getAllTime(run_id);
                        gp.reprocessSpeed(stepDistance);

                        Distribution distribution = new Distribution(gp.getLeft(), gp.getName());
                        distribution.getSpeedDistribution();
                    } else {
                        gp = StepDao.instance.getAllSteps(run_id, jsonNode.get("layout").get(i).get("indicatorName").textValue());
                    }

                    if (gp != null) {
                        Distribution distribution = new Distribution(gp.getLeft(), gp.getName());
                        distribution.getDistribution();

                        ld.getCards().add(distribution);
                    }
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

    @Path("/{run_id}/graph/{numberOfSteps}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Step> getListOfStep(@PathParam("run_id") String run_id, @PathParam("numberOfSteps") String numberOfStep) {
        List<Step> res = StepDao.instance.getSteps(run_id, Integer.parseInt(numberOfStep));
        return res;
    }

    @Path("/{run_id}/graph/{numberOfSteps}/{indicator}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GraphPoints getListOfStepWithPara(@PathParam("run_id") String run_id, @PathParam("numberOfSteps") String numberOfStep, @PathParam("indicator") String indicator) {

        if (indicator.equals("speed")) {
            BigDecimal stepDistance = RunDao.instance.getSpeed(Integer.parseInt(run_id));
            GraphPoints gp = StepDao.instance.getTime(run_id, Integer.parseInt(numberOfStep) + 1);
            gp.reprocessSpeed(stepDistance);
            return gp;
        }

        GraphPoints res = StepDao.instance.getStepsWithPara(run_id, Integer.parseInt(numberOfStep), indicator);
        return res;
    }

    @Path("/{run_id}/graph/{numberOfSteps}/{indicator}/{startP}/{endP}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GraphPoints getListOfStepWithParaAndRange(@PathParam("run_id") String run_id, @PathParam("numberOfSteps") String numberOfStep,
                                                     @PathParam("indicator") String indicator, @PathParam("startP") String startP, @PathParam("endP") String endP) {
        GraphPoints gp = StepDao.instance.getStepsWithParaAndRange(run_id, Integer.parseInt(numberOfStep), indicator, Integer.parseInt(startP), Integer.parseInt(endP));
        return gp;
    }

    @Path("/{run_id}/distribution/{indicator}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Distribution getDistribution(@PathParam("run_id") String run_id, @PathParam("indicator") String indicator) {

        if (indicator.equals("speed")) {
            BigDecimal stepDistance = RunDao.instance.getSpeed(Integer.parseInt(run_id));
            GraphPoints gp = StepDao.instance.getAllTime(run_id);
            gp.reprocessSpeed(stepDistance);

            Distribution distribution = new Distribution(gp.getLeft(), gp.getName());
            distribution.getSpeedDistribution();

            return distribution;
        }

        GraphPoints steps = StepDao.instance.getAllSteps(run_id, indicator);

        Distribution distribution = new Distribution(steps.getLeft(), steps.getName());
        distribution.getDistribution();
        return distribution;
    }



    @Path("/{run_id}/info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Run showRunInfo(@PathParam("run_id") String run_id) {
        return RunDao.instance.getRunsOverviewByID(Integer.parseInt(run_id));
    }

    @Path("/{run_id}/infographic/email")
    @GET
    public String sentToEmail(@PathParam("run_id") String run_id, @QueryParam("email") String email) {

        Run run = RunDao.instance.getRunsOverviewByID(Integer.parseInt(run_id));

        if (run != null) {
            InfographicImageGenerator infographicImageGenerator = new InfographicImageGenerator(
                    run.getName(),
                    run.getDate().toString(),
                    run.getShoes(),
                    run.getDuration().toString(),
                    run.getDistance().toString(),
                    run.getSteps().toString());
            try {
                System.out.println("here");
                MailAPI.generateAndSendEmailWithAttachtMent("hello", "Infographic for " + run.getName() + " on " + run.getDate().toString() + ".   ",
                        email, infographicImageGenerator.generate());
            } catch (AddressException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            try {
                servletResponse.sendError(404, "Page does not exist.");
            } catch (IOException ie) {
                ie.printStackTrace();
            }

            return null;
        }
    }


    @Path("/{run_id}/infographic/browser")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String prepareInfographic(@PathParam("run_id") String run_id) {

        Principal principal = securityContext.getUserPrincipal();

        Run run = RunDao.instance.getRunsOverviewByID(Integer.parseInt(run_id));

        if (run != null) {
            return InfographicTemplate.createInfographic(run.getName(), run.getDate().toString(), run.getDistance().toString(), run.getDuration().toString(), run.getShoes(), run.getSteps().toString());
        } else {
            try {
                servletResponse.sendError(404, "Page does not exist.");
            } catch (IOException ie) {
                ie.printStackTrace();
            }

            return null;
        }
    }
}

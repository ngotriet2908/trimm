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


    @Path("/{run_id}/layout/reset")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public LayoutData setDefaultLayout(@PathParam("run_id") String runId, @Context HttpServletResponse servletResponse,
                                       @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!RunDao.instance.isUsersRun(tokenUsername, Integer.parseInt(runId))) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        String defaultLayout = RunDao.instance.getDefaultLayout(Integer.valueOf(runId));

        RunDao.instance.saveLayout(Integer.parseInt(runId), defaultLayout);

        return null;
    }


    public void premiumFeatureAccessForbidden() {
        try {
            servletResponse.sendError(403, "This feature is only accessible for premium users.");
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }


    @Path("/{run_id}/rename_layout/{layout_id}/{name}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void RenameLayout(@PathParam("run_id") String run_id,
                             @PathParam("layout_id") String layout_id,
                             @PathParam("name") String name,
                             @Context HttpServletResponse servletResponse,
                             @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (UserDao.instance.isPremiumUser(tokenUsername)) {
            if (!RunDao.instance.saveLayoutName(Integer.parseInt(run_id), Integer.parseInt(layout_id), name)) {
                //TODO put something when replace with the same name
            }
        } else {
            premiumFeatureAccessForbidden();
        }
    }

    @Path("/{run_id}/layout/name")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LayoutOptions getLayoutName(@PathParam("run_id") String run_id) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (UserDao.instance.isPremiumUser(tokenUsername)) {
            return RunDao.instance.getLayoutName(Integer.parseInt(run_id));
        } else {
            premiumFeatureAccessForbidden();
            return null;
        }
    }

    @Path("/{run_id}/current_layout/{current_layout}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public void updateCurrentLayout(@PathParam("run_id") String run_id, @PathParam("current_layout") String current_layout) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (UserDao.instance.isPremiumUser(tokenUsername)) {
            if (!RunDao.instance.saveCurrentLayout(Integer.parseInt(run_id), Integer.parseInt(current_layout))) {
                //TODO do something, only error when the update with the same thing as before
            }
        } else {
            premiumFeatureAccessForbidden();
        }
    }


    @Path("/{run_id}/save_favorite/{layout_id}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveFavoriteLayout(@PathParam("layout_id") String layout_id,
                                   @PathParam("run_id") String run_id,
                                   @Context HttpServletResponse servletResponse,
                                   @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (UserDao.instance.isPremiumUser(tokenUsername)) {
            if (!UserDao.instance.saveFavoriteLayout(Integer.parseInt(layout_id), Integer.parseInt(run_id))) {
                //TODO put something when replace with the same name
            }
        } else {
            premiumFeatureAccessForbidden();
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
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (UserDao.instance.isPremiumUser(tokenUsername)) {
            if (!UserDao.instance.loadFavoriteLayout(Integer.parseInt(layout_id), Integer.parseInt(run_id))) {
                //TODO put something when replace with the same name
            }
        } else {
            premiumFeatureAccessForbidden();
        }
    }


    @Path("/{run_id}/layout")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveLayout(@PathParam("run_id") String runId, @Context HttpServletResponse servletResponse,
                           @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!RunDao.instance.isUsersRun(tokenUsername, Integer.parseInt(runId))) {
            servletResponse.sendRedirect("/runner/login");
            return;
        }

        System.out.println("Saving layout. Username: " + tokenUsername + ", runId: " + runId);

        BufferedReader br = null;

        try {
            InputStreamReader reader = new InputStreamReader(
                    servletRequest.getInputStream());
            br = new BufferedReader(reader);

            String layout = br.readLine();

            System.out.println("Layout: " + layout);

            RunDao.instance.saveLayout(Integer.valueOf(runId), layout);

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
    public LayoutData getLayout(@PathParam("run_id") String runId, @Context HttpServletResponse servletResponse,
                                @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!RunDao.instance.isUsersRun(tokenUsername, Integer.parseInt(runId))) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        String layout = RunDao.instance.getLayout(Integer.valueOf(runId));
        ObjectMapper mapper = new ObjectMapper();

        if (layout == null) {
            return new LayoutData();
        } else {
            JsonNode jsonNode = mapper.readTree(layout);
            LayoutData ld = new LayoutData();

            int count = Integer.valueOf(jsonNode.get("count").toString());

            for (int i = 0; i < count; i++) {

                String typeName = jsonNode.get("layout").get(i).get("typeName").textValue();

                if (typeName.equals("individual")) {
                    Individual individual = getIndividual(runId, jsonNode.get("layout").get(i).get("indicatorName").textValue());
                    ld.getCards().add(individual);
                } else if (typeName.equals("graph")) {
                    String indicator = jsonNode.get("layout").get(i).get("indicatorName").textValue();
                    GraphPoints gp = prepareGraphWithNumberOfPoints(runId, indicator, 50);
                    ld.getCards().add(gp);
                } else if (typeName.equals("distribution")) {
                    String indicator = jsonNode.get("layout").get(i).get("indicatorName").textValue();
                    Distribution distribution = prepareDistribution(runId, indicator);
                    ld.getCards().add(distribution);
                } else if (typeName.equals("note")) {
                    Note note = prepareNote(runId);
                    ld.getCards().add(note);
                }
            }

            return ld;
        }
    }

    public Individual prepareIndividual(String runId, String variable) {
        return StepDao.instance.getIndividual(variable, Integer.parseInt(runId));
    }

    public GraphPoints prepareGraphWithNumberOfPoints(String runId, String indicator, int numberOfSteps) {
        if (indicator.equals("speed")) {
            BigDecimal stepLength = RunDao.instance.getStepLength(Integer.parseInt(runId));

            GraphPoints gp = StepDao.instance.getStepsAndTime(runId, numberOfSteps + 1, true);
            gp.calculateSpeed(stepLength);
            return gp;
        }

        return StepDao.instance.getStepsWithNumberOfSteps(runId, numberOfSteps, indicator);
    }

    public Distribution prepareDistribution(String runId, String indicator) {
        if (indicator.equals("speed")) {
            BigDecimal stepDistance = RunDao.instance.getStepLength(Integer.parseInt(runId));
            GraphPoints gp = StepDao.instance.getStepsAndTime(runId, 0, false);
            gp.calculateSpeed(stepDistance);

            Distribution distribution = new Distribution(gp.getLeft(), gp.getName());
            distribution.getDistribution(indicator);

            return distribution;
        }

        GraphPoints steps = StepDao.instance.getAllSteps(runId, indicator);

        Distribution distribution = new Distribution(steps.getLeft(), steps.getName());
        distribution.getDistribution(indicator);
        return distribution;
    }

    public Note prepareNote(String runId) {
        Note note = RunDao.instance.getNote(Integer.valueOf(runId));
        System.out.println("Preparing note.");
        System.out.println("Note text: " + note.getText());
        return note;
    }


    @Path("/{run_id}/individual/{variable}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Individual getIndividual(@PathParam("run_id") String runId, @PathParam("variable") String variable) {
        return prepareIndividual(runId, variable);
    }

    @Path("/{run_id}/graph/{numberOfSteps}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Step> getListOfSteps(@PathParam("run_id") String runId, @PathParam("numberOfSteps") String numberOfSteps) {
        return StepDao.instance.getSteps(runId, Integer.parseInt(numberOfSteps));
    }

    @Path("/{run_id}/graph/{numberOfSteps}/{indicator}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GraphPoints getGraphWithNumberOfPoints(@PathParam("run_id") String runId, @PathParam("numberOfSteps") String numberOfSteps, @PathParam("indicator") String indicator) {
        return prepareGraphWithNumberOfPoints(runId, indicator, Integer.valueOf(numberOfSteps));
    }

    @Path("/{run_id}/graph/{numberOfSteps}/{indicator}/{startP}/{endP}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GraphPoints getGraphWithNumberOfPointsAndRange(@PathParam("run_id") String runId, @PathParam("numberOfSteps") String numberOfSteps,
                                                          @PathParam("indicator") String indicator, @PathParam("startP") String startP, @PathParam("endP") String endP) {
        return StepDao.instance.getStepsWithNumberOfStepsAndRange(runId, Integer.parseInt(numberOfSteps), indicator, Integer.parseInt(startP), Integer.parseInt(endP));
    }

    @Path("/{run_id}/distribution/{indicator}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Distribution getDistribution(@PathParam("run_id") String runId, @PathParam("indicator") String indicator) {
        return prepareDistribution(runId, indicator);
    }

    @Path("/{run_id}/note")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Note getNote(@PathParam("run_id") String runId) {
        return prepareNote(runId);
    }

    @Path("/{run_id}/note")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateNote(@PathParam("run_id") String runId, Note note) {
        System.out.println("Updating note.");
        System.out.println("Note's new text: " + note.getText());

        RunDao.instance.saveNote(Integer.valueOf(runId), note.getText());
    }

    @Path("/{run_id}/info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Run showRunInfo(@PathParam("run_id") String runId) {
        return RunDao.instance.getRun(Integer.parseInt(runId));
    }

    @Path("/{run_id}/infographic/email")
    @GET
    public String sentToEmail(@PathParam("run_id") String runId, @QueryParam("email") String email) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!UserDao.instance.isPremiumUser(tokenUsername)) {
            premiumFeatureAccessForbidden();
            return null;
        }

        Run run = RunDao.instance.getRun(Integer.parseInt(runId));

        if (run != null) {
            InfographicImageGenerator infographicImageGenerator = new InfographicImageGenerator(
                    run.getName(), run.getDate().toString(), run.getShoes(), run.getDuration().toString(),
                    run.getDistance().toString(), run.getSteps().toString());
            try {
                MailAPI.generateAndSendEmailWithAttachment("hello", "Infographic for " +
                                run.getName() + " on " + run.getDate().toString() + ".   ",
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
    public String prepareInfographic(@PathParam("run_id") String runId) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!UserDao.instance.isPremiumUser(tokenUsername)) {
            premiumFeatureAccessForbidden();
            return null;
        }

        Run run = RunDao.instance.getRun(Integer.parseInt(runId));

        if (run != null) {
            return InfographicTemplate.createInfographic(run.getName(), run.getDate().toString(),
                    run.getDistance().toString(), run.getDuration().toString(), run.getShoes(), run.getSteps().toString());
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

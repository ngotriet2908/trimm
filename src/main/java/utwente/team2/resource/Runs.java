package utwente.team2.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utwente.team2.dao.RunDao;
import utwente.team2.dao.StepDao;
import utwente.team2.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Path("/runs")
public class Runs {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @Path("/{run_id}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showRunPage(@PathParam("run_id") String run_id) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/dashboard.html");

        return inputStream;
    }

    @Path("/{run_id}/layout")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveLayout(@PathParam("run_id") String run_id, @Context HttpServletResponse servletResponse,
                      @Context HttpServletRequest servletRequest) throws IOException {

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

        String layout = RunDao.instance.getLayout(Integer.valueOf(run_id));

        ObjectMapper mapper = new ObjectMapper();

        if (layout == null) {
            return new LayoutData();
        } else {

//            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            JsonNode jsonNode = mapper.readTree(layout);

//            List<LayoutElement> myObjects = Arrays.asList(mapper.readValue(layout, LayoutElement[].class));

            LayoutData ld = new LayoutData();

//            while (jsonNode.elements().hasNext()) {
//
//                JsonNode json = jsonNode.elements().next();
//
//                System.out.println(json.get("typeName"));
//
//                if (json.get("typeName").toString().equals("individual")) {
//                    Indicator indicator = StepDao.instance.getFunction(json.get("indicatorName").toString(), Integer.valueOf(run_id));
//                    ld.getCards().add(indicator);
//
//                    System.out.println(indicator.getMeaning());
//                }
//            }
//
//            return ld;

//            System.out.println(jsonNode.elements().next());
//            System.out.println(jsonNode.elements().next());
//            System.out.println(jsonNode.elements().next());
//            System.out.println(jsonNode.elements().next());

//            System.out.println(jsonNode.elements().ne);


            int count = Integer.valueOf(jsonNode.get("count").toString());

            for (int i = 0; i < count; i++) {

                String typeName = jsonNode.get("layout").get(i).get("typeName").textValue();

                System.out.println(typeName.equals("individual"));

                if (typeName.equals("individual")) {
                    Indicator indicator = StepDao.instance.getFunction(jsonNode.get("layout").get(i).get("indicatorName").textValue(), Integer.valueOf(run_id));

//                    System.out.println(indicator.getMeaning());
                    ld.getCards().add(indicator);
                } else if (typeName.equals("graph")) {
                    GraphPoints gp = StepDao.instance.getStepsWithPara(run_id, 50, jsonNode.get("layout").get(i).get("indicatorName").textValue());

//                    System.out.println(gp.getStep_no());
                    ld.getCards().add(gp);
                }
            }

            return ld;
        }
    }

//    // TODO BADDDDDDDD
//    @Path("/{run_id}/layout/data")
//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    public LayoutData getLayoutData(@PathParam("run_id") String run_id, @Context HttpServletResponse servletResponse,
//                                    @Context HttpServletRequest servletRequest) throws IOException {
//
//        System.out.println("Request for data arrived!");
//
//        String layout = RunDao.instance.getLayout(Integer.valueOf(run_id));
//
//        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
//
//        for (Map.Entry entry: parameterMap.entrySet()) {
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        }
//
//
//        return null;
//
//    }


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

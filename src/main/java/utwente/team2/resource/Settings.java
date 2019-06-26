package utwente.team2.resource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import utwente.team2.dao.UserDao;
import utwente.team2.filter.Secured;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Path("/settings")
@Secured
public class Settings {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showSettingsPage(@Context HttpServletResponse servletResponse,
                                        @Context HttpServletRequest servletRequest) {

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("../../html/profile_management.html");

        return inputStream;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void updateProfile(@FormParam("firstname") String firstname, @FormParam("lastname") String lastname, @Context HttpHeaders headers, @Context HttpServletResponse servletResponse) {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        UserDao.instance.updateProfile(tokenUsername, firstname, lastname);
    }

    @Path("/link_strava")
    @GET
    public void getResponseFromStrava(@Context HttpServletResponse servletResponse,
                                      @Context HttpServletRequest servletRequest, @QueryParam("code") String code) {
        System.out.println("Code from strava: " + code);

        String targetURL = "https://www.strava.com/oauth/token";

        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(targetURL);

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<>(4);
            params.add(new BasicNameValuePair("client_id", "35158"));
            params.add(new BasicNameValuePair("client_secret", "c235220d017d9d2d06162c98f7847bedef17e962"));
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();


            if (entity != null) {
                try (InputStream instream = entity.getContent()) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
                        String line;

                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    JSONObject responseJson = new JSONObject(sb.toString());

                    System.out.println("Access Token: " + responseJson.getString("access_token"));
                    System.out.println("Refresh Token: " + responseJson.getString("refresh_token"));
                    System.out.println("Expires at: " + responseJson.getBigDecimal("expires_at"));
                }
            }

            servletResponse.sendRedirect("/runner/profiles");
        } catch (UnsupportedEncodingException ue) {
            ue.printStackTrace();
        } catch (ClientProtocolException ce) {
            ce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
}

package utwente.team2.resource;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import utwente.team2.dao.RunDao;
import utwente.team2.dao.UserDao;
import utwente.team2.filter.Secured;
import utwente.team2.model.FavoriteOptions;
import utwente.team2.model.PieChart;
import utwente.team2.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Base64;
import java.util.List;


@Path("/profiles")
@Secured
public class Profiles {

    @Context
    UriInfo uriInfo;

    @Context
    HttpServletRequest servletRequest;

    @Context
    SecurityContext securityContext;

    @Path("/{username}/picture")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    public void savePicture(@PathParam("username") String username,
                            @FormDataParam("picture") InputStream picture,
                            @FormDataParam("picture") FormDataContentDisposition pictureInfo,
                            @FormDataParam("picture") FormDataBodyPart body,
                            @Context HttpServletResponse servletResponse,
                            @Context HttpServletRequest servletRequest) throws IOException {

        if (body.getMediaType().toString().equals("image/jpeg") ||
                body.getMediaType().toString().equals("image/jpg") ||
                body.getMediaType().toString().equals("image/png")) {
            Principal principal = securityContext.getUserPrincipal();
            String tokenUsername = principal.getName();

            if (!tokenUsername.equals(username)) {
                servletResponse.sendRedirect("/runner/login");
            }

            UserDao.instance.updateProfileImage(username, picture);
        }
    }

    @Path("/{username}/favorite/name")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public FavoriteOptions getLayoutName(@PathParam("username") String username) {
        return UserDao.instance.getFavoriteLayoutName(username);
    }

    @Path("/{username}/rename_favorite/{layout_id}/{name}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void RenameLayout(@PathParam("username") String username,
                             @PathParam("layout_id") String layout_id,
                             @PathParam("name") String name,
                             @Context HttpServletResponse servletResponse,
                             @Context HttpServletRequest servletRequest) throws IOException {
        if (!UserDao.instance.saveFavoriteLayoutName(username, Integer.parseInt(layout_id), name)) {

        }
    }

    @Path("/{username}/picture")
    @GET
    @Produces("image/png")
    // save a new profile picture and respond with 200
    public Response getPicture(@PathParam("username") String username,
                               @Context HttpServletResponse servletResponse,
                               @Context HttpServletRequest servletRequest) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!tokenUsername.equals(username)) {
            servletResponse.sendRedirect("/runner/login");
        }

        byte[] imageData = UserDao.instance.getUserImage(username);
        return Response.ok(imageData).build();
    }

    @Path("/{username}/picture")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPictureBase64(@PathParam("username") String username,
                                     @Context HttpServletResponse servletResponse,
                                     @Context HttpServletRequest servletRequest) throws IOException {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!tokenUsername.equals(username)) {
            servletResponse.sendRedirect("/runner/login");
        }

        byte[] imageData = UserDao.instance.getUserImage(username);

        if (imageData == null) {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("../../img/No-profile.jpg");
            imageData = UserDao.toByteArray(inputStream);
        }

        String encoded = Base64.getEncoder().encodeToString(imageData);
        return Response.ok(encoded).build();
    }


    @Path("/{username}/shoespiechart")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public PieChart getPieChart(@PathParam("username") String username) {

        List<String> res = UserDao.instance.getShoes(username);

        if (res == null) {
            return null;
        }

        PieChart pieChart = new PieChart(res, "shoes");
        return pieChart;
    }


    @Path("/{username}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream showProfilePage(@PathParam("username") String username, @Context HttpServletResponse servletResponse) throws IOException {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!tokenUsername.equals(username)) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream;

        if (UserDao.instance.isPremiumUser(username)) {
            inputStream =  classLoader.getResourceAsStream("../../html/profile_premium.html");
        } else {
            inputStream =  classLoader.getResourceAsStream("../../html/profile.html");
        }

        servletResponse.setHeader("Content-Type", "text/html");
        return inputStream;
    }

    @Path("/{username}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public User login(@PathParam("username") String username, @Context HttpServletResponse servletResponse,
                      @Context HttpHeaders headers) throws IOException {

        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        if (!tokenUsername.equals(username)) {
            servletResponse.sendRedirect("/runner/login");
            return null;
        }

        if (UserDao.instance.getUser(username, null, false) == null) {
            servletResponse.sendError(404, "Page not found.");
            return null;
        } else {
            User user = new User();
            UserDao.instance.getUserDetails(username, user);
            RunDao.instance.getUserTotalStats(username, user);
            RunDao.instance.getUserRunsList(username, user);

            servletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            servletResponse.setHeader("Pragma", "no-cache");
            servletResponse.setDateHeader("Expires", 0);
            servletResponse.setHeader("Content-Type", "application/json");

            return user;
        }
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    public void profileRedirect(@Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest) throws IOException {
        Principal principal = securityContext.getUserPrincipal();
        String tokenUsername = principal.getName();

        servletResponse.sendRedirect("profiles/" + tokenUsername);
    }
}

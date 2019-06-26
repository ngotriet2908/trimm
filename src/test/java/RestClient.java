import utwente.team2.model.User;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

public class RestClient {

    private static final String REST_URI = "http://farm02.ewi.utwente.nl:7004/runner/";
//    private static final String REST_URI = "http://localhost:8080/runner/";

    private Client client = ClientBuilder.newClient();

    private String username;
    private String token;
    private String password;

    public RestClient(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public RestClient() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Response loginTest(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("login");

        Invocation.Builder invocationBuilder = userWebTarget.request();

        MultivaluedHashMap<String,String> formData = new MultivaluedHashMap<>();

        formData.add("username", emp.getUsername());
        formData.add("password", emp.getPassword());

        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.post(Entity.form(formData));
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }



    public Response loadingLoginHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("login");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.TEXT_HTML);


        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response loadingRegisterHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("register");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.TEXT_HTML);


        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response loadRunHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .request(MediaType.TEXT_HTML);

        invocationBuilder.cookie(new Cookie("token", getToken()));

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }


    public Response loadingProfilesHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("profiles");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.TEXT_HTML);

        invocationBuilder.cookie(new Cookie("token", getToken()));

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response loadingCompareHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("compare");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.TEXT_HTML);

        invocationBuilder.cookie(new Cookie("token", getToken()));

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response loadingLogoutHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("logout");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.TEXT_HTML);

        invocationBuilder.cookie(new Cookie("token", getToken()));

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }
    public Response loadingPremiumHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("premium/join");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.TEXT_HTML);

        invocationBuilder.cookie(new Cookie("token", getToken()));

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }
    public Response loadingSettingHTML() {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("settings");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.TEXT_HTML);

        invocationBuilder.cookie(new Cookie("token", getToken()));

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }


    public Response profilePictureTest(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("profiles/{username}/picture");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("username",getUsername())
                .request("image/png");

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response profilePictureBase64Test(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("profiles/{username}/picture");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("username",getUsername())
                .request(MediaType.TEXT_PLAIN);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }
    public Response profileShoePieChart(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("profiles/{username}/shoespiechart");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("username",getUsername())
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }
    public Response profileUser(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("profiles/{username}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("username",getUsername())
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response profileGetnameFavoriteLayoutTest(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("profiles/{username}/favorite/name");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("username",getUsername())
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response profileRenameFavoriteLayoutTest(User emp, String name) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("profiles/{username}/rename_favorite/{layout_id}/{name}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("username",getUsername())
                .resolveTemplate("layout_id",1)
                .resolveTemplate("name",name)
                .request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.put(Entity.entity(emp, MediaType.APPLICATION_JSON));

        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testDefaultLayout(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/layout/reset");

        Invocation.Builder invocationBuilder = userWebTarget
                .resolveTemplate("run_id",1)
                .request();

        invocationBuilder.cookie(new Cookie("token", getToken()));

        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.post(Entity.entity(emp, MediaType.APPLICATION_JSON));
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response profileGetnameLayoutTest(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/layout/name");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response profileRenameLayoutTest(User emp, String name) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/rename_layout/{layout_id}/{name}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .resolveTemplate("layout_id",1)
                .resolveTemplate("name",name)
                .request();
        invocationBuilder.header("Content-Type", MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.put(Entity.entity(emp, MediaType.APPLICATION_JSON));

        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testLayoutData(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/layout");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }


    public Response testIndividual(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/individual/{variable}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .resolveTemplate("variable","pushoffpower_right")
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testGraph(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/graph/{numberOfSteps}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .resolveTemplate("numberOfSteps",50)
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testGraphWithIndicator(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/graph/{numberOfSteps}/{indicator}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .resolveTemplate("numberOfSteps",50)
                .resolveTemplate("indicator","pushoffpower_right")
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testDistribution(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/distribution/{indicator}");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .resolveTemplate("indicator","pushoffpower_right")
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testNote(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/note");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testRunInfo(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/info");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testInfographic(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("runs/{run_id}/infographic/browser");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .resolveTemplate("run_id",1)
                .request(MediaType.TEXT_HTML);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testGetExistingUsername(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("register/username");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .queryParam("username",emp.getUsername())
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }

    public Response testSelectOptions(User emp) {
        WebTarget webTarget = client.target(REST_URI);

        WebTarget userWebTarget = webTarget.path("compare/select");

        Invocation.Builder invocationBuilder
                = userWebTarget
                .request(MediaType.APPLICATION_JSON);

        invocationBuilder.cookie(new Cookie("token", getToken()));
        //invocationBuilder.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);

        Response response = invocationBuilder.get();
        return response;
//        return client
//                .target(REST_URI + "login")
//                .request(MediaType.APPLICATION_JSON)
//                .post(Entity.entity(emp, MediaType.APPLICATION_JSON));
    }


}
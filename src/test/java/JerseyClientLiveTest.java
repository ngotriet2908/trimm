import io.jsonwebtoken.Jwts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import utwente.team2.dao.UserDao;
import utwente.team2.model.User;
import utwente.team2.settings.ApplicationSettings;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JerseyClientLiveTest {



    public static final int HTTP_CREATED = 201;
    public static final String username = "CvdB";
    public static final String  password = "Password7";
    public static final String salt = "iuItUVNy8PpGNL7FT2s32FcQLbnEQRev0rtUtNY9hk5kL9QI7N";
    public static final byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

    private String getToken() {
        ZoneId zoneId = ZoneId.systemDefault();
        Map<String,Object> claims = new HashMap<>();
        //System.out.println(jws);
        claims.put("iss", "runner");
        claims.put("sub", username);
        claims.put("exp", String.valueOf(LocalDateTime.now().plusMinutes(5).atZone(zoneId).toEpochSecond()));
        claims.put("iat", String.valueOf(LocalDateTime.now().atZone(zoneId)));
        claims.put("key", (UserDao.instance.getSHA256(UserDao.instance.getSHA256(password) + salt).substring(0, 5)));

        String jws = Jwts.builder().setClaims(claims).signWith(ApplicationSettings.APP_KEY).compact();
        return jws;
    }

    //Test login

    @Test
    public void testLoadLoginHTML() {

        RestClient client = new RestClient();

        Response response = client.loadingLoginHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test loading login html----");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
//        System.out.println(response.readEntity(String.class));
        System.out.println();
    }


    @Test
    public void testLoginWithData() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());

        Response response = client.loginTest(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test login ---------------");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        if (response.getStatus() == 200) {
            client.setToken(getToken());
//            System.out.println(client.getToken());
            System.out.println("Token: " + client.getToken());

        }
        System.out.println();

    }


    //Test profile

    @Test
    public void testLoadProfilesHTML() {

        RestClient client = new RestClient();
        client.setToken(getToken());

        Response response = client.loadingProfilesHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test loading profiles html----");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
//        System.out.println(response.readEntity(String.class));
        System.out.println();
    }

    @Test
    public void testLoadingProfilePicture() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.profilePictureTest(emp);

        assertEquals(200, response.getStatus());
        assertEquals("image/png", response.getMediaType().toString());
        long length = response.readEntity(byte[].class).length;
        assertTrue( length > 0);

        System.out.println("-------test loading profile picture---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content Length: " + length);
        System.out.println();

    }

    @Test
    public void testLoadingProfilePictureBase64() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.profilePictureBase64Test(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading profile picture Base 64---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content length: " + content.length());
        System.out.println();

    }

    @Test
    public void testFavoriteLayout() {
        System.out.println("-------test favorite layout---");

        String nameWantToChange = "Testing";
        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.profileGetnameFavoriteLayoutTest(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);
        JSONObject jsonObject = new JSONObject(content);
        JSONArray geodata = jsonObject.getJSONArray("layoutDataList");
        String prename = "";
        for(int i = 0; i < geodata.length(); i++) {
            if (geodata.getJSONObject(i).getInt("layoutID") == 1) {
                prename = geodata.getJSONObject(i).getString("name");
                break;
            }

        }

        String preprename = prename;

        System.out.println("prename: " + prename);

        //------------------------------------------------

        response = client.profileRenameFavoriteLayoutTest(emp, nameWantToChange);

        response = client.profileGetnameFavoriteLayoutTest(emp);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        content = response.readEntity(String.class);
        assertTrue( content.length() > 0);
        jsonObject = new JSONObject(content);
        geodata = jsonObject.getJSONArray("layoutDataList");
        String postname = "";

        for(int i = 0; i < geodata.length(); i++) {
            if (geodata.getJSONObject(i).getInt("layoutID") == 1) {
                postname = geodata.getJSONObject(i).getString("name");
                break;
            }

        }


        System.out.println("postname: " + postname);

        assertTrue(postname.equals(nameWantToChange));
        //------------------------------------------------

        response = client.profileRenameFavoriteLayoutTest(emp, preprename);

        response = client.profileGetnameFavoriteLayoutTest(emp);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        content = response.readEntity(String.class);
        assertTrue( content.length() > 0);
        jsonObject = new JSONObject(content);
        geodata = jsonObject.getJSONArray("layoutDataList");

        for(int i = 0; i < geodata.length(); i++) {
            if (geodata.getJSONObject(i).getInt("layoutID") == 1) {
                postname = geodata.getJSONObject(i).getString("name");
                break;
            }

        }

        System.out.println("post-postname: " + postname);
        assertTrue(postname.equals(preprename));


        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content : " + content);
        System.out.println();

    }

    @Test
    public void testShoesPieChart() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.profileShoePieChart(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading shoes pie chart---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testUser() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.profileUser(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading User---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    //test run

    @Test
    public void testRunHTML() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.loadRunHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading Run html---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content Length: " + content.length());
        System.out.println();
    }

    @Test
    public void testDefaultLayout() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testDefaultLayout(emp);

        assertEquals(204, response.getStatus());

        System.out.println("-------test reset layout User---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Content: null");
        System.out.println();
    }

    @Test
    public void tesDashboard() {
        System.out.println("-------test dashboard layout---");

        String nameWantToChange = "Testing";
        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.profileGetnameLayoutTest(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);
        JSONObject jsonObject = new JSONObject(content);
        JSONArray geodata = jsonObject.getJSONArray("layoutDataList");
        String prename = "";
        for(int i = 0; i < geodata.length(); i++) {
            if (geodata.getJSONObject(i).getInt("layoutID") == 1) {
                prename = geodata.getJSONObject(i).getString("name");
                break;
            }

        }

        String preprename = prename;

        System.out.println("prename: " + prename);

        //------------------------------------------------

        response = client.profileRenameLayoutTest(emp, nameWantToChange);

        response = client.profileGetnameLayoutTest(emp);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        content = response.readEntity(String.class);
        assertTrue( content.length() > 0);
        jsonObject = new JSONObject(content);
        geodata = jsonObject.getJSONArray("layoutDataList");
        String postname = "";

        for(int i = 0; i < geodata.length(); i++) {
            if (geodata.getJSONObject(i).getInt("layoutID") == 1) {
                postname = geodata.getJSONObject(i).getString("name");
                break;
            }

        }


        System.out.println("postname: " + postname);

        assertTrue(postname.equals(nameWantToChange));
        //------------------------------------------------

        response = client.profileRenameLayoutTest(emp, preprename);

        response = client.profileGetnameLayoutTest(emp);
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        content = response.readEntity(String.class);
        assertTrue( content.length() > 0);
        jsonObject = new JSONObject(content);
        geodata = jsonObject.getJSONArray("layoutDataList");

        for(int i = 0; i < geodata.length(); i++) {
            if (geodata.getJSONObject(i).getInt("layoutID") == 1) {
                postname = geodata.getJSONObject(i).getString("name");
                break;
            }

        }

        System.out.println("post-postname: " + postname);
        assertTrue(postname.equals(preprename));


        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content : " + content);
        System.out.println();

    }

    @Test
    public void testLayoutData() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testLayoutData(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading layout data---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testGetIndividual() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testIndividual(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading Individual data---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testGetGraph() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testGraph(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading Graph data---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testGetGraphWithIndicator() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testGraphWithIndicator(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading Graph data with indicator---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testGetDistribution() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testDistribution(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading Distribution ---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testGetNote() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testNote(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading note data  ---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testGetRunInfo() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testRunInfo(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading note data  ---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    @Test
    public void testGetInfographic() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testInfographic(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading infographic data  ---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content Length: " + content.length());
        System.out.println();

    }

    //Test Register

    @Test
    public void testLoadRegisterHTML() {

        RestClient client = new RestClient();

        Response response = client.loadingRegisterHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test loading register html----");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
//        System.out.println(response.readEntity(String.class));
        System.out.println();
    }

    @Test
    public void testGetExistingUsername() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testGetExistingUsername(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading existing username  ---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content Length: " + content);
        System.out.println();

        response = client.testGetExistingUsername(new User("hahaha",""));

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading not existing username  ---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content Length: " + content);
        System.out.println();

    }

    //test compare

    @Test
    public void testLoadCompareHTML() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());

        Response response = client.loadingCompareHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test loading comapre html----");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
//        System.out.println(response.readEntity(String.class));
        System.out.println();
    }
    @Test
    public void testSelectOptions() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());
        Response response = client.testSelectOptions(emp);

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        String content = response.readEntity(String.class);
        assertTrue( content.length() > 0);

        System.out.println("-------test loading select data  ---");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
        System.out.println("Content: " + content);
        System.out.println();

    }

    //test logout
    @Test
    public void testLoadLogoutHTML() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());

        Response response = client.loadingLogoutHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test loading logout html----");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
//        System.out.println(response.readEntity(String.class));
        System.out.println();
    }
    //test Premium
    @Test
    public void loadingPremiumHTML() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());

        Response response = client.loadingPremiumHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test loading Premium html----");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
//        System.out.println(response.readEntity(String.class));
        System.out.println();
    }

    //test Settings
    @Test
    public void loadingSettingsHTML() {

        User emp = new User(username, password);
        RestClient client = new RestClient(emp.getUsername(), emp.getPassword());
        client.setToken(getToken());

        Response response = client.loadingSettingHTML();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getMediaType().toString());

        System.out.println("-------test loading settings html----");
        System.out.println("status: " + response.getStatus());
        System.out.println("Media Type: " + response.getMediaType().toString());
//        System.out.println(response.readEntity(String.class));
        System.out.println();
    }

}
package utwente.team2;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseInitialiser implements ServletContextListener
{
    private static Connection con;


    public static Connection getCon() {
        return con;
    }

    public void contextInitialized(ServletContextEvent arg0) {
        System.out.println("SQL: generate enum");
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("SQL: have driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Error loading driver: " + cnfe);
        }

        String host = "castle.ewi.utwente.nl";
        String dbName = "di154";
        String myschema = "project_new";
        String url = "jdbc:postgresql://" + host + ":5432/" + dbName + "?currentSchema=" + myschema;

        try {
            con = DriverManager.getConnection(url, "di154", "8OlI3B/V");
//            System.out.println("SQL: have connection");
//            Statement statement = con.createStatement();
//            String query =  "SELECT DISTINCT u.username, s.password FROM userr u, security_details s " +
//                    "WHERE u.username = s.username";
//            ResultSet resultSet = statement.executeQuery(query);
//            while (resultSet.next()) {
//                String username = resultSet.getString("username");
//                String password = resultSet.getString("password");
//                System.out.println("SQL: User: " + username + " ,password: " + password);
//            }//end contextInitialized method
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        public void contextDestroyed(ServletContextEvent arg0) {
        try {
            con.close ();
            System.out.println("server is disconnected");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//end constextDestroyed method
}
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
        System.out.println("Connecting to databse...");
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver found.");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Error loading driver: " + cnfe);
        }

        String host = "castle.ewi.utwente.nl";
        String dbName = "di154";
        String myschema = "project_new";
        String url = "jdbc:postgresql://" + host + ":5432/" + dbName + "?currentSchema=" + myschema;

//        String host = "farm02.ewi.uwente.nl";
//        String dbName = "docker";
//        String myschema = "project_new";
//        String url = "jdbc:postgresql://" + host + ":7005/" + dbName; // + "?currentSchema=" + myschema;

        try {
            con = DriverManager.getConnection(url, "di154", "8OlI3B/V");
//            con = DriverManager.getConnection(url, "di02", "1Pu7WY99OW");
            System.out.println("Connection established.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        public void contextDestroyed(ServletContextEvent arg0) {
        try {
            con.close ();
            System.out.println("Server disconnected");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
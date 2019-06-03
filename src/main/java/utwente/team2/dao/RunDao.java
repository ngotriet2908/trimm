package utwente.team2.dao;

import utwente.team2.DatabaseInitialiser;
import utwente.team2.model.Run;
import utwente.team2.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public enum RunDao {

    instance;

    public User getUserTotalStats(String username, User user) {
        try {
            // first we get total data froms runs
            // total distance
            // total runs
            // total time
            // total steps
            String query = "SELECT COUNT(*) AS total_runs, SUM(r.duration) AS total_time, SUM(r.distance) AS total_distance, SUM(r.steps) AS total_steps " +
                    "FROM run AS r " +
                    "WHERE r.username = ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be only one row
            while (resultSet.next()) {
                user.setTotalRuns(resultSet.getInt("total_runs"));
                user.setTotalTime(resultSet.getInt("total_time"));
                user.setTotalDistance(resultSet.getInt("total_distance"));
                user.setTotalSteps(resultSet.getInt("total_steps"));
            }

            return user;
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public User getUserTotalStats(String username) {
        return getUserTotalStats(username, new User());
    }


    public List<Run> getUserRunsOverview(String username) {
        try {
            // date
            // name
            // distance
            // time
            // steps
            String query = "SELECT r.id, r.date, r.name, r.distance, r.duration, r.steps " +
                    "FROM run AS r " +
                    "WHERE r.username = ? " +
                    "ORDER BY r.date DESC";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            List<Run> runOverviewList = new ArrayList<>();

            // multiple rows
            while (resultSet.next()) {
                Run runOverviewModel = new Run();

                runOverviewModel.setId(resultSet.getInt("id"));
                runOverviewModel.setDate(resultSet.getDate("date"));
                runOverviewModel.setName(resultSet.getString("name"));
                runOverviewModel.setDistance(resultSet.getInt("distance"));
                runOverviewModel.setDuration(resultSet.getInt("duration"));
                runOverviewModel.setSteps(resultSet.getInt("steps"));

                runOverviewList.add(runOverviewModel);
            }

            return runOverviewList;

        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public User getUserRunsOverview(String username, User user) {
        user.setRunsList(getUserRunsOverview(username));
        return user;
    }



    public String getLayout(int runId) {
        try {
            String query = "SELECT r.layout " +
                    "FROM run AS r " +
                    "WHERE r.id = ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, runId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                return "{\"layout\": \"unavailable\"}";
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }


    public boolean saveLayout(int runId, String layout) {
        try {
            String query = "UPDATE run " +
                    " SET layout = ?" +
                    " WHERE id = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, layout);
            statement.setInt(2, runId);

            int resultSet = statement.executeUpdate();

            return resultSet > 0;
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }


    public Run getRunsOverviewByID(int runid) {
        try {
            // date
            // name
            // distance
            // time
            // steps
            String query = "SELECT * " +
                    "FROM run AS r " +
                    "WHERE r.id =  " + runid;

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            ResultSet resultSet = statement.executeQuery();


            // multiple rows
            if (resultSet.next()) {
                Run runOverviewModel = new Run();

                runOverviewModel.setId(resultSet.getInt("id"));
                runOverviewModel.setDate(resultSet.getDate("date"));
                runOverviewModel.setName(resultSet.getString("name"));
                runOverviewModel.setDistance(resultSet.getInt("distance"));
                runOverviewModel.setDuration(resultSet.getInt("duration"));
                runOverviewModel.setSteps(resultSet.getInt("steps"));
                return runOverviewModel;
            }

            return null;

        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }
}

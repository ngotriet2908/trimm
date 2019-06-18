package utwente.team2.dao;

import utwente.team2.DatabaseInitialiser;
import utwente.team2.model.Run;
import utwente.team2.model.User;

import java.math.BigDecimal;
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


    public String getDefaultLayout(int runId) {
        try {
            String query = "SELECT r.default_layout " +
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

    public BigDecimal getSpeed(int runId) {
        try {
            String query = "SELECT r.distance, r.steps " +
                    "FROM run AS r " +
                    "WHERE r.id = ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, runId);

            ResultSet resultSet = statement.executeQuery();

            System.out.println(statement);

            if (resultSet.next()) {

                String distance = resultSet.getString(1);
                String steps = resultSet.getString(2);
                if (distance == null) {
                    return BigDecimal.valueOf(1.88);
                } else {
                    return BigDecimal.valueOf(Double.parseDouble(distance) / Double.parseDouble(steps));
                }


            } else {
                return BigDecimal.valueOf(1.88);
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



    public String getShoes(int runid) {
        try {
            // date
            // name
            // distance
            // time
            // steps
            String query = "SELECT s.brand || ' ' || s.model as shoesname " +
                    "FROM run AS r, shoes AS s " +
                    "WHERE r.id =  ? " +
                    "AND r.shoes_id = s.id ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, runid);
            ResultSet resultSet = statement.executeQuery();


            // multiple rows
            if (resultSet.next()) {
                String shoesname = resultSet.getString("shoesname");
                if (shoesname != null) {
                    return shoesname;
                } else {
                    return "Mixed/No Shoes";
                }
            }

            return "Mixed/No Shoes";

        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public Run getRunsOverviewByID(int runid) {
        String shoesname = getShoes(runid);

        try {
            // date
            // name
            // distance
            // time
            // steps
            String query = "SELECT r.id, r.date, r.name, r.distance, r.duration, r.steps " +
                    "FROM run AS r " +
                    "WHERE r.id =  ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, runid);
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
                runOverviewModel.setShoesname(shoesname);


                return runOverviewModel;
            }

            return null;

        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public boolean isUsersRun(String username, int runId) {
        try {
            String query = "SELECT r.id " +
                    "FROM run AS r " +
                    "WHERE r.id = ? AND r.username = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, runId);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }
}

package utwente.team2.dao;

import utwente.team2.DatabaseInitialiser;
import utwente.team2.model.GraphPoints;
import utwente.team2.model.Individual;
import utwente.team2.model.Step;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public enum StepDao {

    instance;

    public List<Step> getSteps(String runID, int numberOfStep) {
        try{
            String query =  "SELECT DISTINCT * FROM step s, run " +
                    "WHERE s.run_id = ? " +
                    "AND run.id = s.run_id " +
                    "AND MOD(s.step_no - 1, DIV(run.steps,?)) = 0 " +
                    "ORDER BY s.step_no";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, Integer.parseInt(runID));
            statement.setInt(2, numberOfStep);

            ResultSet resultSet = statement.executeQuery();

            List<Step> steps = new ArrayList<>();

            while(resultSet.next()) {
                int stepID = resultSet.getInt("step_no");

                String time = resultSet.getString("time");
                Integer surface = (Integer)resultSet.getObject("surface_id");;
                String ic_right = resultSet.getString("ic_right");;
                String to_right = resultSet.getString("to_right");;
                BigDecimal axtibacc_right = resultSet.getBigDecimal("axtibacc_right");
                BigDecimal tibimpact_right = resultSet.getBigDecimal("tibimpact_right");
                BigDecimal axsacacc_right = resultSet.getBigDecimal("axsacacc_right");
                BigDecimal sacimpact_right = resultSet.getBigDecimal("sacimpact_right");
                BigDecimal brakingforce_right = resultSet.getBigDecimal("brakingforce_right");
                BigDecimal pushoffpower_right = resultSet.getBigDecimal("pushoffpower_right");
                BigDecimal tibintrot_right = resultSet.getBigDecimal("tibintrot_right");
                BigDecimal vll_right = resultSet.getBigDecimal("vll_right");
                String ic_left = resultSet.getString("ic_left");
                String to_left = resultSet.getString("to_left");
                BigDecimal axtibacc_left = resultSet.getBigDecimal("axtibacc_left");
                BigDecimal tibimpact_left = resultSet.getBigDecimal("tibimpact_left");
                BigDecimal axsacacc_left = resultSet.getBigDecimal("axsacacc_left");
                BigDecimal sacimpact_left = resultSet.getBigDecimal("sacimpact_left");
                BigDecimal brakingforce_left = resultSet.getBigDecimal("brakingforce_left");
                BigDecimal pushoffpower_left = resultSet.getBigDecimal("pushoffpower_left");
                BigDecimal tibintrot_left = resultSet.getBigDecimal("tibintrot_left");
                BigDecimal vll_left = resultSet.getBigDecimal("vll_left");

                Step step = new Step( stepID,  time,  surface,  ic_right,  to_right,
                         axtibacc_right,  tibimpact_right,  axsacacc_right,
                         sacimpact_right,  brakingforce_right,  pushoffpower_right,
                         tibintrot_right,  vll_right,  ic_left,  to_left,
                         axtibacc_left,  tibimpact_left,  axsacacc_left,
                         sacimpact_left,  brakingforce_left,  pushoffpower_left,
                         tibintrot_left,  vll_left);

                    steps.add(step);
            }
            return steps;
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }

        return null;
    }


    public Individual getIndividual(String variable, int runID) {
        try{
            String query =  "SELECT MIN(s."+ variable + ")" + ",MAX(s." + variable + ")" + ",AVG(s." + variable + "),t.name, t.meaning FROM step s, term t " +
                    "WHERE s.run_id = ? " +
                    "AND t.name = '" + variable + "' " +
                    "GROUP BY t.name";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, runID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //TODO get all variables
                return new Individual(resultSet.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                        resultSet.getBigDecimal(2).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                        resultSet.getBigDecimal(3).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                        resultSet.getString(4),resultSet.getString(5));
            }
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }
        return null;
    }


    public List<BigDecimal> getBaseLine(String indicator) {
        try{

            String query =  "SELECT DISTINCT  b.segment, b." + indicator + " FROM baseline b " +
                    "ORDER BY  b.segment";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            ResultSet resultSet = statement.executeQuery();

            List<BigDecimal> res = new ArrayList<>();

            while(resultSet.next()) {

                res.add(resultSet.getBigDecimal(2));
            }
            return res;
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }

        return null;
    }

    public GraphPoints getAllSteps(String runID, String indicator) {
        try{
            String query =  "SELECT DISTINCT  s.step_no, s." + indicator + " FROM step s, run " +
                    "WHERE s.run_id = ? " +
                    "AND run.id = s.run_id " +
                    "ORDER BY s.step_no";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, Integer.parseInt(runID));

            ResultSet resultSet = statement.executeQuery();

            GraphPoints graphPoints = new GraphPoints(indicator);

            while(resultSet.next()) {
                if (resultSet.getBigDecimal(2) == null) {
                    continue;
                }
                graphPoints.getStep_no().add(resultSet.getInt("step_no"));
                graphPoints.getLeft().add(resultSet.getBigDecimal(2));
            }
            return graphPoints;
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }

        return null;
    }

    public GraphPoints getStepsAndTime(String runID, int numberOfSteps, boolean withNumberOfSteps) {
        try{
            String query;
            PreparedStatement statement;

            if (withNumberOfSteps) {
                query = "SELECT DISTINCT  s.step_no, s.time " +
                        "FROM step s, run " +
                        "WHERE s.run_id = ? " +
                        "AND run.id = s.run_id " +
                        "AND MOD(s.step_no - 1, DIV(run.steps,?)) = 0 " +
                        "ORDER BY s.step_no";

                statement = DatabaseInitialiser.getCon().prepareStatement(query);
                statement.setInt(1, Integer.parseInt(runID));
                statement.setInt(2, numberOfSteps);
            } else {
                query =  "SELECT DISTINCT  s.step_no, s.time FROM step s, run " +
                        "WHERE s.run_id = ? " +
                        "AND run.id = s.run_id " +
                        "AND MOD(s.step_no - 1, 50) = 0 " +
                        "ORDER BY s.step_no";

                statement = DatabaseInitialiser.getCon().prepareStatement(query);
                statement.setInt(1, Integer.parseInt(runID));
            }

            ResultSet resultSet = statement.executeQuery();

            GraphPoints graphPoints = new GraphPoints("speed");

            while(resultSet.next()) {
                graphPoints.getStep_no().add(resultSet.getInt("step_no"));

                LocalDateTime timestamp = resultSet.getTimestamp(2).toLocalDateTime();
                ZoneId zoneId = ZoneId.systemDefault();
                long unixTime = timestamp.atZone(zoneId).toEpochSecond();

                graphPoints.getLeft().add(BigDecimal.valueOf(unixTime).setScale(10, BigDecimal.ROUND_UP));
            }
            return graphPoints;
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }

        return null;
    }


    public GraphPoints getStepsWithNumberOfSteps(String runID, int numberOfSteps, String indicator) {
        try{

            String query =  "SELECT DISTINCT  s.step_no, s." + indicator + " FROM step s, run " +
                    "WHERE s.run_id = ? " +
                    "AND run.id = s.run_id " +
                    "AND MOD(s.step_no - 1, DIV(run.steps,?)) = 0 " +
                    "ORDER BY s.step_no";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, Integer.parseInt(runID));
            statement.setInt(2, numberOfSteps);

            ResultSet resultSet = statement.executeQuery();

            GraphPoints graphPoints = new GraphPoints(indicator);

            while(resultSet.next()) {
                graphPoints.getStep_no().add(resultSet.getInt("step_no"));
                graphPoints.getLeft().add(resultSet.getBigDecimal(2));
            }
            graphPoints.setRight(getBaseLine(indicator));
            return graphPoints;
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }

        return null;
    }


    public GraphPoints getStepsWithNumberOfStepsAndRange(String runID, int numberOfSteps, String indicator, int startP, int endP) {
        try{

            String indicator_left = indicator + "_left";
            String indicator_right = indicator + "_right";


            String query =  "SELECT DISTINCT  s.step_no, s." + indicator_left + ", s." + indicator_right + " FROM step s, run " +
                    "WHERE s.run_id = ? " +
                    "AND run.id = s.run_id " +
                    "AND MOD(s.step_no - 1, DIV((? - ?),?)) = 0 " +
                    "AND s.step_no > ? - 2 " +
                    "AND s.step_no < ? + 2 " +
                    "ORDER BY s.step_no";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setInt(1, Integer.parseInt(runID));
            statement.setInt(2, endP);
            statement.setInt(3, startP);
            statement.setInt(4, numberOfSteps);
            statement.setInt(5, startP);
            statement.setInt(6, endP);


            ResultSet resultSet = statement.executeQuery();

            GraphPoints graphPoints = new GraphPoints(indicator);

            while(resultSet.next()) {
                graphPoints.getStep_no().add(resultSet.getInt("step_no"));
                graphPoints.getLeft().add(resultSet.getBigDecimal(2));
                graphPoints.getRight().add(resultSet.getBigDecimal(3));
            }
            return graphPoints;
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }

        return null;
    }
}

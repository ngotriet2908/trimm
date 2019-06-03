package utwente.team2.dao;

import utwente.team2.DatabaseInitialiser;
import utwente.team2.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum UserDao {

    instance;

    public User getUserDetails(String username, User user) {
        try {
            String query = "SELECT * " +
                        "FROM general_user AS u " +
                        "WHERE u.username = ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be only one row
            resultSet.next();

            user.setUsername(username);
            user.setFirstName(resultSet.getString("first_name"));
            user.setLastName(resultSet.getString("last_name"));
            user.setEmail(resultSet.getString("email"));

            if (isPremiumUser(username)) {
                user.setIsPremium(1);
            } else {
                user.setIsPremium(0);
            }

            return user;
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }


    public User getUserDetails(String username) {
        return getUserDetails(username, new User());
    }


    public User getUser(String username, String password, boolean checkPassword) {
        try {
            String query;
            PreparedStatement statement;

            if (checkPassword) {
                query = "SELECT u.username " +
                        "FROM general_user AS u " +
                        "WHERE u.username = ? AND u.password = ?";

                statement = DatabaseInitialiser.getCon().prepareStatement(query);
                statement.setString(1, username);
                statement.setString(2, password);
            } else {
                query = "SELECT u.username " +
                        "FROM general_user AS u " +
                        "WHERE u.username = ? ";

                statement = DatabaseInitialiser.getCon().prepareStatement(query);
                statement.setString(1, username);
            }

            ResultSet resultSet = statement.executeQuery();

            // should be the first entry if exists
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                return user;
            } else {
                return null;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public User getUserWithPassword(String username, String password) {
        return getUser(username, password, true);
    }

    public boolean isPremiumUser(String username) {
        try {
            String queryForPremiumStatus = "SELECT p.username " +
                    "FROM premium_user AS p " +
                    "WHERE p.username = ? ";

            // also check time? TODO

            PreparedStatement statementForPremiumStatus = DatabaseInitialiser.getCon().prepareStatement(queryForPremiumStatus);
            statementForPremiumStatus.setString(1, username);

            ResultSet resultSetForPremiumStatus = statementForPremiumStatus.executeQuery();

            return !(!resultSetForPremiumStatus.isBeforeFirst() && resultSetForPremiumStatus.getRow() == 0);
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }

    public boolean updateProfile(String username, String firstname, String lastname) {
        int allRowAffect = 0;

        try {
            String updatefirstname = "UPDATE general_user " +
                    "SET first_name = ? " +
                    "WHERE username = " + "'" + username + "'";
            String updatelastname = "UPDATE general_user " +
                    "SET last_name = ? " +
                    "WHERE username = " + "'" + username + "'";
            // also check time? TODO


            if (!firstname.equals("")) {
                PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(updatefirstname);
                statementForUpdate.setString(1, firstname);

                int rowAffect = statementForUpdate.executeUpdate();
                allRowAffect += rowAffect;
            }
            if (!lastname.equals("")) {
                PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(updatelastname);
                statementForUpdate.setString(1, lastname);

                int rowAffect = statementForUpdate.executeUpdate();
                allRowAffect += rowAffect;
            }



        } catch (SQLException se) {
            se.printStackTrace();
        }
        return allRowAffect > 0;
    }
    public boolean register(String username, String firstName, String lastName, String email, String password) {
        try {
            String query = "INSERT INTO general_user (username, first_name, last_name, email, password) VALUES (?, ?, ?, ?, ?) ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, email);
            statement.setString(5, password);

            System.out.println(statement);

            int resultSet = statement.executeUpdate();

            // should be only one row
            if (resultSet > 0) {
                System.out.println("Registered a new user!");
                return true;
            } else {
                System.out.println("Failed to register the user!");
                return false;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("Failed to register the user!");
        return false;
    }
//    public String snakeToCamel(String name) {
//        String[] splitted = name.split("_");
//
//        String camelCase = "" + splitted[0];
//
//        for (int i = 1; i < splitted.length; i++) {
//            camelCase += splitted[1].substring(0, 1).toUpperCase() + splitted[1].substring(1);
//        }
//
//        return camelCase;
//    }
}

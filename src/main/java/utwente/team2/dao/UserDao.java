package utwente.team2.dao;

import org.apache.commons.codec.binary.Hex;
import utwente.team2.DatabaseInitialiser;
import utwente.team2.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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
            if (resultSet.next()) {

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
            } else {
                return null;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public byte[] getUserImage(String username) {
        try {
            String query = "SELECT picture " +
                    "FROM user_picture AS u " +
                    "WHERE u.username = ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be only one row
            if (resultSet.next()) {
                return resultSet.getBytes(1);
            } else {
                return null;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }


    public User getUserDetails(String username) {
        return getUserDetails(username, new User());
    }

    public String getUsersPassword(String username) {
        try {
            String query = "SELECT password " +
                    "FROM general_user AS u " +
                    "WHERE u.username = ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be only one row
            if (resultSet.next()) {
                return resultSet.getString("password");
            } else {
                return null;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public String getSalt(String username) {
        try {
            String query;
            PreparedStatement statement;
            query = "SELECT u.salt " +
                    "FROM general_user AS u " +
                    "WHERE u.username = ? ";

            statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be the first entry if exists
            if (resultSet.next()) {
                return resultSet.getString("salt");
            } else {
                return null;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }


    public User getUser(String username, String password, boolean checkPassword) {
        try {
            String query;
            PreparedStatement statement;

            if (checkPassword) {

                String salt = getSalt(username);


                query = "SELECT u.username " +
                        "FROM general_user AS u " +
                        "WHERE u.username = ? AND u.password = ?";

                statement = DatabaseInitialiser.getCon().prepareStatement(query);
                statement.setString(1, username);
                statement.setString(2, getSHA256(getSHA256(password) + salt));
            } else {
                query = "SELECT u.username " +
                        "FROM general_user AS u " +
                        "WHERE u.username = ? ";

                statement = DatabaseInitialiser.getCon().prepareStatement(query);
                statement.setString(1, username);
            }

            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();

            // should be the first entry if exists
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                System.out.println("there exist");
                return user;
            } else {
                System.out.println("no user");
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

    public boolean isUsersEmail(String username, String email) {
        try {
            String query = "SELECT p.username " +
                    "FROM general_user AS p " +
                    "WHERE p.username = ? " +
                    "AND p.email = ?";

            // also check time? TODO

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, email);

            ResultSet resultSet = statement.executeQuery();

            return !(!resultSet.isBeforeFirst() && resultSet.getRow() == 0);
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }

    public boolean isActivated(String username) {
        try {
            String queryForActivation = "SELECT p.is_activated " +
                    "FROM general_user AS p " +
                    "WHERE p.username = ? ";

            // also check time? TODO

            PreparedStatement statementForActivation = DatabaseInitialiser.getCon().prepareStatement(queryForActivation);
            statementForActivation.setString(1, username);

            ResultSet resultSetForActivation = statementForActivation.executeQuery();

            System.out.println(statementForActivation);
            if (!resultSetForActivation.next()) {
                System.out.println("empty list");
                return false;
            }

            return resultSetForActivation.getBoolean(1);
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }


    public boolean updateProfile(String username, String firstName, String lastName) {
        int totalRowsAffected = 0;

        try {
            String updateName = "UPDATE general_user " +
                    "SET first_name = ?, last_name = ? " +
                    "WHERE username = " + "'" + username + "'";

            if (!firstName.equals("") && !lastName.equals("")) {
                PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(updateName);
                statementForUpdate.setString(1, firstName);
                statementForUpdate.setString(2, lastName);

                int rowsAffected = statementForUpdate.executeUpdate();
                totalRowsAffected += rowsAffected;
            }
//            if (!lastname.equals("")) {
//                PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(updatelastname);
//                statementForUpdate.setString(1, lastname);
//
//                int rowAffect = statementForUpdate.executeUpdate();
//                totalRowsAffected += rowAffect;
//            }

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return totalRowsAffected > 0;
    }


    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }


    public boolean activateAccount(String username) {
        int totalRowsAffected = 0;

        try {
            String query = "UPDATE general_user " +
                    "SET is_activated = ? "
                    + "WHERE username = ? ";

            PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(query);

            statementForUpdate.setString(2, username);
            statementForUpdate.setBoolean(1,true);
            int rowsAffected = statementForUpdate.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return totalRowsAffected > 0;
    }



    public String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public boolean updatePassword(String username, String password) {
        int totalRowsAffected = 0;

        String salt = getAlphaNumericString(50); // TODO secure random?

        try {
            String updatePassword = "UPDATE general_user " +
                    "SET password = ? " +
                    ", salt = ? " +
                    "WHERE username = " + "'" + username + "'";

            if (!password.equals("")) {
                PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(updatePassword);
                statementForUpdate.setString(1, getSHA256(getSHA256(password) + salt));
                statementForUpdate.setString(2, salt);

                int rowsAffected = statementForUpdate.executeUpdate();
                totalRowsAffected += rowsAffected;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("success update password: " + (totalRowsAffected > 0));
        return totalRowsAffected > 0;
    }



    public boolean register(String username, String firstName, String lastName, String email, String password) {
        try {
            String salt = getAlphaNumericString(50); // TODO secure random?

            String query = "INSERT INTO general_user (username, first_name, last_name, email, password, salt) VALUES (?, ?, ?, ?, ?, ?) ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, email);
            statement.setString(5, getSHA256(getSHA256(password) + salt));
            statement.setString(6, salt);

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

    public String getSHA256(String password) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String encoded = Hex.encodeHexString(hash);
            return encoded;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getShoes(String username) {
        try {
            String query = "SELECT s.brand || ' ' || s.model, r.id " +
                    "FROM shoes AS s, run AS r, general_user u " +
                    "WHERE s.id = r.shoes_id " +
                    "AND u.username = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();
            List<String> res = new ArrayList<>();
            while (resultSet.next()) {
                if (resultSet.getString(1) != null && !resultSet.getString(1).equals("")) {
                    res.add(resultSet.getString(1));
                }
            }
            return res;

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return null;

    }


    public boolean upgradeToPremium(String username) {
        try {
            String query = "INSERT INTO premium_user (username, start_date, end_date) VALUES (?, ?, ?) ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, username);
            statement.setNull(2, Types.TIMESTAMP);
            statement.setNull(3, Types.TIMESTAMP);

            int resultSet = statement.executeUpdate();

            // should be only one row
            if (resultSet > 0) {
                System.out.println("Upgraded the user's status!");
                return true;
            } else {
                System.out.println("Failed to upgrade the user's status...");
                return false;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("Failed to register the user!");
        return false;
    }


    public boolean updateProfileImage(String username, InputStream image) {
        int totalRowsAffected = 0;

        if (!hasImage(username)) {
            return insertProfileImage(username,image);
        }

        try {
            String query = "UPDATE user_picture " +
                    "SET picture = ?"
                    + "WHERE username = ? ";

            PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(query);

            statementForUpdate.setString(2, username);
            statementForUpdate.setBytes(1, toByteArray(image));
            int rowsAffected = statementForUpdate.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean insertProfileImage(String username, InputStream image) {
        int totalRowsAffected = 0;

        try {
            String query = "INSERT INTO user_picture(username, picname, picture) " +
                    "VALUES (?,?,?)";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, username);
            statement.setString(2, "profile");
            statement.setBytes(3, toByteArray(image));
            boolean rowsAffected = statement.execute();

            return rowsAffected;

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasImage(String username) {
        try {
            String query = "SELECT u.username " +
                    "FROM user_picture as u " +
                    "WHERE username = ? ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException se) {
            se.printStackTrace();
        }
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

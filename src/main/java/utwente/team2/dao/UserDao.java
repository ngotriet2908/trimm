package utwente.team2.dao;

import org.apache.commons.codec.binary.Hex;
import utwente.team2.model.FavoriteOptions;
import utwente.team2.model.LayoutData;
import utwente.team2.model.User;
import utwente.team2.settings.DatabaseInitialiser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public enum UserDao {

    instance;

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

    public void insertFavoriteLayout(String username) {
        String query = "INSERT INTO favorite_layout(username, lid, name) "
                + "VALUES(?,?,?)";

        try {
            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            for(int i = 1; i < 6; i++) {
                statement.setString(1, username);
                statement.setInt(2, i);
                statement.setString(3, "Favorite " + i);
                statement.execute();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public String getUserDetailsWithEmail(String email) {
        try {
            String query = "SELECT * FROM general_user " +
                    "WHERE email = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();
            // should be only one row
            if (resultSet.next()) {

                return resultSet.getString("username");
            } else {
                return null;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public User getUserDetails(String username, User user) {
        try {
            String query = "SELECT * FROM getUserDetails(?)";

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
            String query = "SELECT * FROM getUserImage(?)";


            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be only one row
            if (resultSet.next()) {
                return resultSet.getBytes("r_picture");
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
            String query = "SELECT * FROM getUsersPassword(?)";


            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be only one row
            if (resultSet.next()) {
                return resultSet.getString("r_password");
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
            PreparedStatement statement;
            String query = "SELECT * FROM getSalt(?)";


            statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // should be the first entry if exists
            if (resultSet.next()) {
                return resultSet.getString("r_salt");
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
            String queryForPremiumStatus = "SELECT * FROM isPremiumUser(?)";

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
            String query = "SELECT * FROM isUsersEmail(?,?)";

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

    public boolean emailExist( String email) {
        try {
            String query = "SELECT * FROM general_user " +
                    "WHERE email = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }

    public boolean isActivated(String username) {
        try {
            String queryForActivation = "SELECT * FROM isActivated(?)";

            PreparedStatement statementForActivation = DatabaseInitialiser.getCon().prepareStatement(queryForActivation);
            statementForActivation.setString(1, username);

            ResultSet resultSetForActivation = statementForActivation.executeQuery();

            if (!resultSetForActivation.next()) {
                return false;
            }

            return resultSetForActivation.getBoolean("r_is_activated");
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

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return totalRowsAffected > 0;
    }

    public boolean activateAccount(String username) {
        int totalRowsAffected = 0;

        try {
            String query = "UPDATE general_user " +
                    "SET is_activated = ? "
                    + "WHERE username = ? ";

            PreparedStatement statementForUpdate = DatabaseInitialiser.getCon().prepareStatement(query);

            statementForUpdate.setString(2, username);
            statementForUpdate.setBoolean(1, true);
            int rowsAffected = statementForUpdate.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return totalRowsAffected > 0;
    }


    public String getAlphaNumericString(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            int index = (int) (AlphaNumericString.length() * Math.random());

            sb.append(AlphaNumericString.charAt(index));
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


            int resultSet = statement.executeUpdate();

            // should be only one row
            if (resultSet > 0) {
                System.out.println("Successfully registered a new user!");
                return true;
            } else {
                System.out.println("Failed to register a new user!");
                return false;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("Failed to register a new user!");
        return false;
    }

    public String getSHA256(String password) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getShoes(String username) {
        try {
            String query = "SELECT * FROM getShoes(?)";


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

    public FavoriteOptions getFavoriteLayoutName(String username) {
        try {
            String query = "SELECT * FROM getFavoriteLayoutName(?)";


            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            List<LayoutData> layoutData = new ArrayList<>();

            while (resultSet.next()) {
                String name = resultSet.getString("r_name");
                int lid = resultSet.getInt("r_lid");
                layoutData.add(new LayoutData(name, lid));
            }

            return new FavoriteOptions(layoutData, username);
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public boolean saveFavoriteLayoutName(String username, int layout_id, String name) {

        try {
            String query = "UPDATE favorite_layout " +
                    " SET name = ? " +
                    " WHERE lid = ? " +
                    "AND username = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, name);
            statement.setInt(2, layout_id);
            statement.setString(3, username);

            int resultSet = statement.executeUpdate();

            return resultSet > 0;
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }

    public boolean saveFavoriteLayout(int layout_id, int runID) {
        String layout = RunDao.instance.getLayout(runID);
        String username = RunDao.instance.getUsername(runID);
        try {
            String query = "UPDATE favorite_layout " +
                    " SET layout = ? " +
                    " WHERE lid = ? " +
                    "AND username = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, layout);
            statement.setInt(2, layout_id);
            statement.setString(3, username);

            int resultSet = statement.executeUpdate();

            return resultSet > 0;
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }

    public String getFavoriteLayout(int layout_id, String username) {
        try {
            String query = "SELECT * FROM getFavoriteLayout(?,?)";


            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setInt(1, layout_id);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("r_layout");
            }

            return null;
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return null;
    }

    public boolean loadFavoriteLayout(int layout_id, int runID) {
        String username = RunDao.instance.getUsername(runID);
        String layout = getFavoriteLayout(layout_id, username);
        int currentLayout = RunDao.instance.getCurrentLayout(runID);

        try {
            String query = "UPDATE layout " +
                    " SET layout = ? " +
                    " WHERE lid = ? " +
                    "AND run_id = ?";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, layout);
            statement.setInt(2, currentLayout);
            statement.setInt(3, runID);

            int resultSet = statement.executeUpdate();

            return resultSet > 0;
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }

    public boolean upgradeToPremium(String username) {
        try {
            String query = "INSERT INTO premium_user (username, start_date, end_date) VALUES (?, ?, ?) ";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, username);
            statement.setTimestamp(2, Timestamp.valueOf(LocalDate.now().atStartOfDay()));
            statement.setTimestamp(3, Timestamp.valueOf(LocalDate.now().atStartOfDay().plusYears(1)));

            int resultSet = statement.executeUpdate();

            // should be only one row
            if (resultSet > 0) {
                System.out.println("Upgraded the user's status.");
                return true;
            } else {
                System.out.println("Failed to upgrade the user's status...");
                return false;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return false;
    }


    public boolean updateProfileImage(String username, InputStream image) {
        if (!hasImage(username)) {
            return insertProfileImage(username, image);
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
            String query = "SELECT * FROM hasImage(?)";

            PreparedStatement statement = DatabaseInitialiser.getCon().prepareStatement(query);

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }
}

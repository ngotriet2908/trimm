package utwente.team2.databaseBackup;

import org.apache.poi.ss.usermodel.*;
import utwente.team2.dao.UserDao;
import utwente.team2.model.GraphPoints;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class DataExporter {
    private static final int MAXIMUM_LINE = 1000000000;

    private final String host = "castle.ewi.utwente.nl";
    private final String dbName= "di154";
    private final String myschema = "project_new";

    private final String url = "jdbc:postgresql://" + host + ":5432/"+ dbName + "?currentSchema=" + myschema;
    //    private final String url = "jdbc:postgresql://" + host + ":5432/"+ dbName;
    private final String user = "di154";
    private final String password = "8OlI3B/V";

    private Connection conn;

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    private static SimpleDateFormat DATE_FORMAT_RUN = new SimpleDateFormat("MM/dd/yy");

    public DataExporter() {
        connect();
    }

    public void connect() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }

        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public GraphPoints getAllSteps(int runID, String indicator) {
//        System.out.println("get run info " + runID);
        try{
            String query =  "SELECT DISTINCT  s.step_no, s." + indicator + " FROM step s, run " +
                    "WHERE s.run_id = ? " +
                    "AND run.id = s.run_id " +
                    "ORDER BY s.step_no";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, runID);


//            System.out.println(statement.toString());

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

    public BigDecimal getAverageFromList(List<BigDecimal> bigDecimals) {
        BigDecimal bigDecimal = BigDecimal.ZERO;

        for(int i = 0; i < bigDecimals.size(); i++) {
            bigDecimal = bigDecimal.add(bigDecimals.get(i));
        }

        return bigDecimal.divide(BigDecimal.valueOf(bigDecimals.size()), BigDecimal.ROUND_UP).setScale(10, BigDecimal.ROUND_UP);
    }

    public List<BigDecimal> getAverage(GraphPoints graphPoints) {
        List<BigDecimal> res = new ArrayList<>();
        int segment = graphPoints.getLeft().size() / 50;

        for(int i = 0; i < 50; i++) {
            res.add(getAverageFromList(graphPoints.getLeft().subList(segment * i, segment * (i + 1))));
        }
        return res;
    }

    public void UpdateBaseLine(List<BigDecimal> bigDecimals, String indicator) {
        try {
            String query = "UPDATE baseline " +
                    " SET " + indicator + " = ?" +
                    " WHERE segment = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            for(int i = 0; i < 50; i++) {
                statement.setBigDecimal(1, bigDecimals.get(i));
                statement.setInt(2, i + 1);
//                System.out.println(statement); //TODO turnoff this one
                int resultSet = statement.executeUpdate();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public List<BigDecimal> getBaseLine(String indicator) {
        List<List<BigDecimal>> averageAllRun = new ArrayList<>();
        List<BigDecimal> res = new ArrayList<>();

        for(int i = 1; i <= 9; i++) {
            if (i == 8) {
                continue;
            }
            GraphPoints graphPoints = getAllSteps(i, indicator);
            List<BigDecimal> average = getAverage(graphPoints);
            averageAllRun.add(average);
        }

        for(int i = 0; i < 50; i++) {
            BigDecimal ColResult = BigDecimal.ZERO;
            for(int j = 0; j < averageAllRun.size(); j++) {
                ColResult = ColResult.add(averageAllRun.get(j).get(i));
            }

            res.add(ColResult.divide(BigDecimal.valueOf(averageAllRun.size())).setScale(10,BigDecimal.ROUND_UP));
        }
        return res;
    }



    public void createBaseLineMole() {
        String query = "INSERT INTO baseline(segment) "
                + "VALUES(?)";

        try {
            PreparedStatement statement = conn.prepareStatement(query);
            for(int i = 1; i <= 50; i++) {
                statement.setInt(1, i);
                statement.execute();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }






    public void insertSteps() {
        List<String[]> attributeList = new ArrayList<>();
        int maxLine = 0;

        String query = "INSERT INTO step(run_id, step_no, time, surface_id, " +
                "ic_right, to_right, axtibacc_right, tibimpact_right, axsacacc_right, sacimpact_right, " +
                "brakingforce_right, pushoffpower_right, tibintrot_right, vll_right, " +
                "ic_left, to_left, axtibacc_left, tibimpact_left, axsacacc_left, sacimpact_left, " +
                "brakingforce_left, pushoffpower_left, tibintrot_left, vll_left) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (BufferedReader br = new BufferedReader(new FileReader("RA-data/stepdata_201905011157.csv"))) {
            // read the first line from the text file
            String line = br.readLine();
            line = br.readLine();

            int rowsFound = 0;
            PreparedStatement statement = conn.prepareStatement(query);

            // loop until all lines are read
            while (line != null) {
                String[] attributes = line.split(",");
                String[] atts = new String[30];
                for (int i = 0; i < atts.length; i++) {
                    atts[i] = "";
                }

                for (int i = 0; i < attributes.length; i++) {
                    atts[i] = attributes[i];
                }
                int numberOfNotNullValue = 0;

                for (int i = 0; i < attributes.length; i++) {
                    if (!attributes[i].equals("")) {
                        numberOfNotNullValue++;
                    }
                }

                if (numberOfNotNullValue >= 3) {
                    attributeList.add(atts);
                }

                rowsFound++;

                // insert in database
                for (int i = 0; i < attributeList.size(); i++) {
                    statement.setTimestamp(3, new Timestamp(DATE_FORMAT.parse(attributeList.get(i)[0]).getTime()));

                    statement.setInt(2, Integer.parseInt(attributeList.get(i)[1]));

                    statement.setInt(1, Integer.parseInt(attributeList.get(i)[3]));

                    // surface
                    if (!attributeList.get(i)[4].equals("")) {
                        statement.setInt(4, Integer.parseInt(attributeList.get(i)[4]));
                    } else {
                        statement.setInt(4, 0);
                    }

                    // right
                    for (int k = 5; k <= 6; k++) {
                        if (!attributeList.get(i)[k].equals("")) {
                            statement.setTimestamp(k, new Timestamp(DATE_FORMAT.parse(attributeList.get(i)[k]).getTime()));
                        } else {
                            statement.setNull(k, Types.TIMESTAMP);
                        }
                    }

                    for (int k = 7; k <= 14; k++) {
                        if (!attributeList.get(i)[k].equals("")) {
                            statement.setDouble(k, Double.parseDouble(attributeList.get(i)[k]));
                        } else {
                            statement.setNull(k, Types.NUMERIC);
                        }
                    }

                    // left
                    for (int k = 15; k <= 16; k++) {
                        if (!attributeList.get(i)[k].equals("")) {
                            statement.setTimestamp(k, new Timestamp(DATE_FORMAT.parse(attributeList.get(i)[k]).getTime()));
                        } else {
                            statement.setNull(k, Types.TIMESTAMP);
                        }
                    }

                    for (int k = 17; k <= 24; k++) {
                        if (!attributeList.get(i)[k].equals("")) {
                            statement.setDouble(k, Double.parseDouble(attributeList.get(i)[k]));
                        } else {
                            statement.setNull(k, Types.NUMERIC);
                        }
                    }
                }

                statement.addBatch();

                if ((rowsFound + 1) % 100 == 0) {
                    statement.executeBatch(); // Execute every 100 steps.
                }

                // clean attributeList
                attributeList = new ArrayList<>();

                // read next line before looping
                // if end of file reached, line would be null
                line = br.readLine();
                if (maxLine++ > MAXIMUM_LINE) {
                    break;
                }
            }

            statement.executeBatch();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
    }

    public static byte [] ImageToByte(File file) throws FileNotFoundException{
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
                //System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
    }


    public void insertUsers() {
        String query = "INSERT INTO general_user(username, first_name, last_name, email, password, salt, is_activated) "
                + "VALUES(?,?,?,?,?,?,?)";

        try {
            PreparedStatement statement = conn.prepareStatement(query);

            String salt = UserDao.instance.getAlphaNumericString(50);

            statement.setString(1, "CvdB");
            statement.setString(2, "Christian");
            statement.setString(3, "Van Den Berge");
            statement.setString(4, "khavronayevhen@gmail.com");
            statement.setString(5, UserDao.instance.getSHA256(UserDao.instance.getSHA256("Password7")+ salt));
            statement.setString(6, salt);
            statement.setBoolean(7, true);
            statement.execute();

            salt = UserDao.instance.getAlphaNumericString(50);

            statement.setString(1, "JF");
            statement.setString(2, "Johnny");
            statement.setString(3, "Frankenstein");
            statement.setString(4, "ngotriet2908@gmail.com");
            statement.setString(5, UserDao.instance.getSHA256(UserDao.instance.getSHA256("Password7")+ salt));
            statement.setString(6, salt);
            statement.setBoolean(7, true);


            statement.execute();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void insertFavoriteLayout() {
        String query = "INSERT INTO favorite_layout(username, lid, name) "
                + "VALUES(?,?,?)";

        try {
            PreparedStatement statement = conn.prepareStatement(query);

            for(int i = 1; i < 6; i++) {
                statement.setString(1, "CvdB");
                statement.setInt(2, i);
                statement.setString(3, "Favorite " + i);
                statement.execute();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }


    public void insertImageUsers() {
        String query = "INSERT INTO user_picture(username, picname, picture) "
                + "VALUES(?,?,?)";

        try {
            PreparedStatement statement = conn.prepareStatement(query);


            statement.setString(1, "CvdB");
            statement.setString(2, "Christian");
            statement.setBytes(3, ImageToByte(new File("src/main/webapp/img/profile_green.jpg")));
            statement.execute();


            statement.setString(1, "JF");
            statement.setString(2, "Johnny");
            statement.setBytes(3, ImageToByte(new File("src/main/webapp/img/profile_red.jpg")));
            statement.execute();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void insertPremiums() {
        String query = "INSERT INTO premium_user(username, start_date, end_date) "
                + "VALUES(?,?,?)";

        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, "CvdB");
            statement.setTimestamp(2, Timestamp.valueOf(LocalDate.now().atStartOfDay()));
            statement.setTimestamp(3, Timestamp.valueOf(LocalDate.now().atStartOfDay().plusYears(1)));
            statement.execute();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }


    public void insertShoes() {
        String query = "INSERT INTO shoes(id, brand, model, stack_height_heel, stack_height_forefoot, drop_value, weight_value) "
                + "VALUES(?,?,?,?,?,?,?)";

        try {
            Workbook workbook = WorkbookFactory.create(new File("RA-data/BodyPackRuns.xlsx"));
            //System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");
            Sheet shoes = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            PreparedStatement statement = conn.prepareStatement(query);

            for (int i = shoes.getFirstRowNum() + 1; i <= shoes.getLastRowNum(); i++) {
                Row row = shoes.getRow(i);

                for(int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);

                    if (j == row.getFirstCellNum()) {
                        statement.setInt(1, Integer.parseInt(dataFormatter.formatCellValue(cell)));
                    } else if (j == row.getFirstCellNum() + 1) {
                        statement.setString(2, dataFormatter.formatCellValue(cell));
                    } else if (j == row.getFirstCellNum() + 2) {
                        statement.setString(3, dataFormatter.formatCellValue(cell));
                    } else if (j == row.getFirstCellNum() + 3) {
                        statement.setInt(4, Integer.parseInt(dataFormatter.formatCellValue(cell)));
                    } else if (j == row.getFirstCellNum() + 4) {
                        statement.setInt(5, Integer.parseInt(dataFormatter.formatCellValue(cell)));
                    } else if (j == row.getFirstCellNum() + 5) {
                        statement.setInt(6, Integer.parseInt(dataFormatter.formatCellValue(cell)));
                    } else if (j == row.getFirstCellNum() + 6) {
                        statement.setInt(7, Integer.parseInt(dataFormatter.formatCellValue(cell)));
                    }
                }

                statement.execute();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSurfaces() {
        String query = "INSERT INTO surface(id, description, type) "
                + "VALUES(?,?,?)";

        try (BufferedReader br = new BufferedReader(new FileReader("RA-data/surface_201905060849.csv"))) {
            PreparedStatement statement = conn.prepareStatement(query);

            // read the first line from the text file
            String line = br.readLine();
            line = br.readLine();

            while (line != null) {
                String[] attributes = line.split(",");

                statement.setInt(1, Integer.parseInt(attributes[0]));
                statement.setString(2, attributes[1]);
                statement.setString(3, attributes[2]);

                statement.execute();

                line = br.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }


    public void insertTerms() {
        String query = "INSERT INTO term(name, meaning) "
                + "VALUES(?,?)";

        try {
            Workbook workbook = WorkbookFactory.create(new File("RA-data/terms.xlsx"));
            Sheet terms = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            PreparedStatement statement = conn.prepareStatement(query);

            for (Row row: terms) {
                Cell cell = row.getCell(0);
                String att_name = dataFormatter.formatCellValue(cell);
                cell = row.getCell(1);
                String att_content = dataFormatter.formatCellValue(cell);

                statement.setString(1, att_name);
                statement.setString(2, att_content);

                statement.execute();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertLayout(int runs) {
        String query = "INSERT INTO layout(lid, run_id, name) "
                + "VALUES(?,?,?)";

        try {

            DataFormatter dataFormatter = new DataFormatter();

            PreparedStatement statement = conn.prepareStatement(query);

            for (int i = 1; i <= runs; i++) {


                for(int j = 1; j <= 5; j++) {
                    if (i == 9 && j > 1) {
                        continue;
                    }


                    statement.setInt(1, j);
                    statement.setInt(2, i);
                    statement.setString(3, "Layout " + j);
                    statement.execute();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public int insertRuns() {
        String query = "INSERT INTO run(date, bodypackFile, id, username, distance, duration," +
                "shoes_id, surface_id, description, remarks, stravaLink, name, default_layout, current_layout) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?, ?)";

        int rowsAdded = 0;

        try {
            Workbook workbook = WorkbookFactory.create(new File("RA-data/BodyPackRuns.xlsx"));
            Sheet run = workbook.getSheetAt(1);
            DataFormatter dataFormatter = new DataFormatter();

            PreparedStatement statement = conn.prepareStatement(query);
            for (int i = run.getFirstRowNum() + 1; i <= run.getLastRowNum(); i++) {
                Row row = run.getRow(i);

                if (row == null) {
                    continue;
                }

                for (int j = row.getFirstCellNum(); j <= row.getFirstCellNum() + 10; j++) {
                    Cell cell = row.getCell(j);

                    if (j == row.getFirstCellNum()) {
                        // date
                        statement.setTimestamp(1, new Timestamp(DATE_FORMAT_RUN.parse(dataFormatter.formatCellValue(cell)).getTime()));
                    } else if (j == row.getFirstCellNum() + 3 ||
                            j == row.getFirstCellNum() + 8 ||
                            j == row.getFirstCellNum() + 9 ||
                            j == row.getFirstCellNum() + 10) {

                        // varchars
                        if (dataFormatter.formatCellValue(cell).equals("")) {
                            statement.setNull(j, Types.VARCHAR);
                        } else {
                            statement.setString(j, dataFormatter.formatCellValue(cell));
                        }
                    } else if (j == row.getFirstCellNum() + 1) {
                        // bodypack
                        statement.setString(j, dataFormatter.formatCellValue(cell));
                    } else {
                        // ints
                        if (j == row.getFirstCellNum() + 2) {
                            // id
                            statement.setInt(j, Integer.valueOf(dataFormatter.formatCellValue(cell)));
                        } else if (j == row.getFirstCellNum() + 6) {
                            // shoes
                            String shoes = dataFormatter.formatCellValue(cell);

                            if (!shoes.equals("null")) {
                                statement.setInt(j, Integer.valueOf(shoes));
                            } else {
                                statement.setNull(j, Types.INTEGER);
                            }
                        } else if (j == row.getFirstCellNum() + 4) {
                            // distance
                            String distanceString = dataFormatter.formatCellValue(cell);

                            if (!distanceString.equals("null")) {
                                int totalDistance = (int) (Double.valueOf(distanceString) * 1000);
                                statement.setInt(j, totalDistance);
                            } else {
                                statement.setNull(j, Types.INTEGER);
                            }
                        } else if (j == row.getFirstCellNum() + 5) {
                            // time
                            String timeString = dataFormatter.formatCellValue(cell);

                            if (!timeString.equals("")) {
                                String[] timeArray = timeString.split(":");
                                int hours = Integer.valueOf(timeArray[0]);
                                int min = Integer.valueOf(timeArray[1]);
                                int sec = Integer.valueOf(timeArray[2]);

                                int totalSeconds = hours * 3600 + min * 60 + sec;

                                statement.setInt(j, totalSeconds);
                            } else {
                                statement.setNull(j, Types.INTEGER);
                            }
                        } else if (j == row.getFirstCellNum() + 7) {
                            // surface
                            statement.setInt(j, 0);
                        }

                    }
                }
                statement.setString(13, "{\"count\":50,\"layout\":[{\"typeName\":\"individual\",\"indicatorName\":\"axtibacc_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"axtibacc_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibimpact_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibimpact_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"axsacacc_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"axsacacc_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"sacimpact_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"sacimpact_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"brakingforce_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"brakingforce_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"pushoffpower_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"pushoffpower_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibintrot_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibintrot_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"vll_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"vll_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"axtibacc_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"axtibacc_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"tibimpact_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"tibimpact_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"axsacacc_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"axsacacc_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"sacimpact_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"sacimpact_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"brakingforce_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"brakingforce_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"pushoffpower_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"pushoffpower_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"tibintrot_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"tibintrot_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"vll_left\"},{\"typeName\":\"graph\",\"indicatorName\":\"vll_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"axtibacc_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"axtibacc_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"tibimpact_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"tibimpact_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"axsacacc_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"axsacacc_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"sacimpact_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"sacimpact_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"brakingforce_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"brakingforce_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"pushoffpower_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"pushoffpower_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"tibintrot_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"tibintrot_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"vll_left\"},{\"typeName\":\"distribution\",\"indicatorName\":\"vll_right\"},{\"typeName\":\"distribution\",\"indicatorName\":\"speed\"},{\"typeName\":\"graph\",\"indicatorName\":\"speed\"}]}\t");

                String[] names = {"Typical run", "Morning run", "Exercise", "Run with friends", "Training", "Run for fun", "Run to school", "Zombie run", "Boring run"};

                statement.setString(12, names[rowsAdded]);
                statement.setInt(14, 1);
                rowsAdded++;
                statement.execute();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return rowsAdded;
    }



    public boolean importSchema(InputStream in) {
        System.out.print("Creating schema...");
        Scanner s = new Scanner(in);
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        try
        {
            st = conn.createStatement();
            while (s.hasNext())
            {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/"))
                {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                if (line.trim().length() > 0)
                {
                    st.execute(line);
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
            return false;
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException se) {
                    System.out.println("Failed to build schema:(");
                    se.printStackTrace();
                    return false;
                }
            }
            System.out.println("Finish create schema");
            return true;
        }
    }

    public void addStepsCountToRun() {
        String query = "UPDATE project_new.run AS run " +
                "SET steps = s.steps " +
                "FROM (SELECT step.run_id, COUNT(*) AS steps FROM project_new.step AS step GROUP BY step.run_id) AS s " +
                "WHERE id = s.run_id";

        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.execute();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            File schema = new File("src/main/java/utwente/team2/databaseBackup/schema.sql");
            InputStream targetStream = new FileInputStream(schema);

            DataExporter pd = new DataExporter();

            System.out.println("Starting...");

            if (pd.importSchema(targetStream)) {
                pd.insertShoes();
                pd.insertSurfaces();
                pd.insertTerms();
                pd.insertUsers();
                pd.insertPremiums();
                int runs = pd.insertRuns();
                pd.insertLayout(runs);
                pd.insertImageUsers();
                pd.insertSteps();
                pd.addStepsCountToRun();
                pd.insertFavoriteLayout();
                pd.createBaseLineMole();
                String[] indicators = {"axtibacc_right", "tibimpact_right", "axsacacc_right", "sacimpact_right",
                        "brakingforce_right", "pushoffpower_right", "tibintrot_right", "vll_right",
                        "axtibacc_left", "tibimpact_left", "axsacacc_left", "sacimpact_left",
                        "brakingforce_left", "pushoffpower_left", "tibintrot_left", "vll_left"};

                for(int i = 0; i < indicators.length; i++) {
                    pd.UpdateBaseLine(pd.getBaseLine(indicators[i]),indicators[i]);
                }
            }
            System.out.println("Done!");
            pd.conn.close();

        } catch (FileNotFoundException e) {
            System.out.println("schema file not found");
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
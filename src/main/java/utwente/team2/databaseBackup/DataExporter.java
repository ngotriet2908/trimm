package utwente.team2.databaseBackup;

import org.apache.poi.ss.usermodel.*;
import utwente.team2.dao.UserDao;

import java.io.*;
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
        String query = "INSERT INTO general_user(username, first_name, last_name, email, password, salt) "
                + "VALUES(?,?,?,?,?,?)";

        try {
            PreparedStatement statement = conn.prepareStatement(query);

            String salt = UserDao.instance.getAlphaNumericString(50);

            statement.setString(1, "CvdB");
            statement.setString(2, "Christian");
            statement.setString(3, "Van Den Berge");
            statement.setString(4, "khavronayevhen@gmail.com");
            statement.setString(5, UserDao.instance.getSHA256(UserDao.instance.getSHA256("Password7")+ salt));
            statement.setString(6, salt);
            statement.execute();

            salt = UserDao.instance.getAlphaNumericString(50);

            statement.setString(1, "JF");
            statement.setString(2, "Johnny");
            statement.setString(3, "Frankenstein");
            statement.setString(4, "ngotriet2908@gmail.com");
            statement.setString(5, UserDao.instance.getSHA256(UserDao.instance.getSHA256("Password7")+ salt));
            statement.setString(6, salt);

            statement.execute();
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


    public void insertRuns() {
        String query = "INSERT INTO run(date, bodypackFile, id, username, distance, duration," +
                "shoes_id, surface_id, description, remarks, stravaLink, name, layout) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
                statement.setString(13, "{\"count\":24,\"layout\":[{\"typeName\":\"individual\",\"indicatorName\":\"axtibacc_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"axtibacc_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibimpact_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibimpact_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"axsacacc_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"axsacacc_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"sacimpact_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"sacimpact_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"brakingforce_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"brakingforce_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"pushoffpower_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"pushoffpower_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibintrot_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"tibintrot_right\"},{\"typeName\":\"individual\",\"indicatorName\":\"vll_left\"},{\"typeName\":\"individual\",\"indicatorName\":\"vll_right\"},{\"typeName\":\"graph\",\"indicatorName\":\"axtibacc\"},{\"typeName\":\"graph\",\"indicatorName\":\"tibimpact\"},{\"typeName\":\"graph\",\"indicatorName\":\"axsacacc\"},{\"typeName\":\"graph\",\"indicatorName\":\"sacimpact\"},{\"typeName\":\"graph\",\"indicatorName\":\"brakingforce\"},{\"typeName\":\"graph\",\"indicatorName\":\"pushoffpower\"},{\"typeName\":\"graph\",\"indicatorName\":\"tibintrot\"},{\"typeName\":\"graph\",\"indicatorName\":\"vll\"}]}");


                String[] namee = {"Typical run", "Morning Run", "Exercise", "Run with friends", "Training", "Run for fun", "Run to school"};

                statement.setString(12, namee[(int)(Math.random() * 7)]);
                statement.execute();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        } catch (SQLException se) {
            se.printStackTrace();
        }
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
                pd.insertRuns();
                pd.insertImageUsers();
                pd.insertSteps();
                pd.addStepsCountToRun();
                System.out.println("Done!");

                pd.conn.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("schema file not found");
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class User {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private Integer isPremium;
    private Date startDate;
    private Date endDate;

    private List<Run> runsList;

    // total stats
    private Integer totalRuns;
    private Integer totalTime;
    private Integer totalDistance;
    private Integer totalSteps;


    public User(String username, String password, String firstName, String lastName, String email, String phone,
                String recoveryQuestion1, String recoveryAnswer1, String recoveryQuestion2, String recoveryAnswer2,
                Integer isPremium, Date startDate, Date endDate, List<Run> runsList, Integer totalRuns, Integer totalTime,
                Integer totalDistance, Integer totalSteps) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.isPremium = isPremium;
        this.startDate = startDate;
        this.endDate = endDate;
        this.runsList = runsList;
        this.totalRuns = totalRuns;
        this.totalTime = totalTime;
        this.totalDistance = totalDistance;
        this.totalSteps = totalSteps;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.firstName = null;
        this.lastName = null;
        this.email = null;
        this.phone = null;
        this.isPremium = null;
        this.startDate = null;
        this.endDate = null;
        this.runsList = null;
        this.totalRuns = null;
        this.totalTime = null;
        this.totalDistance = null;
        this.totalSteps = null;
    }

    public User() {
        this.username = null;
        this.password = null;
        this.firstName = null;
        this.lastName = null;
        this.email = null;
        this.phone = null;
        this.isPremium = null;
        this.startDate = null;
        this.endDate = null;
        this.runsList = null;
        this.totalRuns = null;
        this.totalTime = null;
        this.totalDistance = null;
        this.totalSteps = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(Integer isPremium) {
        this.isPremium = isPremium;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Run> getRunsList() {
        return runsList;
    }

    public void setRunsList(List<Run> runsList) {
        this.runsList = runsList;
    }

    public Integer getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(Integer totalRuns) {
        this.totalRuns = totalRuns;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Integer totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    //    public void sortArray() {
//        Collections.sort(runs, new Comparator<Run>() {
//            @Override
//            public int compare(Run o1, Run o2) {
//                return o1.getRunId().compareTo(o2.getRunId());
//            }
//        });
//        Collections.reverse(runs);
//    }
}

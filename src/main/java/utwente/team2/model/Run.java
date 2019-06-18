package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
public class Run {
    private Integer id;
    private String username;
    private Date date;
    private String name;
    private String bodypackfile;
    private Integer distance;
    private Integer duration;
    private Integer steps;

    private Integer shoesId;
    private Integer surfaceId;
    private String shoesname;

    public Run(Integer id, String username, Date date, String name, String bodypackfile, Integer distance, Integer duration, Integer steps, Integer shoesId,
               Integer surfaceId, String shoesname, String description, String remarks, String stravaLink) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.name = name;
        this.bodypackfile = bodypackfile;
        this.distance = distance;
        this.duration = duration;
        this.steps = steps;
        this.shoesId = shoesId;
        this.surfaceId = surfaceId;
        this.shoesname = shoesname;
        this.description = description;
        this.remarks = remarks;
        this.stravaLink = stravaLink;
    }

    public String getShoesname() {
        return shoesname;
    }

    public void setShoesname(String shoesname) {
        this.shoesname = shoesname;
    }

    private String description;
    private String remarks;

    private String stravaLink;

    public Run(Integer id, String username, Date date, String name, String bodypackfile, Integer distance, Integer duration,
               Integer steps, Integer shoesId, Integer surfaceId, String description, String remarks, String stravaLink) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.name = name;
        this.bodypackfile = bodypackfile;
        this.distance = distance;
        this.duration = duration;
        this.steps = steps;
        this.shoesId = shoesId;
        this.surfaceId = surfaceId;
        this.description = description;
        this.remarks = remarks;
        this.stravaLink = stravaLink;
    }

    public Run() {
        this.id = null;
        this.username = null;
        this.date = null;
        this.name = null;
        this.bodypackfile = null;
        this.distance = null;
        this.duration = null;
        this.steps = null;
        this.shoesId = null;
        this.surfaceId = null;
        this.description = null;
        this.remarks = null;
        this.stravaLink = null;
    }

    public Run(Integer id) {
        this.id = id;
        this.username = null;
        this.date = null;
        this.name = null;
        this.bodypackfile = null;
        this.distance = null;
        this.duration = null;
        this.steps = null;
        this.shoesId = null;
        this.surfaceId = null;
        this.description = null;
        this.remarks = null;
        this.stravaLink = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBodypackfile() {
        return bodypackfile;
    }

    public void setBodypackfile(String bodypackfile) {
        this.bodypackfile = bodypackfile;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Integer getShoesId() {
        return shoesId;
    }

    public void setShoesId(Integer shoesId) {
        this.shoesId = shoesId;
    }

    public Integer getSurfaceId() {
        return surfaceId;
    }

    public void setSurfaceId(Integer surfaceId) {
        this.surfaceId = surfaceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStravaLink() {
        return stravaLink;
    }

    public void setStravaLink(String stravaLink) {
        this.stravaLink = stravaLink;
    }
}

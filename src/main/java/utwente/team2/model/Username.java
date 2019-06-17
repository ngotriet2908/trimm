package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Username {
    private String username;
    private boolean exists;

    public Username(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}

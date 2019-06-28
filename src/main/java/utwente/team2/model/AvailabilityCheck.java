package utwente.team2.model;

public class AvailabilityCheck {
    private boolean exists;

    public AvailabilityCheck(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}

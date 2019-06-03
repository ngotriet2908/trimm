package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement
public class Step {
    private int stepID;
    private String time;
    private int surface;
    private String ic_right;
    private String to_right;
    private BigDecimal axtibacc_right;
    private BigDecimal tibimpact_right;
    private BigDecimal axsacacc_right;
    private BigDecimal sacimpact_right;
    private BigDecimal brakingforce_right;
    private BigDecimal pushoffpower_right;
    private BigDecimal tibintrot_right;
    private BigDecimal vll_right;
    private String ic_left;
    private String to_left;
    private BigDecimal axtibacc_left;
    private BigDecimal tibimpact_left;
    private BigDecimal axsacacc_left;
    private BigDecimal sacimpact_left;
    private BigDecimal brakingforce_left;
    private BigDecimal pushoffpower_left;
    private BigDecimal tibintrot_left;
    private BigDecimal vll_left;

    public Step(int stepID, String time, int surface, String ic_right, String to_right, BigDecimal axtibacc_right, BigDecimal tibimpact_right, BigDecimal axsacacc_right, BigDecimal sacimpact_right, BigDecimal brakingforce_right, BigDecimal pushoffpower_right, BigDecimal tibintrot_right, BigDecimal vll_right, String ic_left, String to_left, BigDecimal axtibacc_left, BigDecimal tibimpact_left, BigDecimal axsacacc_left, BigDecimal sacimpact_left, BigDecimal brakingforce_left, BigDecimal pushoffpower_left, BigDecimal tibintrot_left, BigDecimal vll_left) {
        this.stepID = stepID;
        this.time = time;
        this.surface = surface;
        this.ic_right = ic_right;
        this.to_right = to_right;
        this.axtibacc_right = axtibacc_right;
        this.tibimpact_right = tibimpact_right;
        this.axsacacc_right = axsacacc_right;
        this.sacimpact_right = sacimpact_right;
        this.brakingforce_right = brakingforce_right;
        this.pushoffpower_right = pushoffpower_right;
        this.tibintrot_right = tibintrot_right;
        this.vll_right = vll_right;
        this.ic_left = ic_left;
        this.to_left = to_left;
        this.axtibacc_left = axtibacc_left;
        this.tibimpact_left = tibimpact_left;
        this.axsacacc_left = axsacacc_left;
        this.sacimpact_left = sacimpact_left;
        this.brakingforce_left = brakingforce_left;
        this.pushoffpower_left = pushoffpower_left;
        this.tibintrot_left = tibintrot_left;
        this.vll_left = vll_left;
    }

    public Step(int stepID) {
        this.stepID = stepID;
        time = "null";
        ic_left = "null";
        ic_right = "null";
        to_left = "null";
        to_right = "null";
    }
    public int getStepID() {
        return stepID;
    }

    public void setStepID(int stepID) {
        this.stepID = stepID;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public int getSurface() {
        return surface;
    }

    public void setSurface(int surface) {
        this.surface = surface;
    }
    public String getIc_right() {
        return ic_right;
    }

    public void setIc_right(String ic_right) {
        this.ic_right = ic_right;
    }
    public String getTo_right() {
        return to_right;
    }

    public void setTo_right(String to_right) {
        this.to_right = to_right;
    }
    public BigDecimal getAxtibacc_right() {
        return axtibacc_right;
    }

    public void setAxtibacc_right(BigDecimal axtibacc_right) {
        this.axtibacc_right = axtibacc_right;
    }
    public BigDecimal getTibimpact_right() {
        return tibimpact_right;
    }

    public void setTibimpact_right(BigDecimal tibimpact_right) {
        this.tibimpact_right = tibimpact_right;
    }
    public BigDecimal getAxsacacc_right() {
        return axsacacc_right;
    }

    public void setAxsacacc_right(BigDecimal axsacacc_right) {
        this.axsacacc_right = axsacacc_right;
    }
    public BigDecimal getSacimpact_right() {
        return sacimpact_right;
    }

    public void setSacimpact_right(BigDecimal sacimpact_right) {
        this.sacimpact_right = sacimpact_right;
    }
    public BigDecimal getBrakingforce_right() {
        return brakingforce_right;
    }

    public void setBrakingforce_right(BigDecimal brakingforce_right) {
        this.brakingforce_right = brakingforce_right;
    }
    public BigDecimal getPushoffpower_right() {
        return pushoffpower_right;
    }

    public void setPushoffpower_right(BigDecimal pushoffpower_right) {
        this.pushoffpower_right = pushoffpower_right;
    }
    public BigDecimal getTibintrot_right() {
        return tibintrot_right;
    }

    public void setTibintrot_right(BigDecimal tibintrot_right) {
        this.tibintrot_right = tibintrot_right;
    }
    public BigDecimal getVll_right() {
        return vll_right;
    }

    public void setVll_right(BigDecimal vll_right) {
        this.vll_right = vll_right;
    }
    public String getIc_left() {
        return ic_left;
    }

    public void setIc_left(String ic_left) {
        this.ic_left = ic_left;
    }
    public String getTo_left() {
        return to_left;
    }

    public void setTo_left(String to_left) {
        this.to_left = to_left;
    }
    public BigDecimal getAxtibacc_left() {
        return axtibacc_left;
    }

    public void setAxtibacc_left(BigDecimal axtibacc_left) {
        this.axtibacc_left = axtibacc_left;
    }
    public BigDecimal getTibimpact_left() {
        return tibimpact_left;
    }

    public void setTibimpact_left(BigDecimal tibimpact_left) {
        this.tibimpact_left = tibimpact_left;
    }
    public BigDecimal getAxsacacc_left() {
        return axsacacc_left;
    }

    public void setAxsacacc_left(BigDecimal axsacacc_left) {
        this.axsacacc_left = axsacacc_left;
    }
    public BigDecimal getSacimpact_left() {
        return sacimpact_left;
    }

    public void setSacimpact_left(BigDecimal sacimpact_left) {
        this.sacimpact_left = sacimpact_left;
    }
    public BigDecimal getBrakingforce_left() {
        return brakingforce_left;
    }

    public void setBrakingforce_left(BigDecimal brakingforce_left) {
        this.brakingforce_left = brakingforce_left;
    }
    public BigDecimal getPushoffpower_left() {
        return pushoffpower_left;
    }

    public void setPushoffpower_left(BigDecimal pushoffpower_left) {
        this.pushoffpower_left = pushoffpower_left;
    }
    public BigDecimal getTibintrot_left() {
        return tibintrot_left;
    }

    public void setTibintrot_left(BigDecimal tibintrot_left) {
        this.tibintrot_left = tibintrot_left;
    }
    public BigDecimal getVll_left() {
        return vll_left;
    }

    public void setVll_left(BigDecimal vll_left) {
        this.vll_left = vll_left;
    }
}

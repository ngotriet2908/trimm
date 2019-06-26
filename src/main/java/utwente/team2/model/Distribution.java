package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement
public class Distribution implements Card {
    private String cardTypeName;
    private String name;
    private List<BigDecimal> pointX;
    private List<BigDecimal> pointY;

    public Distribution() {
        pointY = new ArrayList<>();
        pointX = new ArrayList<>();

        cardTypeName = "distribution";
    }

    public Distribution(List<BigDecimal> pointX, List<BigDecimal> pointY, String name) {
        this.pointX = pointX;
        this.pointY = pointY;

        this.name = name;
        this.cardTypeName = "distribution";
    }

    public Distribution(List<BigDecimal> points, String name) {
        pointX = points;
        Collections.sort(pointX);

        this.name = name;
        this.cardTypeName = "distribution";
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BigDecimal> getPointX() {
        return pointX;
    }

    public void setPointX(List<BigDecimal> pointX) {
        this.pointX = pointX;
    }

    public List<BigDecimal> getPointY() {
        return pointY;
    }

    public void setPointY(List<BigDecimal> pointY) {
        this.pointY = pointY;
    }

    public void getDistribution(String indicator) {
        int MAX_POINT = 50;

        if (indicator.equals("speed")) {
            pointX.remove(pointX.size() - 1);
        }

        BigDecimal minP = pointX.get(0);
        BigDecimal maxP = pointX.get(pointX.size() - 1);
        BigDecimal segment = (maxP.subtract(minP)).divide(BigDecimal.valueOf(MAX_POINT), BigDecimal.ROUND_UP);

        int[] number = new int[MAX_POINT];

        for (int i = 0; i < MAX_POINT; i++) {
            number[i] = 0;
        }

        for (int i = 0; i < pointX.size(); i++) {
            for (int j = 0; j < MAX_POINT; j++) {

                BigDecimal tmp = minP;
                tmp = tmp.add(segment.multiply(BigDecimal.valueOf(j)));

                BigDecimal tmpp = minP;
                tmpp = tmpp.add(segment.multiply(BigDecimal.valueOf(j + 1)));

                if (((pointX.get(i).compareTo(tmp) > 0) && (pointX.get(i).compareTo(tmpp) < 0)) ||
                        (pointX.get(i).compareTo(tmp)) == 0 ||
                        (pointX.get(i).compareTo(tmpp)) == 0) {

                    number[j]++;
                    break;
                }
            }
        }

        List<BigDecimal> pointXtmp = new ArrayList<>();
        List<BigDecimal> pointYtmp = new ArrayList<>();

        for (int i = 0; i < MAX_POINT; i++) {
            if (segment.compareTo(BigDecimal.ONE) < 0) {
                pointXtmp.add(segment.multiply(BigDecimal.valueOf(i)).add(minP).setScale(3, BigDecimal.ROUND_UP));
            } else {
                pointXtmp.add(segment.multiply(BigDecimal.valueOf(i)).add(minP).setScale(0, BigDecimal.ROUND_UP));
            }

            pointYtmp.add(BigDecimal.valueOf(number[i]));
        }
        pointX = pointXtmp;
        pointY = pointYtmp;
    }

}
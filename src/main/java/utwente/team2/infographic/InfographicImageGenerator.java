package utwente.team2.infographic;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class InfographicImageGenerator {
    private String name;
    private String date;
    private String shoes;
    private String duration;
    private String distance;
    private String steps;


    public InfographicImageGenerator(String name, String date, String shoes, String duration, String distance, String steps) {
        this.name = name;
        this.date = date;
        this.shoes = shoes;

        int seconds = Integer.parseInt(duration);
        int hour = Math.round(seconds / 3600);
        int minutes = Math.round((seconds % 3600) / 60);
        seconds = Math.round((seconds % 3600) % 60);

        this.duration = hour + ":" + minutes + ":" + seconds;
        this.distance = distance;
        this.steps = steps;
    }

    public BufferedImage generate() {
        BufferedImage image;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            image = ImageIO.read(classLoader.getResourceAsStream("../../img/infographic/template.png"));
            Graphics g = image.getGraphics();

            try {
                ClassLoader classLoaderr = getClass().getClassLoader();
                InputStream stream = classLoaderr.getResourceAsStream("../../fonts/Lato-Regular.ttf");
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(200.0f);

                g.setFont(font.deriveFont(font.getSize() * 0.2F));
                g.setColor(Color.BLACK);
                g.drawString(name, 34, 160);
                g.drawString(date, 35, 230);

                g.drawString(duration, 35, 950);
                g.drawString(shoes, 535, 1470);
                g.drawString(distance, 35, 550);
                g.drawString(steps, 975, 850);

                g.dispose();
            } catch (FontFormatException e) {
                e.printStackTrace();
            }

            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

package utwente.team2.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageProcessing {
    private String name;
    private String date;
    private String shoes;
    private String duration;
    private String distance;
    private String steps;


    public ImageProcessing(String name, String date, String shoes, String duration, String distance, String steps) {
        this.name = name;
        this.date = date;
        this.shoes = shoes;


        int seconds = Integer.parseInt(duration);
        int hour = Math.round(seconds/3600);
        int minutes = Math.round((seconds % 3600) / 60);
        seconds = Math.round((seconds % 3600) % 60);


        this.duration = hour + ":" + minutes + ":" + seconds;
        this.distance = distance;
        this.steps = steps;
    }

    public BufferedImage generate() {
        System.out.println("prepare to read image");

        BufferedImage image = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            image = ImageIO.read(classLoader.getResourceAsStream("../../img/e-ink/eink_export_image_template.png"));
            System.out.println("reading image");
            //Name
            Graphics g = image.getGraphics();
            //g.setFont(new Font("Helvetica", Font.PLAIN, 400));
            try {
                ClassLoader classLoaderr = getClass().getClassLoader();
                InputStream stream = classLoaderr.getResourceAsStream("../../img/e-ink/Rachella.ttf");
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(400f);
                g.setFont(font);
                g.setColor(Color.BLACK);
                g.drawString(name, 1900 , 550);
                g.dispose();
            } catch (FontFormatException e) {
                e.printStackTrace();
            }


            //Date
            g = image.getGraphics();
            g.setFont(new Font("TimesRoman", Font.PLAIN, 200));
            g.setColor(Color.BLACK);
            g.drawString(date, 250 , 1700);
            g.dispose();

            //Duration
            g = image.getGraphics();
            g.setFont(new Font("TimesRoman", Font.PLAIN, 200));
            g.setColor(Color.BLACK);
            g.drawString(duration, 1900 , 1700);
            g.dispose();

            //Shoes
            g = image.getGraphics();
            g.setFont(new Font("TimesRoman", Font.PLAIN, 200));
            g.setColor(Color.BLACK);
            g.drawString(shoes, 3800 , 1700);
            g.dispose();

            //Distance
            g = image.getGraphics();
            g.setFont(new Font("TimesRoman", Font.PLAIN, 200));
            g.setColor(Color.BLACK);
            g.drawString(distance, 250 , 3600);
            g.dispose();

            //Steps
            g = image.getGraphics();
            g.setFont(new Font("TimesRoman", Font.PLAIN, 200));
            g.setColor(Color.BLACK);
            g.drawString(steps, 4000 , 3600);
            g.dispose();

            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            //Create Document Object
//            com.itextpdf.text.Document convertPngToPdf = new com.itextpdf.text.Document();
//            //Create PdfWriter for Document to hold physical file
//            //Change the PDF file path to suit to your needs
//            PdfWriter.getInstance(convertPngToPdf, new FileOutputStream("src/main/webapp/img/e-ink/result.pdf"));
//            convertPngToPdf.setPageSize(new com.itextpdf.text.Rectangle(5000,6667));
//            convertPngToPdf.open();
//            //Get the PNG image to Convert to PDF
//            //getImage of PngImage class is a static method
//            //Edit the file location to suit to your needs
//            convertPngToPdf.add(com.itextpdf.text.Image.getInstance("src/main/webapp/img/e-ink/result.png"));
//            //Close Document
//            convertPngToPdf.close();
//            System.out.println("Converted and stamped PNG Image in a PDF Document Using iText and Java");
//        } catch (Exception e) {
//
//        }
        return null;
    }


    public static void main(String[] args) throws Exception {
        ImageProcessing imageProcessing = new ImageProcessing("typical run", "29/08/2000",
                "Salomon", "01:00:12", "12 km", "30456");
    }
}

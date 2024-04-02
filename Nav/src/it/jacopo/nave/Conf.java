package it.jacopo.nave;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public class Conf {
	
	final static String _RESOURCES_IMG_PATH = "resources/img/";
	
	final static String SFONDO_JPG = "sfondo.jpg";
	final static int asteroid_number = 15;
	final static int _FPSms = 16;
	final static int MAX_AGGIUNTE = 10;
	final static int Level_timer = 20000;
	final static int Level_Total = 10;
	final static int FRAME_HEIGHT = 800;
	final static int FRAME_WIDTH = 1200;
	
	//Asteroide
	
	final static double ACCELERATION_CHANGE = 0.1; // Piccole variazioni nella velocità
    final static double MIN_SPEED = 0.5;
    final static double MAX_SPEED = 2.0;
     // Direzione base verso destra
    final static double MAX_ANGLE_VARIATION = Math.PI / 18; // Incremento della variazione angolare per una curvatura maggiore
	
	//Methods
	
	public static Polygon getPolygonFromImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        Polygon poly = new Polygon();

        // Scansione dei pixel per individuare quelli non trasparenti e aggiungere i loro vertici al poligono
        for (int y = 0; y < height; y+=2) {
            for (int x = 0; x < width; x+=2) {
                if ((img.getRGB(x, y) >> 24) != 0x00) { // Se il pixel non è trasparente
                    poly.addPoint(x, y);
                }
            }
        }
        return poly;
	}

	public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Crea un buffered image con trasparenza
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Disegna l'immagine su buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        
        bGr.dispose();

        // Restituisce il buffered image
        return bimage;
    }
}

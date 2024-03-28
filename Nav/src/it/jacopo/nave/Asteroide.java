package it.jacopo.nave;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

public class Asteroide extends GameObject {
    int centroX = 0;
    int centroY = 0;
    double raggio = new Random().nextInt(20) + 40;
    int primax = 0;
    int primay = 0;
    int npunti = 16;
    int rand1, rand2;
    int x1, y1;
    Image image;
    double angoloRotazione; // Angolo di rotazione per la rotazione casuale

    public Asteroide(String nome, String imagePath) {
    	this.name=nome;
    	
    	BufferedImage originalImage = null;
    	int newWidth = 0;
    	int newHeight = 0;
    	 // Caricamento dell'immagine
    	try {
            originalImage = ImageIO.read(new File(imagePath));
            // Calcola le nuove dimensioni dell'immagine riducendola 
            newWidth = (int) (originalImage.getWidth(null) * 0.2); // 80% dell'originale
            newHeight = (int) (originalImage.getHeight(null) * 0.2); // 80% dell'originale
            
            // Ridimensiona l'immagine
            this.image = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
        } catch (IOException e) {
            e.printStackTrace();
            this.image = null; // Gestisci l'errore impostando l'immagine a null
        }
    	
        x = 0;
        y = 0;
        this.speed = 2.0 + Math.random() * 3.0; // Velocità casuale da 2.0 a 5.0

        shape = getPolygonFromImage(toBufferedImage(this.image));

        // Imposta una velocità di rotazione casuale
        angoloRotazione = (Math.random() * 2 - 1) * Math.PI / 180; // Rotazione casuale da -1 a 1 grado per frame
    }
    
    private BufferedImage toBufferedImage(Image img) {
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
    
    private Polygon getPolygonFromImage(BufferedImage img) {
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

    @Override
    void draw(Graphics2D g) {
        // Controlla che l'immagine non sia null e che il gioco non sia in pausa
        if (this.image != null /*&& !gameStopped*/) {
            // Apply transformations to the graphics context to draw the image
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            at.rotate(angolo);
            
            // Draw the asteroid image with the current transformations
            g.drawImage(this.image, at, null);

            // Set the stroke and color for the polygon outline
            g.setStroke(new BasicStroke(3));
            g.setColor(Color.RED);
            
            // Now transform the polygon in the same way as the image
            Shape transformedShape = at.createTransformedShape(shape);
            
            // Draw the outline of the polygon
            g.draw(transformedShape);
        } else {
            // Se l'immagine non è stata caricata, disegna un placeholder
            super.draw(g); // Or draw the base shape as a fallback
        }
    }


    @Override
    void updateMovement() {
        // Constants for asteroid movement
        final double ACCELERATION_CHANGE = 0.05; // How much the asteroid's speed can change each frame
        final double ANGLE_CHANGE = Math.PI / 180; // One degree in radians
        final double MIN_SPEED = 0.5; // Minimum speed of the asteroid
        final double MAX_SPEED = 2.0; // Maximum speed of the asteroid

        // Add randomness to speed and direction
        speed += (Math.random() - 0.5) * ACCELERATION_CHANGE; // Random change in speed
        angolo += (Math.random() - 0.5) * ANGLE_CHANGE; // Random change in direction

        // Ensure that the speed stays within bounds
        speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));

        // Update the position based on the new speed and direction
        x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));

        // Update the rotation angle with a small random value to simulate rotation
        final double MIN_ANGLE_ROTATION = -Math.PI / 360; // -0.5 degrees in radians per frame
        final double MAX_ANGLE_ROTATION = Math.PI / 360;  // 0.5 degrees in radians per frame
        angoloRotazione += MIN_ANGLE_ROTATION + (Math.random() * (MAX_ANGLE_ROTATION - MIN_ANGLE_ROTATION));

        // Update the Polygon shape here
        if (image != null) {
            BufferedImage bufferedImage = toBufferedImage(image);
            shape = getPolygonFromImage(bufferedImage);
        }
    }




}


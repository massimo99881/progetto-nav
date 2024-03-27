package it.jacopo.nave;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
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
    	 // Caricamento dell'immagine
    	try {
            originalImage = ImageIO.read(new File(imagePath));
            // Calcola le nuove dimensioni dell'immagine riducendola del 60%
            int newWidth = (int) (originalImage.getWidth(null) * 0.2); // 40% dell'originale
            int newHeight = (int) (originalImage.getHeight(null) * 0.2); // 40% dell'originale
            
            // Ridimensiona l'immagine
            this.image = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
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
    	// Rotazione dell'asteroide
        angolo += angoloRotazione;
    	if (this.image != null) {
            // Disegna l'immagine dell'asteroide
    		
            g.drawImage(this.image, (int) x, (int) y, null);
            shape = getPolygonFromImage(toBufferedImage(this.image));
            g.drawPolygon(shape);
        } else {
            // Se l'immagine non è stata caricata, disegna un placeholder
            // Ad esempio, potresti disegnare un cerchio o usare il poligono già definito
            super.draw(g); // O disegna il poligono come fallback
        }
    	
    	 
    }

    @Override
    void updateMovement() {
        // Aggiorna l'angolo di rotazione per far ruotare l'asteroide attorno a se stesso
        angolo += angoloRotazione;

        // Aggiunge una variazione casuale alla velocità lineare
        double deltaSpeed = (new Random().nextDouble() - 0.5) * 0.5; // Variazione casuale da -0.5 a +0.5
        speed += deltaSpeed;

        // Limita la velocità minima e massima
        double minSpeed = 1.0; // Velocità minima
        double maxSpeed = 5.0; // Velocità massima
        speed = Math.max(minSpeed, Math.min(maxSpeed, speed));

        // Aggiorna la posizione dell'asteroide in base alla sua velocità di movimento lineare
        x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));
        shape = getPolygonFromImage(toBufferedImage(this.image));
  
    }

}


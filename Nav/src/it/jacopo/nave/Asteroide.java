package it.jacopo.nave;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

public class Asteroide extends Cache {
    Image image;
    double angoloRotazione; // Angolo di rotazione per la rotazione casuale
    final double ANGLE_BASE ;
    private int colpiSubiti = 0;
    final double ACCELERATION_CHANGE = 0.1; // Piccole variazioni nella velocità
    final double MIN_SPEED = 0.5;
    final double MAX_SPEED = 1.5;
    float opacita = 1.0f; // Opacità iniziale al 100%
    
    private AffineTransform cachedTransform;
    private double prevX, prevY, prevAngoloRotazione;
    private Pannello pannello; // Riferimento al pannello
    String name;
	Polygon shape;
	double speed;
	double angolo;
    int x, y;
    protected int raggio;
	private String immaginePath;
    
    
 // Metodo per gestire l'essere colpiti da un proiettile
    public void colpito() {
    	
    	colpiSubiti++;
        // Gestisci il colpo riducendo l'opacità
    	if(opacita>=0) {
    		opacita -= 0.1; // Riduci del 20% per ogni colpo
    	}
//    	if (colpiSubiti >= 5) {
//            System.out.println(name + " è stato distrutto");
//        }
         
    }
    
    public String getNome() {
    	return this.name;
    }

    public Asteroide(Pannello pan, String nome, String imagePath) {
    	super();
    	this.pannello = pan;
    	this.name=nome;
    	this.immaginePath = imagePath;
    	Map<String, Cache> imageCache = Singleton.getInstance().getImageCache();
    	
    	if(imageCache.get(imagePath)==null) {
    		System.out.println("***");
    	}
    	
        this.image = imageCache.get(imagePath).getImage();
    	ANGLE_BASE = (Math.random() - 0.5) * 2; // angolo traiettoria
        this.speed = 5.0 + Math.random() * 4.0; // Velocità casuale da 2.0 a 5.0
        shape = imageCache.get(imagePath).getPolygon();
        angoloRotazione = (Math.random() * 2 - 1) * Math.PI / 180;
        if (Math.random() > 0.5) {
            angoloRotazione *= -1; // Cambia il verso della rotazione
        }
        this.raggio = Math.min(this.image.getWidth(null), this.image.getHeight(null)) / 2;
    }

    void draw(Graphics2D g) {
    	if (opacita <= 0) {
            return; // Non disegnare l'asteroide se è completamente trasparente
        }
    	// Calcola il rettangolo di bounding dell'asteroide
    	
        int imageWidth = this.image.getWidth(null);
        int imageHeight = this.image.getHeight(null);
        
        // Controllo se l'asteroide è dentro i confini dello schermo
        if (x + imageWidth >= 0 && x <= pannello.width && y + imageHeight >= 0 && y <= pannello.height) {
        	if (this.image != null && opacita > 0 /* && !gameStopped */) {
        		
        		// Controlla se la posizione o l'angolo di rotazione sono cambiati
                if (cachedTransform == null || x != prevX || y != prevY || angoloRotazione != prevAngoloRotazione) {
                    // Calcola la nuova trasformazione perché lo stato è cambiato
                    cachedTransform = new AffineTransform();
                    imageWidth = this.image.getWidth(null);
                    imageHeight = this.image.getHeight(null);
                    int imageCenterX = imageWidth / 2;
                    int imageCenterY = imageHeight / 2;

                    cachedTransform.translate(x + imageCenterX, y + imageCenterY);
                    cachedTransform.rotate(angoloRotazione, 0, 0);
                    cachedTransform.translate(-imageCenterX, -imageCenterY);
                    
                  //TODO da eliminare: mostra shape asteroidi
//                    AffineTransform at2 = new AffineTransform();
//            	    at2.translate(x + imageCenterX, y + imageCenterY);
//            		at2.rotate(angoloRotazione, 0, 0);
//            	    g.draw(at2.createTransformedShape(shape));

                    // Aggiorna lo stato precedente
                    prevX = x;
                    prevY = y;
                    prevAngoloRotazione = angoloRotazione;
                }
        		
                // Imposta l'opacità dell'asteroide basandosi sul campo 'opacita'
                float safeOpacity = Math.max(0f, Math.min(opacita, 1f));  // Assicura che l'opacità sia nel range [0,1]
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safeOpacity));

                // Disegna l'immagine dell'asteroide utilizzando la trasformazione memorizzata nella cache
                g.drawImage(this.image, cachedTransform, null);

                // Reimposta l'opacità a 1.0 per non influenzare il disegno di altri oggetti
                //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                
            } 
        }
    }

    
    void updateMovement() {

        // Aggiorna la velocità con una variazione casuale
        speed += (0.5) * ACCELERATION_CHANGE;
        speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));

        // Applica una variazione angolare casuale entro un range più ampio per una curvatura maggiore della traiettoria
        double MAX_ANGLE_VARIATION = Math.PI / 18; 
        // Incremento della variazione angolare per una curvatura maggiore
        double angleVariation = (0.5) * 2 * MAX_ANGLE_VARIATION;
        angolo = ANGLE_BASE + angleVariation;

        // Aggiorna la posizione dell'asteroide basandosi sulla sua velocità e angolo aggiornati
        x -= (int) (speed * Math.cos(angolo)); // Questo sposterà l'asteroide verso sinistra
        y += (int) (speed * Math.sin(angolo));

        // Opcionalmente, aggiorna la rotazione dell'asteroide per effetti visivi
        angoloRotazione += Math.PI / 180; // Aggiusta a piacimento
    }

    
    Shape getTransf() {
        AffineTransform at = new AffineTransform();

        // Calcola il centro dell'immagine per la rotazione
        int imageWidth = this.image.getWidth(null);
        int imageHeight = this.image.getHeight(null);
        int imageCenterX = imageWidth / 2;
        int imageCenterY = imageHeight / 2;

        // Prima trasla al centro dello schermo (o alla posizione desiderata)
        at.translate(x + imageCenterX, y + imageCenterY);

        // Poi ruota attorno al centro dell'immagine
        at.rotate(angoloRotazione, 0, 0);

        // Trasla indietro in modo che l'angolo in alto a sinistra sia nella posizione corretta
        at.translate(-imageCenterX, -imageCenterY);

        // Applica la trasformazione alla forma dell'asteroide
        return at.createTransformedShape(shape);
    }
    Rectangle getBounds() {
	    return getTransf().getBounds();
	}
    
    public float getOpacita() {
    	return this.opacita;
    }
    public double getAngoloRotazione() {
    	return this.angoloRotazione;
    }
    public double getSpeed() {
    	return this.speed;
    }
    public double getAngolo() {
    	return this.angolo;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
    	this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
    	this.y = y;
    }
    public String getName() {
    	return this.name;
    }
    public int getColpiSubiti() {
        return colpiSubiti;
    }
    public String getImmaginePath() {
    	return this.immaginePath;
    }

    //setters

	public void setAngoloRotazione(double angoloRotazioneA) {
		this.angoloRotazione = angoloRotazioneA;
	}
	public void setSpeed(double speedA) {
		this.speed = speedA;
	}
	public void setAngolo(double angoloA) {
		this.angolo = angoloA;
	}
	public void setOpacita(float opacitaA) {
		this.opacita = opacitaA;
	}
	public void setColpiSubiti(int colpiSubitiA) {
		this.colpiSubiti = colpiSubitiA;
	}
}


package it.jacopo.nave;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Panello extends JPanel implements KeyListener, MouseMotionListener{
	Map<String, GameObject> obj = new HashMap<>();
	private boolean isInCollision = false;
	boolean gameStopped = false;

	Update update;
	Area area1;
	int cx = 100, cy = 100;//thread update per drift astronave
	public Panello() {
		Nav nave = new Nav("navicella1");  //nave principale	
		Nav nave2 = new Nav("navicella2"); //nave fantoccio per test collisioni
		// Posizionamento della seconda navicella in basso a destra
        nave2.x =  100; 
        nave2.y =  100; 

		obj.put(nave.nome, nave);
		obj.put(nave2.nome, nave2);
		
		// Inizializza 5 asteroidi con posizioni iniziali visibili
	    for (int i = 1; i <= 5; i++) {
	        Asteroide asteroide = new Asteroide("asteroide" + i, "asteroide" + i + ".png");
	        asteroide.x = 1110; // Tutti gli asteroidi partono dalla stessa posizione X iniziale
	        asteroide.y = 100 * i; // Distribuisce gli asteroidi verticalmente

	        // Aggiungi l'asteroide alla mappa degli oggetti
	        obj.put(asteroide.name, asteroide);
	    }
		
		
		update = new Update(obj.get("navicella1"), this);
		
		startAsteroidMovement();
	}
	
	// Metodo per avviare il thread per l'aggiornamento dell'asteroide
    private void startAsteroidMovement() {
        Thread asteroidMovementThread = new Thread(() -> {
            while (true) {
                // Aggiorna il movimento dell'asteroide
                obj.get("asteroide1").updateMovement();

                // Aggiorna la visualizzazione del pannello
                repaint();

                // Aggiorna ogni 100 millisecondi
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        asteroidMovementThread.start();
    }
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	    Graphics2D g2d = (Graphics2D) g;

	    if (gameStopped) {
	        return;
	    }
	    
	    
	    // Loop through all GameObjects to update and draw them
        for (Entry<String, GameObject> e : obj.entrySet()) {
            GameObject gameObject = e.getValue();
            if (gameObject instanceof Asteroide) {
                // Se l'oggetto è un asteroide, aggiorna il suo movimento
                gameObject.updateMovement();
            }
            
            gameObject.draw(g2d);
        }

	    for (Entry<String, GameObject> e : obj.entrySet()) {
	        e.getValue().draw(g2d);
	    }
		area1 = new Area(obj.get("navicella1").getTransf()); //restituzione area nav1
		//Area area2 = new Area(obj.get("navicella2").getTransf()); //restituzione area nav2
		
		Shape circle = createCircle(cx, cy, 20);
		Area area2 = new Area(circle); // Area del cerchio
		area1.intersect(area2); //area1 diventa l'intersezione fra le 2
		isInCollision = !area1.isEmpty(); // Aggiorna lo stato di intersezione
	    
	    if (isInCollision) {
	        obj.get("navicella1").speed = Double.MIN_VALUE; // Ferma la navicella
	        area1.reset();
	    }

		for (Entry<String, GameObject> entry : obj.entrySet()) {
            if (entry.getValue() instanceof Asteroide) {
            	Area area11 = new Area(obj.get("navicella1").getTransf());
                Area areaAsteroide = new Area(entry.getValue().getTransf());
                area11.intersect(areaAsteroide);
                if (!area11.isEmpty()) {
                    System.out.println( "Collisione avvenuta! Gioco terminato.");
                    //update.interrupt(); // Interrompe il thread di aggiornamento
                    gameStopped = true; // Imposta la variabile di stato per evitare ulteriori azioni
                    
                 // Mostra una dialog di avviso
                    int choice = JOptionPane.showConfirmDialog(this, "Hai perso! Vuoi riavviare il gioco?", "Game Over", JOptionPane.YES_NO_OPTION);
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        // Riavvia il gioco
                        // Implementa la logica per riavviare il gioco qui, ad esempio reimposta le variabili di stato e inizializza nuovi oggetti
                    	resetGame();
                    } else {
                        // L'utente ha scelto di non riavviare il gioco, esci dall'applicazione o prendi altre azioni di chiusura
                        // ...
                    }
                    
                    return; 
                }
            }
        }
		
		
	}
	
	// Metodo per resettare il gioco
	private void resetGame() {
	    // Reimposta lo stato del gioco
	    gameStopped = false;

	    // Imposta una nuova posizione iniziale sicura per la navicella principale
	    obj.get("navicella1").x = 20; // Imposta la posizione X in un angolo opposto
	    obj.get("navicella1").y = 110; // Imposta la posizione Y in un angolo opposto

	    // Reimposta la velocità della navicella principale
	    obj.get("navicella1").speed = 10; // O qualsiasi valore desiderato per la velocità iniziale

	    // Se il thread di aggiornamento non è già in esecuzione, crea un nuovo oggetto Update e avvia il thread
	    if (!update.isAlive()) {
	        update = new Update(obj.get("navicella1"), this);
	        update.start();
	    }

	    // Potresti anche reimpostare altre variabili di stato o oggetti del gioco qui, se necessario
	}


	
	// Metodo per creare un cerchio
    private Shape createCircle(int x, int y, int r) {
        return new java.awt.geom.Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }
    
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
	        if (!isInCollision) {
	            obj.get("navicella1").speed += 10;
	        }
	        repaint();
	        if (!update.isAlive()) {
	            update.start();
	        }
	    }
	}


	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {	
		cx = e.getX();
		cy = e.getY();
		//calcolo in radianti dell'angolo della retta fra cursore e nav
		obj.get("navicella1").angolo = Math.atan2(((e.getY()) - (obj.get("navicella1").y)) , ((e.getX()) - (obj.get("navicella1").x))); 
		
		if((int)(obj.get("navicella1").speed) == Double.MIN_VALUE) { //se la navicella è in movimento e muovo il cursore non faccio
			repaint();							//repaint perchè ci pensa già keyPressed
		}
		
	}
	
	
	
	
	
}

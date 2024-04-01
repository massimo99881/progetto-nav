package it.jacopo.nave;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Panello extends JPanel implements KeyListener, MouseMotionListener{
	Map<String, GameObject> obj = new HashMap<>();
	private boolean isInCollision = false;
	boolean gameStopped = false;
	ArrayList<Proiettile> proiettili = new ArrayList<>();
    private Timer gameTimer;
	Area area1;
	int cx = 100, cy = 100;//thread update per drift astronave
	private Image sfondo;
	private List<String> nomiAsteroidi = new ArrayList<>();
	private Timer aggiungiAsteroidiTimer;
	private int aggiunteEffettuate = 0;
	private final int MAX_AGGIUNTE = 10;

	
	public Panello() {
		Nav nave = new Nav("navicella1");  //nave principale	
		Nav nave2 = new Nav("navicella2"); //nave fantoccio per test collisioni
		// Posizionamento della seconda navicella in basso a destra
        nave2.x =  100; 
        nave2.y =  100; 
        
        try {
            sfondo = ImageIO.read(new File("sfondo.jpg")); // Aggiusta il percorso secondo necessità
        } catch (IOException e) {
            e.printStackTrace();
            sfondo = null;
        }

		obj.put(nave.nome, nave);
		obj.put(nave2.nome, nave2);
		
		addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            if (e.getButton() == MouseEvent.BUTTON1) {
	                // Ottiene la navicella principale
	                Nav nave = (Nav) obj.get("navicella1");
	                if (nave != null) {
	                    // Crea un proiettile alla posizione della navicella
	                    proiettili.add(new Proiettile(nave.x + nave.shape.getBounds().width / 2, nave.y + nave.shape.getBounds().height / 2));
	                }
	            }
	        }
	    });
		
		// Inizializza 15 asteroidi con posizioni iniziali visibili
	    for (int i = 1; i <= 15; i++) {
	    	String nomeAsteroide = "asteroide" + i;
	        Asteroide asteroide = new Asteroide(nomeAsteroide, "asteroide" + i + ".png");
	        asteroide.x = 1110; // Tutti gli asteroidi partono dalla stessa posizione X iniziale
	        Random rand = new Random();
	        int numeroCasuale = rand.nextInt(441) + 10; // Genera un numero casuale tra 10 (incluso) e 451 (escluso)
	        asteroide.y = numeroCasuale; // Distribuisce gli asteroidi verticalmente
	        nomiAsteroidi.add(nomeAsteroide);
	        // Aggiungi l'asteroide alla mappa degli oggetti
	        obj.put(asteroide.name, asteroide);
	    }
	    
	 // Inizializza e avvia il Timer
        gameTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aggiornaGioco(); // Aggiorna lo stato del gioco
                repaint(); // Richiede il ridisegno del pannello
            }
        });
        gameTimer.start(); // Avvia il Timer
        
     // Nella classe Panello, dopo l'inizializzazione di gameTimer
        aggiungiAsteroidiTimer = new Timer(20000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (aggiunteEffettuate < MAX_AGGIUNTE) {
                    aggiungiAsteroidi();
                    aggiunteEffettuate++;
                } else {
                    aggiungiAsteroidiTimer.stop(); // Ferma il Timer
                    JOptionPane.showMessageDialog(null, "Hai vinto!", "Complimenti", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        aggiungiAsteroidiTimer.start();

	}
	
	
	// Metodo per aggiungere un singolo asteroide con aggiornamento per usare nomiAsteroidi
	private void aggiungiAsteroide() {
	    Random rand = new Random();
	    int posizioneYCasuale = rand.nextInt(441) + 10; // Genera una posizione Y casuale
	    int indiceImmagineCasuale = rand.nextInt(15) + 1; // Genera un indice casuale tra 1 e 15

	    // Genera il nome univoco dell'asteroide basato sul numero di elementi nella lista nomiAsteroidi
	    String nomeAsteroide = "asteroide" + (nomiAsteroidi.size() + 1);
	    nomiAsteroidi.add(nomeAsteroide); // Aggiunge il nome alla lista per tenere traccia
	    
	    // Crea l'oggetto Asteroide e aggiungilo alla mappa obj
	    Asteroide asteroide = new Asteroide(nomeAsteroide, "asteroide" + indiceImmagineCasuale + ".png");
	    asteroide.x = 1110;
	    asteroide.y = posizioneYCasuale;
	    obj.put(nomeAsteroide, asteroide);
	}

	// Metodo aggiungiAsteroidi per aggiungere asteroidi periodicamente
	private void aggiungiAsteroidi() {
	    for (int i = 0; i < 10; i++) {
	        aggiungiAsteroide(); // Aggiunge un singolo asteroide utilizzando il metodo definito sopra
	    }
	    // Opzionalmente, puoi aggiungere qui logica aggiuntiva, per esempio per fermare il Timer dopo un certo numero di aggiunte
	}



	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Controlla se l'immagine di sfondo è stata caricata correttamente
	    if (sfondo != null) {
	        // Adatta l'immagine di sfondo per coprire tutto il pannello
	        g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
	    }
		
	    Graphics2D g2d = (Graphics2D) g;

	    if (gameStopped) {
	        return;
	    }
	    
	 // Disegna i proiettili
        for (Proiettile proiettile : proiettili) {
            proiettile.disegna(g2d);
        }
        
     // Disegna le esplosioni
//        Iterator<Esplosione> esplosioneIterator = esplosioni.iterator();
//        while (esplosioneIterator.hasNext()) {
//            Esplosione esplosione = esplosioneIterator.next();
//            if (!esplosione.aggiorna()) {
//                esplosioneIterator.remove();
//            } else {
//                esplosione.disegna(g2d);
//            }
//        }
        
	    // Loop through all GameObjects to update and draw them
//        for (Entry<String, GameObject> e : obj.entrySet()) {
//            GameObject gameObject = e.getValue();
//            if (gameObject instanceof Asteroide) {
//                // Se l'oggetto è un asteroide, aggiorna il suo movimento
//                gameObject.updateMovement();
//            }
//            
//            gameObject.draw(g2d);
//        }

//	    for (Entry<String, GameObject> e : obj.entrySet()) {
//	        e.getValue().draw(g2d);
//	    }
		controllaCollisioneNavCursore();

		for (Entry<String, GameObject> entry : obj.entrySet()) {
			GameObject gameObject = entry.getValue();
            if (gameObject instanceof Asteroide) {
            	
            	controllaCollisioneNavAsteroid(entry);
                
                // Se l'oggetto è un asteroide, aggiorna il suo movimento
                gameObject.updateMovement();
            }
            gameObject.draw(g2d);
        }
	}


	private void controllaCollisioneNavCursore() {
//		Nav navicella1 = (Nav) obj.get("navicella1");
		Shape circle = createCircle(cx, cy, 20);
		Area area2 = new Area(circle); // Area del cerchio
		
//		if (!navicella1.getBounds().intersects(area2.getBounds())) {
//			return;
//		}
		
		area1 = new Area(obj.get("navicella1").getTransf()); 
		
		
		area1.intersect(area2); //area1 diventa l'intersezione fra le 2
		isInCollision = !area1.isEmpty(); // Aggiorna lo stato di intersezione
	    
	    if (isInCollision) {
	        obj.get("navicella1").speed = Double.MIN_VALUE; // Ferma la navicella
	        area1.reset();
	    }
	}


	private void controllaCollisioneNavAsteroid(Entry<String, GameObject> entry) {
		GameObject navicella1 = obj.get("navicella1");
        Asteroide asteroide = (Asteroide) entry.getValue();

        // Controllo preliminare bounding box per ridurre i calcoli
        if (!navicella1.getBounds().intersects(asteroide.getBounds())) {
			return;
		}
        
		Area area11 = new Area(navicella1.getTransf());
		Area areaAsteroide = new Area(asteroide.getTransf());
		area11.intersect(areaAsteroide);
		
		if (!area11.isEmpty()) {
		    System.out.println( "Collisione avvenuta! Gioco terminato.");
		    gameStopped = true; 
		    
		 
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
	
	
	public void aggiornaGioco() {
	    // Aggiornamento dei proiettili
	    Iterator<Proiettile> iterProiettili = proiettili.iterator();
	    while (iterProiettili.hasNext()) {
	        Proiettile proiettile = iterProiettili.next();
	        proiettile.aggiorna();
	        
	     // Rimuovi i proiettili che escono dallo schermo
	        if (proiettile.x < 0 || proiettile.x > getWidth()) {
	            iterProiettili.remove();
	            continue;
	        }

	        // Ottiene la Shape del proiettile
	        Shape shapeProiettile = proiettile.getShape();
	        
	        Rectangle boundsProiettile = proiettile.getBounds();
	        
	        Iterator<Entry<String, GameObject>> iterObj = obj.entrySet().iterator();
	        while (iterObj.hasNext()) {
	            Entry<String, GameObject> entry = iterObj.next();
	            GameObject gameObject = entry.getValue();
	            
	            if (gameObject instanceof Asteroide) {
	            	
	            	 Rectangle boundsAsteroide = gameObject.getBounds(); // Assumendo che Asteroide abbia anche un metodo getBounds
	                 if (boundsProiettile.intersects(boundsAsteroide)) {
	                	Asteroide asteroide = (Asteroide) gameObject;
	 	                Area areaAsteroide = new Area(asteroide.getTransf());
	 	                Area areaProiettile = new Area(shapeProiettile);
	 	                
	 	                areaAsteroide.intersect(areaProiettile);
	 	                if (!areaAsteroide.isEmpty()) {
	 	                    // Collisione rilevata, gestisci qui
	 	                    iterProiettili.remove(); // Rimuovi il proiettile
	 	                    asteroide.colpito(); // Aggiorna lo stato dell'asteroide per il colpo ricevuto

	 	                    if (asteroide.getColpiSubiti() >= 5) {
	 	                        iterObj.remove(); // Rimuovi l'asteroide se è stato distrutto
	 	                        System.out.println(asteroide.name + " è stato distrutto"); // Stampa un messaggio in console
	 	                    }
	 	                    break; // Esci dal ciclo se una collisione è stata trovata
	 	                }
	                 }
	            	
	            	
	                
	            }
	        }
	    }

	    // Aggiornamento delle esplosioni...
	}



	
	// Metodo per resettare il gioco
	private void resetGame() {
	    // Reimposta lo stato del gioco
	    gameStopped = false;
	    gameTimer.stop();
	    // Imposta una nuova posizione iniziale sicura per la navicella principale
	    obj.get("navicella1").x = 20; // Imposta la posizione X in un angolo opposto
	    obj.get("navicella1").y = 110; // Imposta la posizione Y in un angolo opposto

	    // Reimposta la velocità della navicella principale
	    obj.get("navicella1").speed = 10; // O qualsiasi valore desiderato per la velocità iniziale


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
	        //repaint();
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
		
//		if((int)(obj.get("navicella1").speed) == Double.MIN_VALUE) { //se la navicella è in movimento e muovo il cursore non faccio
//			repaint();							//repaint perchè ci pensa già keyPressed
//		}
		
	}
	
	
	
	
	
}

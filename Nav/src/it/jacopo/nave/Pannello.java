package it.jacopo.nave;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Pannello extends JPanel implements KeyListener, MouseMotionListener, ComponentListener{
	
	private GameClient client;
	
	private ProiettilePool proiettilePool = new ProiettilePool();
	private int sfondoX = 0;
	private final int VELOCITA_SFONDO = -1; // Sposta lo sfondo di 1 pixel a ogni tick del timer verso sinistra
	
	public int width;
	public int height;
	
	Map<String, Cache> obj = new HashMap<>();
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
	private boolean staSparando = false;
	private Timer timerSparo;
	private long lastShootTime = 0;
	private final long SHOOT_INTERVAL = 100; // Intervallo tra gli spari in millisecondi
	private int asteroidiDistrutti = 0;
	private int larghezzaPrecedente;
	private String clientNavicella = "";
	
	private Clip clipAudio;
	
	
	public Pannello() {
		try {
            this.client = new GameClient();
            this.client.startClient(this::handleNetworkMessage);
        } catch (IOException e) {
            System.err.println("Errore nell'inizializzare il client di rete: " + e.getMessage());
            // Gestisci l'errore come preferisci, es. mostrando un messaggio all'utente
        }
		
		
		
		Nav nave = new Nav("navicella1");  //nave principale	
		Nav nave2 = new Nav("navicella2"); //nave fantoccio per test collisioni
		// Posizionamento della seconda navicella in basso a destra
        nave2.x =  25; 
        nave2.y =  Conf.FRAME_HEIGHT/3 + 150; 
        
        nave.y = Conf.FRAME_HEIGHT/3;
        nave.x = 25; 
        
        try {
            sfondo = ImageIO.read(new File(Conf._RESOURCES_IMG_PATH + Conf.SFONDO_JPG)); // Aggiusta il percorso secondo necessità
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
			    	spara(); 
			    }
			}
	    });
		addMouseListener(new MouseAdapter() {
		    @Override
		    public void mousePressed(MouseEvent e) {
		        if (e.getButton() == MouseEvent.BUTTON1) {
		            staSparando = true;
		            spara(); // Metodo per sparare
		        }
		    }

		    @Override
		    public void mouseReleased(MouseEvent e) {
		        if (e.getButton() == MouseEvent.BUTTON1) {
		            staSparando = false;
		        }
		    }
		});

		
		// Inizializza 15 asteroidi con posizioni iniziali visibili
	    for (int i = 1; i <= Conf.asteroid_number; i++) { 
	    	String nomeAsteroide = "asteroide" + i;
	        Asteroide asteroide = new Asteroide(this, nomeAsteroide, Conf._RESOURCES_IMG_PATH + "asteroide" + i + ".png");
	        asteroide.x = Conf.FRAME_WIDTH; // Tutti gli asteroidi partono dalla stessa posizione X iniziale
	        Random rand = new Random();
	        int numeroCasuale = rand.nextInt(441) + 10; // Genera un numero casuale tra 10 (incluso) e 451 (escluso)
	        asteroide.y = numeroCasuale; // Distribuisce gli asteroidi verticalmente
	        nomiAsteroidi.add(nomeAsteroide);
	        // Aggiungi l'asteroide alla mappa degli oggetti
	        obj.put(asteroide.name, asteroide);
	    }
	    
	    // Inizializza e avvia il Timer
        gameTimer = new Timer(Conf._FPSms, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aggiornaGioco(); // Aggiorna lo stato del gioco
                repaint(); // Richiede il ridisegno del pannello
            }
        });
        gameTimer.start(); // Avvia il Timer
        
        // Nella classe Panello, dopo l'inizializzazione di gameTimer
        aggiungiAsteroidiTimer = new Timer(Conf.Level_timer, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (aggiunteEffettuate < Conf.Level_Total) {
                    aggiungiAsteroidi();
                    aggiunteEffettuate++;
                } else {
                	try {
        		        File fileAudio = new File(Conf._RESOURCES_AUDIO_PATH + "winner.wav"); 
        		        AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileAudio);
        		        clipAudio = AudioSystem.getClip();
        		        clipAudio.open(audioStream);
        		        clipAudio.start();
        		    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
        		        ex.printStackTrace();
        		    }
                    aggiungiAsteroidiTimer.stop(); // Ferma il Timer
                    JOptionPane.showMessageDialog(null, "Hai vinto!", "Complimenti", JOptionPane.INFORMATION_MESSAGE);
                    resetGame(); // Chiama resetGame dopo che l'utente ha fatto click su "OK"
                }
            }
        });
        aggiungiAsteroidiTimer.start();
        this.width = this.getWidth();
        this.height = this.getHeight();
        
        timerSparo = new Timer(100, new ActionListener() { // Imposta un intervallo appropriato per la frequenza di sparo
            @Override
            public void actionPerformed(ActionEvent e) {
                if (staSparando) {
                    spara();
                }
            }
        });
        timerSparo.start();
     
        this.larghezzaPrecedente = this.getWidth(); 
        // Aggiungi 'this' come ComponentListener del pannello
        this.addComponentListener(this);
        
        caricaAudio(); // Carica l'audio
        clipAudio.start(); // Avvia l'audio
        clipAudio.loop(Clip.LOOP_CONTINUOUSLY); // Loop continuo
        
        
        
	}
	
	private void handleNetworkMessage(String message) {
	    // Analizza il messaggio JSON
		//System.out.println(message);
		
	    JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
	    

	    // Estrai il tipo di evento dal messaggio
	    String eventType = jsonMessage.get("tipo").getAsString();

	    // Gestisci il tipo di evento
	    switch (eventType) {
	        case "tipoNavicella":
	            // Imposta il tipo di navicella per questo client
	            this.clientNavicella = jsonMessage.get("navicella").getAsString();
	            break;
	        case "posizione":
	            // Estrai il nome della navicella e le coordinate dal messaggio
	            String nomeNavicella = jsonMessage.get("nome").getAsString();
	            int x = jsonMessage.get("x").getAsInt();
	            int y = jsonMessage.get("y").getAsInt();
	            double angolo = jsonMessage.get("angolo").getAsDouble();
	            if (!nomeNavicella.equals(this.clientNavicella)) {
	                // Aggiorna la posizione della navicella avversaria
	            	updateShipPosition(nomeNavicella, x, y, angolo);
	            }
	            break;
	        case "aggiornamentoProiettile":
	            double startX = jsonMessage.get("x").getAsDouble();
	            double startY = jsonMessage.get("y").getAsDouble();
	            double angoloP = jsonMessage.get("angolo").getAsDouble();
	            System.out.println("Pannello:aggiornamentoProiettile: "+jsonMessage);
	            Proiettile proiettile = new Proiettile(startX, startY, angoloP);
	            SwingUtilities.invokeLater(() -> proiettili.add(proiettile));
	            break;
	        default:
	            System.err.println("Pannello: Tipo di evento sconosciuto: " + eventType);
	            break;
	    }
	}


	
	public void sendPlayerPosition(int x, int y, double angolo) {
	    JsonObject jsonMessage = new JsonObject();
	    jsonMessage.addProperty("tipo", "posizione");
	    jsonMessage.addProperty("nome", clientNavicella);
	    jsonMessage.addProperty("x", x);
	    jsonMessage.addProperty("y", y);
	    jsonMessage.addProperty("angolo", angolo); // Aggiungi l'angolo
	    client.send(jsonMessage.toString());
	}



	public void updateShipPosition(String nomeNavicella, int x, int y, double angolo) {
	    // Cerca la navicella specificata nella mappa degli oggetti di gioco
	    Nav navicella = (Nav) obj.get(nomeNavicella);

	    // Se la navicella esiste, verifica se la posizione o l'angolo sono cambiati
	    if (navicella != null) {
	        // Controlla se ci sono stati cambiamenti significativi
	        boolean isPositionChanged = navicella.x != x || navicella.y != y;
	        boolean isAngleChanged = navicella.angolo != angolo;

	        // Aggiorna solo se la posizione o l'angolo sono cambiati
	        if (isPositionChanged || isAngleChanged) {
	            navicella.x = x;
	            navicella.y = y;
	            navicella.angolo = angolo;
	            //repaint(); // Rinfresca il pannello solo se c'è stato un cambiamento
	        }
	    } else {
	        System.err.println("Navicella non trovata: " + nomeNavicella);
	    }
	}



	
	private void caricaAudio() {
	    try {
	        File fileAudio = new File(Conf._RESOURCES_AUDIO_PATH + "Ignis.wav"); // Assicurati che il percorso sia corretto
	        AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileAudio);
	        clipAudio = AudioSystem.getClip();
	        clipAudio.open(audioStream);
	    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
	        e.printStackTrace();
	    }
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
	    if (this.getWidth() > 0 && this.larghezzaPrecedente > 0) { // Assicurati che entrambe le larghezze siano valide
	    	// Aggiorna la posizione delle navicelle
	        for (Entry<String, Cache> entry : obj.entrySet()) {
	            Cache gameObject = entry.getValue();
	            if (gameObject instanceof Nav) {
	            	Nav n = (Nav) gameObject;
	                double proporzione = n.x / (double) this.larghezzaPrecedente;
	                n.x = (int) (proporzione * this.getWidth());
	            }
	        }

	        // Aggiorna la posizione degli asteroidi
	        for (String nomeAsteroide : nomiAsteroidi) {
	            Asteroide asteroide = (Asteroide)obj.get(nomeAsteroide);
	            if (asteroide != null) {
	                double proporzione = asteroide.x / (double) this.larghezzaPrecedente;
	                asteroide.x = (int) (proporzione * this.getWidth());
	            }
	        }
	    }

	    // Aggiorna 'larghezzaPrecedente' con la nuova larghezza dopo il ridimensionamento
	    this.larghezzaPrecedente = this.getWidth();
	    
	    sendGameDimensionsAfterRender();
	}


	private void sendGameDimensionsAfterRender() {
	    // Verifica se il pannello è stato effettivamente visualizzato con dimensioni valide
	    if (this.getWidth() > 0 && this.getHeight() > 0) {
	        // Invia le dimensioni al server
	        client.sendGameDimensions(this.getWidth(), this.getHeight());
	    } else {
	        // Se le dimensioni non sono valide, potresti voler riprovare dopo un breve ritardo
	        SwingUtilities.invokeLater(() -> {
	            sendGameDimensionsAfterRender();
	        });
	    }
	}

	
	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
		sendGameDimensionsAfterRender();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}


	private void spara() {
	    if (staSparando) {
	    	long currentTime = System.currentTimeMillis();
	        if (currentTime - lastShootTime < SHOOT_INTERVAL) {
	            return; // Non abbastanza tempo è passato, quindi non sparare
	        }
	        lastShootTime = currentTime; // Aggiorna l'ultimo tempo di sparo

	        Nav nave = (Nav) obj.get(clientNavicella);
	        if (nave != null) {
	            double startX = nave.x + 30 * Math.cos(nave.angolo);
	            double startY = nave.y + 30 * Math.sin(nave.angolo);
	            //Proiettile proiettile = new Proiettile(startX, startY, nave.angolo);
	            Proiettile proiettile = proiettilePool.getProiettile(startX, startY, nave.angolo);
	            proiettili.add(proiettile);
	            
	            riproduciSuonoSparo(); // Riproduce il suono dello sparo
	            
	         // Invia messaggio di sparo al server
	            JsonObject jsonMessage = new JsonObject();
	            jsonMessage.addProperty("tipo", "sparo");
	            jsonMessage.addProperty("x", startX);
	            jsonMessage.addProperty("y", startY);
	            jsonMessage.addProperty("angolo", nave.angolo);
	            client.send(jsonMessage.toString());
	        }
	    }
	}
	
	// Metodo per aggiungere un singolo asteroide con aggiornamento per usare nomiAsteroidi
	private void aggiungiAsteroide() {
	    Random rand = new Random();
	    int posizioneYCasuale = rand.nextInt(441) + 10; // Genera una posizione Y casuale
	    int indiceImmagineCasuale = rand.nextInt(Conf.asteroid_number) + 1; // Genera un indice casuale tra 1 e 15

	    // Genera il nome univoco dell'asteroide basato sul numero di elementi nella lista nomiAsteroidi
	    String nomeAsteroide = "asteroide" + (nomiAsteroidi.size() + 1);
	    nomiAsteroidi.add(nomeAsteroide); // Aggiunge il nome alla lista per tenere traccia
	    
	    // Crea l'oggetto Asteroide e aggiungilo alla mappa obj
	    Asteroide asteroide = new Asteroide(this, nomeAsteroide, Conf._RESOURCES_IMG_PATH + "asteroide" + indiceImmagineCasuale + ".png");
	    asteroide.x = this.getWidth()-5;
	    asteroide.y = posizioneYCasuale;
	    obj.put(nomeAsteroide, asteroide);
	}

	// Metodo aggiungiAsteroidi per aggiungere asteroidi periodicamente
	private void aggiungiAsteroidi() {
	    for (int i = 0; i < Conf.MAX_AGGIUNTE; i++) {
	        aggiungiAsteroide(); // Aggiunge un singolo asteroide utilizzando il metodo definito sopra
	    }
	    // Opzionalmente, puoi aggiungere qui logica aggiuntiva, per esempio per fermare il Timer dopo un certo numero di aggiunte
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (clientNavicella == null || !obj.containsKey(clientNavicella)) {
	        // La navicella non è stata ancora impostata o non è presente nella mappa,
	        // quindi non procedere ulteriormente per evitare NullPointerException.
	        return;
	    }
		
		this.width = this.getWidth();
        this.height = this.getHeight();
        
		// Controlla se l'immagine di sfondo è stata caricata correttamente
		if (sfondo != null) {
	        // Disegna la prima copia dello sfondo
	        g.drawImage(sfondo, sfondoX, 0, this);
	        // Disegna la seconda copia dello sfondo se la prima copia sta per uscire dal lato sinistro
	        if (sfondoX < 0) {
	            g.drawImage(sfondo, sfondoX + sfondo.getWidth(this), 0, this);
	        }
	    }
		// Aggiorna la posizione dello sfondo per il prossimo frame
	    sfondoX += VELOCITA_SFONDO;
	    // Riavvolge lo sfondo se ha completamente attraversato verso sinistra
	    if (sfondoX <= -sfondo.getWidth(this)) {
	        sfondoX = 0;
	    }
		
	    Graphics2D g2d = (Graphics2D) g;
	    // Evita di disegnare ulteriormente se il gioco è stato fermato
	    if (gameStopped) {
	        return;
	    }
	    
	    // Disegna i proiettili
        for (Proiettile proiettile : proiettili) {
            proiettile.disegna(g2d);
        }
        // Controlla collisioni tra la navicella e il cursore
		controllaCollisioneNavCursore();

		// Crea una copia temporanea della mappa degli oggetti per iterazione sicura
		Map<String, Cache> tempObjects = new HashMap<>(obj);
	    for (Entry<String, Cache> entry : tempObjects.entrySet()) {
	        Cache gameObject = entry.getValue();
	        if (gameObject instanceof Asteroide) {
	        	Asteroide a = (Asteroide) gameObject;
	            a.updateMovement();
	            // Controlla le collisioni con ogni asteroide qui
	            controllaCollisioneNavAsteroid(entry);
	            a.draw(g2d);
	        }
	        if (gameObject instanceof Nav) {
	        	Nav n = (Nav) gameObject;
	        	n.draw(g2d);
	        }
	        
	    }
	    
	 // Imposta il colore e il font per il testo del contatore
	    g.setColor(Color.WHITE);
	    g.setFont(new Font("Arial", Font.BOLD, 14));

	    // Disegna il contatore nella parte superiore della finestra di gioco
	    String contatoreText = "Asteroidi Distrutti: " + asteroidiDistrutti;
	    g.drawString(contatoreText, 10, 20); // 10 pixel dal bordo sinistro e 20 pixel dal bordo superiore
	    //Nav n = (Nav)obj.get(clientNavicella);
	    //sendPlayerPosition(n.x,n.y,n.angolo); //TODO capire
	}


	private void controllaCollisioneNavCursore() {
		if (clientNavicella == null || !obj.containsKey(clientNavicella)) {
	        // La navicella non è stata ancora impostata o non è presente nella mappa,
	        // quindi non procedere ulteriormente per evitare NullPointerException.
	        return;
	    }
		
		Shape circle = createCircle(cx, cy, 20);
		Area area2 = new Area(circle); // Area del cerchio
		Nav navicella = (Nav)obj.get(clientNavicella);
		area1 = new Area(navicella.getTransf()); 
		
		area1.intersect(area2); //area1 diventa l'intersezione fra le 2
		isInCollision = !area1.isEmpty(); // Aggiorna lo stato di intersezione
	    
	    if (isInCollision) {
	    	navicella.speed = Double.MIN_VALUE; // Ferma la navicella
	        area1.reset();
	    }
	}


	private void controllaCollisioneNavAsteroid(Entry<String, Cache> entry) {
		Nav navicella1 = (Nav)obj.get(clientNavicella);
        Asteroide asteroide = (Asteroide) entry.getValue();

        // Controllo preliminare bounding box per ridurre i calcoli
        if (!navicella1.getBounds().intersects(asteroide.getBounds())) {
			return;
		}
        
		Area areaNav = new Area(navicella1.getTransf());
		Area areaAsteroide = new Area(asteroide.getTransf());
		areaNav.intersect(areaAsteroide);
		
		if (!areaNav.isEmpty()) {
		    System.out.println( "Collisione avvenuta! Gioco terminato.");
		    gameStopped = true; 
		    clipAudio.stop();
		    try {
		        File fileAudio = new File(Conf._RESOURCES_AUDIO_PATH + "losing.wav"); // Assicurati che il percorso sia corretto
		        AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileAudio);
		        clipAudio = AudioSystem.getClip();
		        clipAudio.open(audioStream);
		        clipAudio.start();
		    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
		        e.printStackTrace();
		    }
		    JOptionPane.showMessageDialog(null, "Hai perso! Il gioco verrà riavviato.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
	        resetGame(); 
	        // Riavvia il gioco immediatamente dopo la chiusura del messaggio
	        return;
		}
	}
	private void riproduciSuonoSparo() {
	    try {
	        // Carica il suono dello sparo
	        File fileAudio = new File(Conf._RESOURCES_AUDIO_PATH + "laser.wav"); // Assicurati che il percorso sia corretto
	        AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileAudio);
	        Clip clipSparo = AudioSystem.getClip();
	        clipSparo.open(audioStream);

	        // Riproduce il suono una volta
	        clipSparo.start();
	    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
	        e.printStackTrace();
	    }
	}

	
	public void aggiornaGioco() {
		
	    // ciclo per ogni proiettile
	    Iterator<Proiettile> iterProiettili = proiettili.iterator();
	    while (iterProiettili.hasNext()) {
	    	
	        Proiettile proiettile = iterProiettili.next();
	        proiettile.aggiorna();
	        
	        // Controlla se il proiettile è uscito dallo schermo
	        boolean proiettileDaRimuovere = proiettile.x < 0 || proiettile.x > getWidth() || proiettile.y < 0 || proiettile.y > getHeight();
	        if (proiettileDaRimuovere) {
	        	// Rimuove il proiettile dalla lista dei proiettili attivi
	            iterProiettili.remove(); 
	            // Rilascia il proiettile nel pool per il riutilizzo
	            proiettilePool.releaseProiettile(proiettile); 
	            continue;
	        }
	        
	        //ciclo per ogni asteroide
	        Iterator<Entry<String, Cache>> iterObj = obj.entrySet().iterator();
	        while (iterObj.hasNext()) {
	            Entry<String, Cache> entry = iterObj.next();
	            Cache gameObject = entry.getValue();
	            
	            if (gameObject instanceof Asteroide) {
	            	Asteroide asteroide = (Asteroide)gameObject;
            	 	//prima ottimizzazione: calcolare le aree e shape e verifico se i rettangoli collidono
            	 	Rectangle boundsAsteroide = asteroide.getBounds(); 
            	 	Rectangle boundsProiettile = proiettile.getBounds();
            	 	
	                if (boundsProiettile.intersects(boundsAsteroide)) {
	                	 
	                	//Se la distanza è maggiore , salta la verifica dettagliata e continua con il prossimo asteroide
	                	//asteroide.updateMovement();
	                	//proiettile.aggiorna();
	                	double distanza = Math.sqrt(
	                            Math.pow(asteroide.getX() - proiettile.getX(), 2) +
	                            Math.pow(asteroide.getY() - proiettile.getY(), 2)
	                        );
	                	
	                	/**
	                	 * Soglia di distanza per decidere se effettuare controlli di collisione dettagliati
	                	 * Una soglia maggiore porta a un rilevamento delle collisioni più preciso ma può aumentare il carico computazionale
	                	 */
	                    if (distanza >= 30) {
	                    	// Rimuovi il proiettile
	 	                    iterProiettili.remove(); 
	 	                    // Rilascia il proiettile nel pool per il riutilizzo
	 	                    proiettilePool.releaseProiettile(proiettile); 
	 	                    // Aggiorna lo stato dell'asteroide per il colpo ricevuto
	 	                    asteroide.colpito(); 

	 	                    if (asteroide.getColpiSubiti() >= 5) {
	 	                    	// Rimuovi l'asteroide se è stato distrutto
	 	                        iterObj.remove(); 
	 	                        // Stampa un messaggio in console
	 	                        System.out.println(asteroide.name + " è stato distrutto"); 
	 	                       asteroidiDistrutti++; 
	 	                       
	 	                      try {
		 	         		        File fileAudio = new File(Conf._RESOURCES_AUDIO_PATH + "asteroid.wav"); 
		 	         		        AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileAudio);
		 	         		        clipAudio = AudioSystem.getClip();
		 	         		        clipAudio.open(audioStream);
		 	         		        clipAudio.start();
		 	         		    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
		 	         		        ex.printStackTrace();
		 	         		    }
	 	                    }
	 	                    // Esci dal ciclo se una collisione è stata trovata
	 	                    break; 
	                    }
	                	
	                    // eseguire la verifica più dettagliata basata su Area e Shape
	 	                Area areaAsteroide = new Area(asteroide.getTransf());
	 	                Area areaProiettile = new Area(proiettile.getShape());
	 	                areaAsteroide.intersect(areaProiettile);
	 	                if (!areaAsteroide.isEmpty()) {
	 	                	// Rimuovi il proiettile
	 	                    iterProiettili.remove(); 
	 	                    // Rilascia il proiettile nel pool per il riutilizzo
	 	                    proiettilePool.releaseProiettile(proiettile); 
	 	                    // Aggiorna lo stato dell'asteroide per il colpo ricevuto
	 	                    asteroide.colpito(); 

	 	                    if (asteroide.getColpiSubiti() >= 5) {
	 	                    	// Rimuovi l'asteroide se è stato distrutto
	 	                        iterObj.remove(); 
	 	                        // Stampa un messaggio in console
	 	                        System.out.println(asteroide.name + " è stato distrutto"); 
	 	                    }
	 	                    // Esci dal ciclo se una collisione è stata trovata
	 	                    break; 
	 	                }
	                }
	            }
	        }
	    }

	}

	private void resetGame() {
		Asteroide.precaricaImmagini();
	    gameStopped = false;
	    // Resetta gli asteroidi distrutti
	    asteroidiDistrutti = 0;
	    
	    proiettili.clear(); // Svuota la lista dei proiettili
	    obj.clear(); // Rimuovi tutti gli oggetti del gioco
	    nomiAsteroidi.clear(); // Svuota la lista dei nomi degli asteroidi
	    sfondoX = 0; // Resetta la posizione dello sfondo
	    gameTimer.stop(); // Ferma il timer del gioco
	    aggiungiAsteroidiTimer.stop(); // Ferma il timer che aggiunge gli asteroidi
	    initializeGameObjects(); // Reinizializza gli oggetti del gioco
	    gameTimer.start(); // Riavvia il timer del gioco
	    if (clipAudio != null && clipAudio.isOpen()) {
	        clipAudio.close(); // Chiude l'audio clip corrente
	    }
	    caricaAudio(); // Ricarica l'audio
	    clipAudio.start(); // Riavvia l'audio
	    clipAudio.loop(Clip.LOOP_CONTINUOUSLY); // Loop continuo
	}
	
	private void initializeGameObjects() {
	    // Ricrea la navicella principale e una navicella "fantoccio" per test di collisione, se necessario
	    Nav nave = new Nav("navicella1");
	    Nav nave2 = new Nav("navicella2");
	    
	    this.width = this.getWidth();
        this.height = this.getHeight();
	    
	    nave2.x =  25; 
        nave2.y =  Conf.FRAME_HEIGHT/3 + 150; 
        
        nave.y = Conf.FRAME_HEIGHT/3;
        nave.x = 25;
        
	    obj.put(nave.nome, nave);
	    obj.put(nave2.nome, nave2);

	    // Inizializza gli asteroidi
	    for (int i = 1; i <= Conf.asteroid_number; i++) {
	        String nomeAsteroide = "asteroide" + i;
	        Asteroide asteroide = new Asteroide(this, nomeAsteroide, Conf._RESOURCES_IMG_PATH + "asteroide" + i + ".png");
	        asteroide.x = this.getWidth()-5; // Ad esempio, se la larghezza della finestra di gioco è 1200
	        Random rand = new Random();
	        int numeroCasuale = rand.nextInt(441) + 10;
	        asteroide.y = numeroCasuale;
	        obj.put(asteroide.name, asteroide);
	        nomiAsteroidi.add(nomeAsteroide);
	    }

	    // Riavvia il timer per l'aggiunta periodica degli asteroidi
	    aggiunteEffettuate = 0; // Resetta il conteggio delle aggiunte
	    aggiungiAsteroidiTimer.start();

	    // Assicurati di aggiornare 'larghezzaPrecedente' alla larghezza attuale dopo il reset
	    this.larghezzaPrecedente = this.getWidth();
	}

	
	// Metodo per creare un cerchio
    private Shape createCircle(int x, int y, int r) {
        return new java.awt.geom.Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }
    
    // Metodi per inviare aggiornamenti al server basati sulle azioni dell'utente
    // Ad esempio, quando l'utente muove la navicella o spara

    // Override dei metodi dell'interfaccia KeyListener e MouseMotionListener
    // per gestire l'input dell'utente e inviare aggiornamenti al server

    
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
	    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
	        if (!isInCollision) {
	        	Nav nave = (Nav) obj.get(clientNavicella);
	            nave.speed += 10;
	            //TODO capire
	            sendPlayerPosition(nave.x, nave.y, nave.angolo);
	        }
	        //repaint();
	    }
	}


	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (staSparando) {
            Nav nave = (Nav) obj.get(clientNavicella);
            if (nave != null) {
                // Calcola l'angolo tra la navicella e la posizione del cursore del mouse
                double angleToMouse = Math.atan2(e.getY() - nave.y, e.getX() - nave.x);
                nave.angolo = angleToMouse;
                spara(); 
                
                //TODO capire
                sendPlayerPosition(nave.x, nave.y, nave.angolo);
            }
        }
	}
	
//	@Override
//	public void mouseMoved(MouseEvent e) {	
//		cx = e.getX();
//		cy = e.getY();
//		obj.get(clientNavicella).angolo = Math.atan2(((e.getY()) - (obj.get(clientNavicella).y)) , ((e.getX()) - (obj.get(clientNavicella).x))); 
//	}
	//TODO capire
	@Override
	public void mouseMoved(MouseEvent e) {  
	    Nav nave = (Nav) obj.get(clientNavicella);
	    if (nave != null) { 
	        cx = e.getX();
	        cy = e.getY();
	        double angolo = Math.atan2(e.getY() - nave.y, e.getX() - nave.x);
	        nave.angolo = angolo;
	        sendPlayerPosition(nave.x, nave.y, nave.angolo);
	    }
	}
	
}

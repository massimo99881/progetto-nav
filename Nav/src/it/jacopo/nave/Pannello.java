package it.jacopo.nave;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask; // Util TimerTask for scheduling tasks

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Pannello extends JPanel implements KeyListener, MouseMotionListener, ComponentListener{
	
	private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String serverAddress = "127.0.0.1";
    private final int serverPort = 8080;
    private volatile boolean running = true; // Flag per controllare il ciclo di ricezione
    private String playerType;
	
	private int sfondoX = 0;
	private final int VELOCITA_SFONDO = -1; // Sposta lo sfondo di 1 pixel a ogni tick del timer verso sinistra
	public int width;
	public int height;
	
	private boolean isInCollision = false;
	boolean gameStopped = false;
	ArrayList<Proiettile> proiettili = new ArrayList<>();
    private javax.swing.Timer gameTimer;
	Area area1;
	int cx = 100, cy = 100;//thread update per drift astronave
	private Image sfondo;
	private boolean staSparando = false;
	private javax.swing.Timer timerSparo;
	private long lastShootTime = 0;
	private final long SHOOT_INTERVAL = 100; // Intervallo tra gli spari in millisecondi
	private int asteroidiDistrutti = 0;
	private int larghezzaPrecedente;
	private String clientNavicella = "";
	private Singleton singleton ;
	private Clip clipAudio;
	private int contatoreSerie = 0;
	
	private void scheduleAsteroidTimer(long startTime) {
	    // Calculate delay to start the timer
	    long delay = startTime - System.currentTimeMillis();
	    if (delay < 0) delay = 0; // Ensure the delay is non-negative

	    // Create a new instance of java.util.Timer
	    Timer timer = new Timer();
	    timer.schedule(new TimerTask() {
	        @Override
	        public void run() {
	            // Ensure updates to the UI are made on the Swing event dispatch thread
	            SwingUtilities.invokeLater(() -> {
	                initializeAsteroids();
	            });
	        }
	    }, delay, 10000); // Run every 10000 ms (10 seconds) after the initial delay
	}

	
	
	private void initializeAsteroids() {
	    int baseIndex = contatoreSerie * Conf.asteroid_number;
	    for (int i = 1; i <= Conf.asteroid_number; i++) {
	        String nomeAsteroide = "asteroide" + (baseIndex + i);
	        Asteroide asteroide = new Asteroide(this, nomeAsteroide, Conf._RESOURCES_IMG_PATH + "asteroide" + i + ".png");
	        asteroide.x = Conf.FRAME_WIDTH; 
	        asteroide.y = Conf.FRAME_HEIGHT - i * 50;
	        singleton.getNomiAsteroidi().add(nomeAsteroide);
	        singleton.getObj().put(nomeAsteroide, asteroide);
	    }
	    contatoreSerie++;  
	}
	
	public Pannello() throws IOException {
		this.singleton = Singleton.getInstance();
		
		precaricaImmagini();
		
		
		setupNetworking();
		setupNavicelle();
		setupSfondo();
		setupListeners();
	    setupTimerGame();
        
        this.width = this.getWidth();
        this.height = this.getHeight();
        
        setupTimerSparo();
     
        this.larghezzaPrecedente = this.getWidth(); 
        // Aggiungi 'this' come ComponentListener del pannello
        this.addComponentListener(this);
        
        caricaAudio(); // Carica l'audio
        clipAudio.start(); // Avvia l'audio
        clipAudio.loop(Clip.LOOP_CONTINUOUSLY); // Loop continuo
	}
	
	void precaricaImmagini() {
    	int asteroidNumber = Conf.asteroid_number;
        // Caricamento e cache delle prime 15 immagini con nomi specifici
        for (int i = 1; i <= asteroidNumber; i++) {
            String percorso = Conf._RESOURCES_IMG_PATH + "asteroide" + i + ".png";
            caricaImmagine(percorso);
        }
    }

    void caricaImmagine(String path) {
    	Map<String, Cache> imageCache = Singleton.getInstance().getImageCache();
        if (!imageCache.containsKey(path)) {
        	try {
                BufferedImage originalImage = ImageIO.read(new File(path));
                double scaleFactor = 0.2;
                //double scaleFactor = 0.2 + (0.45 - 0.2) * rand.nextDouble();
                int newWidth = (int) (originalImage.getWidth() * scaleFactor);
                int newHeight = (int) (originalImage.getHeight() * scaleFactor);
                Cache ac = new Cache(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
                singleton.getImageCache().put(path, ac);
            } 
        	catch (IOException e) {
                e.printStackTrace();
            }
        	catch (Exception e) {
                System.err.println("Errore nel caricamento dell'immagine da: " + path);
                e.printStackTrace();
            }
        }
    }
	
	private void setupNavicelle() {
		Nav nave = new Nav("navicella1");  //nave principale	
		Nav nave2 = new Nav("navicella2"); //nave fantoccio per test collisioni
		// Posizionamento della seconda navicella in basso a destra
        nave2.x =  25; 
        nave2.y =  Conf.FRAME_HEIGHT/3 + 150; 
        nave.y = Conf.FRAME_HEIGHT/3;
        nave.x = 25; 
        
        singleton.getObj().put(nave.nome, nave);
        singleton.getObj().put(nave2.nome, nave2);
	}
	
	private void setupNetworking() throws IOException {
		try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            startClient();
        } catch (IOException e) {
            throw new IOException("Impossibile stabilire la connessione con il server.", e);
        }
    }
	
	public void sendGameDimensions(int width, int height) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "dimensioniGioco");
        jsonMessage.addProperty("larghezza", width);
        jsonMessage.addProperty("altezza", height);
        send(jsonMessage.toString());
    }
	
	public void send(String message) {
        out.println(message);
    }
	
	public void startClient() {
	    new Thread(() -> {
	        while (running) {
	            try {
	                String message = in.readLine();
	                if (message != null) {
	                    handleIncomingMessage(message); // gestisce messaggi in arrivo da server
	                } else {
	                    System.out.println("Connessione terminata dal server.");
	                    break;
	                }
	            } catch (IOException e) {
	                if (running) {
	                    System.err.println("Errore di connessione: " + e.getMessage());
	                }
	                break;
	            }
	        }
	    }, "Client-Receiver").start();
	}
	
	
	
	private void handleIncomingMessage(String message) {
		System.out.println("Pannello\t"+message);
        JsonObject receivedJson = JsonParser.parseString(message).getAsJsonObject();
        String tipo = receivedJson.get("tipo").getAsString();

        switch (tipo) {
            case "tipoNavicella":
                playerType = receivedJson.get("navicella").getAsString();
                this.clientNavicella = playerType;
                break;
            case "startAsteroidi":
            	long startTime = receivedJson.get("startTime").getAsLong();
                scheduleAsteroidTimer(startTime);
            	break;
            case "posizione":
	            // Estrai il nome della navicella e le coordinate dal messaggio
	            String nomeNavicella = receivedJson.get("nome").getAsString();
	            int x = receivedJson.get("x").getAsInt();
	            int y = receivedJson.get("y").getAsInt();
	            double angolo = receivedJson.get("angolo").getAsDouble();
	            if (!nomeNavicella.equals(this.clientNavicella)) {
	                // Aggiorna la posizione della navicella avversaria
	            	updateShipPosition(nomeNavicella, x, y, angolo);
	            }
	            break;
            case "sparo":
	        	String mittente = receivedJson.get("mittente").getAsString();
	            double startX = receivedJson.get("x").getAsDouble();
	            double startY = receivedJson.get("y").getAsDouble();
	            double angoloP = receivedJson.get("angolo").getAsDouble();
	            System.out.println("Pannello:sparo: "+receivedJson);
	            //Proiettile proiettile = new Proiettile(startX, startY, angoloP);
	            if (!mittente.equals(this.clientNavicella)) { // Assicurati che il proiettile non sia della propria navicella
	                Proiettile proiettile = singleton.getProiettile(startX, startY, angoloP, mittente);
	                SwingUtilities.invokeLater(() -> proiettili.add(proiettile));
	            }
	            
	            break;
            case "asteroide":
//            	int asteroideX = receivedJson.get("x").getAsInt();
//	            int asteroideY = receivedJson.get("y").getAsInt();
//	            String imagePath = receivedJson.get("imagePath").getAsString();
//	            String name = receivedJson.get("name").getAsString();
//	            double asteroideAngolo = receivedJson.get("angolo").getAsDouble();
//	            double asteroideAngoloRotazione = receivedJson.get("angoloRotazione").getAsDouble();
//	            Asteroide asteroide = new Asteroide(this, name, imagePath);
//	            asteroide.setX(asteroideX);
//	            asteroide.setY(asteroideY);
//	            asteroide.setAngolo(asteroideAngolo);
//	            asteroide.setAngoloRotazione(asteroideAngoloRotazione);
//	            Map<String, Cache> obj = singleton.getObj();
//	            Cache tmp = obj.get(name);
//	            if(tmp==null) {
//	            	singleton.getObj().put(name, asteroide);
//	            }
	            
	            //controllaCollisioneNavAsteroid(entry);
	            //SwingUtilities.invokeLater(() -> asteroidi.add(asteroide));
            	break;
            default:
                System.err.println("GameClient: Tipo di messaggio sconosciuto: " + tipo);
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
	    send(jsonMessage.toString());
	}

	public void updateShipPosition(String nomeNavicella, int x, int y, double angolo) {
	    // Cerca la navicella specificata nella mappa degli oggetti di gioco
		Map<String, Cache> obj = singleton.getObj();
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
		Map<String, Cache> obj = singleton.getObj();
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
	        for (String nomeAsteroide : singleton.getNomiAsteroidi()) {
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
	        sendGameDimensions(this.getWidth(), this.getHeight());
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

	        Nav nave = (Nav) singleton.getObj().get(clientNavicella);
	        if (nave != null) {
	            double startX = nave.x + 30 * Math.cos(nave.angolo);
	            double startY = nave.y + 30 * Math.sin(nave.angolo);
	            //Proiettile proiettile = new Proiettile(startX, startY, nave.angolo);
	            Proiettile proiettile = singleton.getProiettile(startX, startY, nave.angolo, clientNavicella);
	            proiettili.add(proiettile);
	            
	            riproduciSuonoSparo(); // Riproduce il suono dello sparo
	            
	         // Invia messaggio di sparo al server
	            JsonObject jsonMessage = new JsonObject();
	            jsonMessage.addProperty("tipo", "sparo");
	            jsonMessage.addProperty("id", proiettile.getId());
	            jsonMessage.addProperty("x", startX);
	            jsonMessage.addProperty("y", startY);
	            jsonMessage.addProperty("mittente", clientNavicella);
	            jsonMessage.addProperty("angolo", nave.angolo);
	            send(jsonMessage.toString());
	        }
	    }
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (clientNavicella == null || !singleton.getObj().containsKey(clientNavicella)) {
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
	    List<Proiettile> activeProiettili = singleton.getActiveProiettili();
	    for (Proiettile proiettile : activeProiettili) {
	        proiettile.disegna(g2d);
	    }
	    
        // Controlla collisioni tra la navicella e il cursore
		controllaCollisioneNavCursore();

		// Crea una copia temporanea della mappa degli oggetti per iterazione sicura
		Map<String, Cache> tempObjects = new HashMap<>(singleton.getObj());
	    for (Entry<String, Cache> entry : tempObjects.entrySet()) {
	        Cache gameObject = entry.getValue();
	        if (gameObject instanceof Asteroide) {
	        	Asteroide a = (Asteroide) gameObject;
	            a.updateMovement();
	            // Controlla le collisioni con ogni asteroide qui
	            //controllaCollisioneNavAsteroid(entry);
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
		if (clientNavicella == null || !singleton.getObj().containsKey(clientNavicella)) {
	        // La navicella non è stata ancora impostata o non è presente nella mappa,
	        // quindi non procedere ulteriormente per evitare NullPointerException.
	        return;
	    }
		
		Shape circle = createCircle(cx, cy, 20);
		Area area2 = new Area(circle); // Area del cerchio
		Nav navicella = (Nav)singleton.getObj().get(clientNavicella);
		area1 = new Area(navicella.getTransf()); 
		
		area1.intersect(area2); //area1 diventa l'intersezione fra le 2
		isInCollision = !area1.isEmpty(); // Aggiorna lo stato di intersezione
	    
	    if (isInCollision) {
	    	navicella.speed = Double.MIN_VALUE; // Ferma la navicella
	        area1.reset();
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
	            singleton.releaseProiettile(proiettile); 
	            continue;
	        }
	    }

	}

	private void setupTimerSparo() {
		timerSparo = new javax.swing.Timer(100, new ActionListener() { // Imposta un intervallo appropriato per la frequenza di sparo
            @Override
            public void actionPerformed(ActionEvent e) {
                if (staSparando) {
                    spara();
                }
            }
        });
        timerSparo.start();
	}

	private void setupTimerGame() {
		// Inizializza e avvia il Timer
        gameTimer = new javax.swing.Timer(Conf._FPSms, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aggiornaGioco(); // Aggiorna lo stato del gioco
                repaint(); // Richiede il ridisegno del pannello
            }
        });
        gameTimer.start(); // Avvia il Timer
	}

	private void setupListeners() {
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
	}

	private void setupSfondo() {
		try {
            sfondo = ImageIO.read(new File(Conf._RESOURCES_IMG_PATH + Conf.SFONDO_JPG)); // Aggiusta il percorso secondo necessità
        } catch (IOException e) {
            e.printStackTrace();
            sfondo = null;
        }
	}
	
    private Shape createCircle(int x, int y, int r) {
        return new java.awt.geom.Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }
    
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
	    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
	        if (!isInCollision) {
	        	Nav nave = (Nav) singleton.getObj().get(clientNavicella);
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
            Nav nave = (Nav) singleton.getObj().get(clientNavicella);
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
	
	@Override
	public void mouseMoved(MouseEvent e) {  
	    Nav nave = (Nav) singleton.getObj().get(clientNavicella);
	    if (nave != null) { 
	        cx = e.getX();
	        cy = e.getY();
	        double angolo = Math.atan2(e.getY() - nave.y, e.getX() - nave.x);
	        nave.angolo = angolo;
	        sendPlayerPosition(nave.x, nave.y, nave.angolo);
	    }
	}
	
}

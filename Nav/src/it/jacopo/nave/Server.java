package it.jacopo.nave;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;

public class Server {
    private ServerSocket serverSocket;
    private final int port = 8080;
    private final List<Handler> clients = new CopyOnWriteArrayList<>();
    private int playerCount = 0;
    private final Map<String, Dimension> gameDimensions = new HashMap<>();
    private Singleton singleton = Singleton.getInstance(); 

    public Server() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);
        
        precaricaImmagini();
        
        
        
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
    
    public void setGameDimensions(String clientID, int larghezza, int altezza) {
        gameDimensions.put(clientID, new Dimension(larghezza, altezza));
        System.out.println("Dimensioni gioco aggiornate per " + clientID + ": " + larghezza + "x" + altezza);
    }

    public synchronized void removeClient(Handler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client disconnesso: " + clientHandler.getPlayerType());
    }
    
    private void iniziaTimerAggiornamentiProiettili() {
    	Timer timerAggiornamentoProiettili = new Timer(true);
        timerAggiornamentoProiettili.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                inviaAggiornamentiProiettili();
            }
        }, 0, 100); 
    }
    
    private void iniziaTimerAggiornamentiAsteroidi() {
    	Timer timerAggiornamentoAsteroidi = new Timer(true);
    	timerAggiornamentoAsteroidi.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                inviaAggiornamentiAsteroidi();
            }
        }, 0, 100); 
    }

    private void inviaAggiornamentiProiettili() {
    	List<Proiettile> proiettiliAttivi = singleton.getActiveProiettili();
    	Iterator<Proiettile> iterator = proiettiliAttivi.iterator();
        while (iterator.hasNext()) {
            Proiettile proiettile = iterator.next();
            proiettile.aggiorna();

            if (!proiettileValido(proiettile)) {
            	iterator.remove();  // Rimuovi il proiettile dalla lista dei proiettili attivi
            	singleton.releaseProiettile(proiettile);  // Restituisci il proiettile al pool
            } else {
            	// Invia aggiornamenti a tutti i clienti tranne al mittente del proiettile
                for (Handler client : clients) {
                    if (!client.getPlayerType().equals(proiettile.getMittente())) {
                    	
                    	JsonObject jsonMessage = new JsonObject();
                        jsonMessage.addProperty("tipo", "sparo");
                        jsonMessage.addProperty("mittente", proiettile.getMittente());
                        jsonMessage.addProperty("x", proiettile.getX());
                        jsonMessage.addProperty("y", proiettile.getY());
                        jsonMessage.addProperty("angolo", proiettile.angolo);
                        System.out.println("GameServer > "+client.getPlayerType()+": "+jsonMessage.toString());
                        client.sendMessage(jsonMessage.toString());
                    }
                }
            }
        }
    }
    
    private void inviaAggiornamentiAsteroidi() {
    	Map<String, Cache> obj = singleton.getObj();
    	Map<String, Cache> tempObjects = new HashMap<>(obj);
	    for (Entry<String, Cache> entry : tempObjects.entrySet()) {
	        Cache gameObject = entry.getValue();
	        if (gameObject instanceof Asteroide) {
	        	Asteroide asteroide = (Asteroide) gameObject;
	        	for (Handler client : clients) {
                	JsonObject jsonMessage = new JsonObject();
                    jsonMessage.addProperty("tipo", "asteroide");
                    jsonMessage.addProperty("name", asteroide.getName());
                    jsonMessage.addProperty("imagePath", asteroide.getImmaginePath());
                    jsonMessage.addProperty("x", asteroide.getX());
                    jsonMessage.addProperty("angoloRotazione", asteroide.getAngoloRotazione());
                    jsonMessage.addProperty("y", asteroide.getY());
                    jsonMessage.addProperty("angolo", asteroide.angolo);
                    System.out.println("GameServer > "+jsonMessage.toString());
                    client.sendMessage(jsonMessage.toString());
                }
	        }
	    }
    }
    
    public synchronized void broadcast(String message, String excludePlayerType) {
        for (Handler client : clients) {
            if (!client.getPlayerType().equals(excludePlayerType)) {
            	System.out.println("GameServer > "+client.getPlayerType()+" :"+message);
                client.sendMessage(message);
            }
        }
    }
    
    public synchronized void broadcast(String message) {
        for (Handler client : clients) {
            System.out.println("GameServer > " + client.getPlayerType() + " :" + message);
            client.sendMessage(message);
        }
    }

    private boolean proiettileValido(Proiettile proiettile) {
        for (Dimension dim : gameDimensions.values()) {
            if (proiettile.getX() >= 0 && proiettile.getX() <= dim.width &&
                proiettile.getY() >= 0 && proiettile.getY() <= dim.height) {
                return true;
            }
        }
        return false;
    }
    
    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                playerCount++;
                String playerType = playerCount == 1 ? "navicella1" : "navicella2";
                System.out.println("Giocatore " + playerCount + " connesso, assegnato " + playerType);

                Handler handler = new Handler(clientSocket, this, playerType);
                
                Nav navicella = new Nav(playerType);
                handler.setNavicella(navicella);
                
                clients.add(handler);
                
                if(playerCount==2) {
                	// Inizializza 15 asteroidi con posizioni iniziali visibili
            	    for (int i = 1; i <= Conf.asteroid_number; i++) { 
            	    	String nomeAsteroide = "asteroide" + i;
            	        Asteroide asteroide = new Asteroide(null, nomeAsteroide, Conf._RESOURCES_IMG_PATH + "asteroide" + i + ".png");
            	        asteroide.x = Conf.FRAME_WIDTH; 
            	        asteroide.y = i % Conf.FRAME_WIDTH * 100; 
            	        singleton.getNomiAsteroidi().add(nomeAsteroide);
            	        singleton.getObj().put(asteroide.name, asteroide);
            	        
            	    }
            	    
            	    for(Handler h : clients) {
            	    	new Thread(h).start();
            	    }
            	    
            	    
            	    iniziaTimerAggiornamentiProiettili();
                    iniziaTimerAggiornamentiAsteroidi();
                }
                else {
                	System.out.println("In attesa secondo giocatore...");
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
        
        
    }

}

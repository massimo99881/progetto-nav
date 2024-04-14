package it.jacopo.nave;

import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.JsonObject;

public class GameServer {
    private ServerSocket serverSocket;
    private final int port = 8080;
    private final List<Handler> clients = new CopyOnWriteArrayList<>();
    private int playerCount = 0;
    private final Map<String, Dimension> gameDimensions = new HashMap<>();
    private ProiettilePool proiettilePool = ProiettilePool.getInstance(); 
    private List<Asteroide> asteroidi = new CopyOnWriteArrayList<>();
    
    
    private Timer aggiungiAsteroidiTimer;
    private int aggiunteEffettuate = 0;

    public GameServer() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);
        
        Asteroide.precaricaImmagini();
        
        iniziaTimerAggiornamentiProiettili();
        iniziaTimerAggiornamentiAsteroidi();
        
        // Inizializza 15 asteroidi con posizioni iniziali visibili
	    for (int i = 1; i <= Conf.asteroid_number; i++) { 
	    	String nomeAsteroide = "asteroide" + i;
	        Asteroide asteroide = new Asteroide(nomeAsteroide, Conf._RESOURCES_IMG_PATH + "asteroide" + i + ".png");
	        asteroide.x = Conf.FRAME_WIDTH; // Tutti gli asteroidi partono dalla stessa posizione X iniziale
	        Random rand = new Random();
	        int numeroCasuale = rand.nextInt(441) + 10; // Genera un numero casuale tra 10 (incluso) e 451 (escluso)
	        asteroide.y = numeroCasuale; // Distribuisce gli asteroidi verticalmente
	        proiettilePool.getNomiAsteroidi().add(nomeAsteroide);
	        // Aggiungi l'asteroide alla mappa degli oggetti
	        proiettilePool.getObj().put(asteroide.name, asteroide);
	    }
    }
    
    private void aggiornaPosizioniAsteroidi() {
        for (Asteroide asteroide : asteroidi) {
            asteroide.updateMovement();
            JsonObject jsonMessage = new JsonObject();
            jsonMessage.addProperty("tipo", "aggiornamentoPosizioneAsteroide");
            jsonMessage.addProperty("nome", asteroide.getName());
            jsonMessage.addProperty("x", asteroide.getX());
            jsonMessage.addProperty("y", asteroide.getY());
            jsonMessage.addProperty("angoloRotazione", asteroide.getAngoloRotazione());
            jsonMessage.addProperty("speed", asteroide.getSpeed());
            jsonMessage.addProperty("angolo", asteroide.getAngolo());
            jsonMessage.addProperty("opacita", asteroide.getOpacita());
            jsonMessage.addProperty("colpiSubiti", asteroide.getColpiSubiti());  // Aggiungendo il numero di colpi subiti
            broadcast(jsonMessage.toString());
        }
    }
    
    private void verificaCollisioni() {
        for (Handler client : clients) {
            Nav navicella = client.getNavicella();
            for (Asteroide asteroide : asteroidi) {
                if (navicella.getBounds().intersects(asteroide.getBounds())) {
                    JsonObject jsonMessage = new JsonObject();
                    jsonMessage.addProperty("tipo", "collisione");
                    jsonMessage.addProperty("con", asteroide.getName());
                    client.sendMessage(jsonMessage.toString());
                }
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
    
    private void iniziaTimerAggiornamentiAsteroidi() {
    	
        aggiungiAsteroidiTimer = new Timer(true);
        aggiungiAsteroidiTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            	
            	if (aggiunteEffettuate < Conf.Level_Total) {
            		
                    aggiungiAsteroidi();
                    aggiunteEffettuate++;
                } else {
//                	try {
//        		        File fileAudio = new File(Conf._RESOURCES_AUDIO_PATH + "winner.wav"); 
//        		        AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileAudio);
//        		        clipAudio = AudioSystem.getClip();
//        		        clipAudio.open(audioStream);
//        		        clipAudio.start();
//        		    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
//        		        ex.printStackTrace();
//        		    }
//                    aggiungiAsteroidiTimer.stop(); // Ferma il Timer
//                    JOptionPane.showMessageDialog(null, "Hai vinto!", "Complimenti", JOptionPane.INFORMATION_MESSAGE);
//                    resetGame(); // Chiama resetGame dopo che l'utente ha fatto click su "OK"
                }
            	
                aggiornaPosizioniAsteroidi();
                verificaCollisioni();
            }
        }, 0, Conf.Level_timer);  
    }
    
 	private void aggiungiAsteroidi() {
 		
 	    for (int i = 0; i < Conf.MAX_AGGIUNTE; i++) {
 	        aggiungiAsteroide(proiettilePool.getObj()); 
 	    }
 	}
 	
 	private void aggiungiAsteroide(Map<String, Cache> obj) {
 	    Random rand = new Random();
 	    int posizioneYCasuale = rand.nextInt(441) + 10; 
 	    int indiceImmagineCasuale = rand.nextInt(Conf.asteroid_number) + 1; 
 	    String nomeAsteroide = "asteroide" + (proiettilePool.getNomiAsteroidi().size() + 1);
 	   proiettilePool.getNomiAsteroidi().add(nomeAsteroide); 
 	    Asteroide asteroide = new Asteroide(nomeAsteroide, Conf._RESOURCES_IMG_PATH + "asteroide" + indiceImmagineCasuale + ".png");
 	    Dimension n = gameDimensions.get("navicella1");
 	    asteroide.x = Conf.FRAME_WIDTH-5;
 	    asteroide.y = posizioneYCasuale;
 	    obj.put(nomeAsteroide, asteroide);
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

    private void inviaAggiornamentiProiettili() {
    	List<Proiettile> proiettiliAttivi = proiettilePool.getActiveProiettili();
    	Iterator<Proiettile> iterator = proiettiliAttivi.iterator();
        while (iterator.hasNext()) {
            Proiettile proiettile = iterator.next();
            proiettile.aggiorna();

            if (!proiettileValido(proiettile)) {
            	iterator.remove();  // Rimuovi il proiettile dalla lista dei proiettili attivi
                proiettilePool.releaseProiettile(proiettile);  // Restituisci il proiettile al pool
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
                new Thread(handler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        GameServer server = new GameServer();
        server.start();
        
        
    }

}

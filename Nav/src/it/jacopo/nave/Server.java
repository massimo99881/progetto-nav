package it.jacopo.nave;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import com.google.gson.JsonObject;

public class Server {
    private ServerSocket serverSocket;
    private final List<Handler> clients = new CopyOnWriteArrayList<>();
    private Singleton singleton = Singleton.getInstance();
    private long ntpTime;
    
    private Map<String, Integer> destroiedAsteroidsCount = new HashMap<>();
    
    private Timer ondateTimer;
    private boolean isPaused = false;
    private int ondataAttuale = 0;
    
    private Map<String, Integer> asteroidsDestroyedCounts = new HashMap<>();

    public Server() throws IOException {
    	ntpTime = getNtpTime();
        serverSocket = new ServerSocket(8086);
        System.out.println("Server avviato sulla porta 8086");
        ondateTimer = new Timer();
        programmaOndate();
        
    }
    
    void broadcastAsteroidDestruction(String asteroideName, String excludePlayerType) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "asteroideDistrutto");
        jsonMessage.addProperty("nomeAsteroide", asteroideName);
        String message = jsonMessage.toString();
        for (Handler client : clients) {
            if (!client.getPlayerType().equals(excludePlayerType)) {
                client.sendMessage(message);
            }
        }
    }

    
    public Nav getNavicella(String playerType) {
        for (Handler client : clients) {
            if (client.getPlayerType().equals(playerType)) {
                return client.getNavicella();
            }
        }
        return null; // Return null if no matching navicella is found
    }
    
    public void receiveAsteroidsDestroyedReport(String playerType, int count) {
        asteroidsDestroyedCounts.put(playerType, count);
        
        // Controlla se sono stati ricevuti i conteggi da entrambi i giocatori
        if (asteroidsDestroyedCounts.size() == 2) {
            evaluateGameOutcome();
        }
    }

    private void evaluateGameOutcome() {
        int asteroidsDestroyed1 = asteroidsDestroyedCounts.getOrDefault("navicella1", 0);
        int asteroidsDestroyed2 = asteroidsDestroyedCounts.getOrDefault("navicella2", 0);

        System.out.println("Distrutti Nav1: "+asteroidsDestroyed1);
        System.out.println("Distrutti Nav2: "+asteroidsDestroyed2);
        
        String winner, loser;
        if (asteroidsDestroyed1 > asteroidsDestroyed2) {
            winner = "navicella1";
            loser = "navicella2";
        } else if (asteroidsDestroyed1 < asteroidsDestroyed2) {
            winner = "navicella2";
            loser = "navicella1";
        } else {
            sendEndGameMessageToNav("navicella1", "PAREGGIO! Entrambe le navicelle hanno distrutto lo stesso numero di asteroidi.");
            sendEndGameMessageToNav("navicella2", "PAREGGIO! Entrambe le navicelle hanno distrutto lo stesso numero di asteroidi.");
            return;
        }

        sendEndGameMessageToNav(winner, "HAI VINTO per aver distrutto più asteroidi!");
        sendEndGameMessageToNav(loser, "HAI PERSO per aver distrutto meno asteroidi.");
        asteroidsDestroyedCounts.clear(); // Pulisci i conteggi per il prossimo gioco
    }

    public void sendEndGameMessageToNav(String nav, String message) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "gameEnd");
        jsonMessage.addProperty("message", message);
        
        for (Handler client : clients) {
            if (client.getPlayerType().equals(nav)) {
            	
                client.sendMessage(jsonMessage.toString());
            }
        }
    }
    
    public void processDeath(String playerType) {
        // Logic to process player death
        System.out.println(playerType + " has died.");

        String winner = playerType.equals("navicella1") ? "navicella2" : "navicella1";
        sendEndGameMessageToNav(winner, "HAI VINTO! perché l'altro giocatore è stato colpito da un asteroide");

    }

    
    private void programmaOndate() {
        ondateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isPaused) return; // Controlla se il timer è in pausa
                
                if (ondataAttuale <= Conf.MAX_ONDATE) {
                    try {
                        //if (ondataAttuale!=Conf.MAX_ONDATE) 
                        scheduleAsteroidCreation();
                        ondataAttuale++;
                        broadcastWaveUpdate();
                    } catch (IOException e) {
                        System.err.println("Errore nella programmazione degli asteroidi: " + e.getMessage());
                    }
                } else {
                    ondateTimer.cancel();  // Fermare il timer dopo l'ultima ondata
                    //checkAsteroidWavesCompletion();
                }
            }
        }, 0, 30000); // Schedula una nuova ondata ogni 30 secondi
    }
    
    private void broadcastWaveUpdate() {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "updateWave");
        jsonMessage.addProperty("ondataAttuale", ondataAttuale);
        String message = jsonMessage.toString();
        broadcast(message);  // Usa il metodo broadcast esistente per inviare il messaggio a tutti i client
    }

    
    private long getNtpTime() throws IOException {
        String timeServer = "pool.ntp.org";
        NTPUDPClient client = new NTPUDPClient();
        client.open();
        try {
            InetAddress address = InetAddress.getByName(timeServer);
            TimeInfo timeInfo = client.getTime(address);
            long time = timeInfo.getMessage().getTransmitTimeStamp().getTime();
            time /= 1000;
            System.out.println("ntpTime = "+time);
            return time;
        } finally {
            client.close();
        }
    }
    
    public void scheduleAsteroidCreation() throws IOException {
    	ntpTime += 1000; // 1 secondi nel futuro
        int seed = new Random().nextInt(); // Genera un seed casuale
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "startAsteroidi");
        jsonMessage.addProperty("ntpTime", ntpTime);
        jsonMessage.addProperty("seed", seed);
        jsonMessage.addProperty("ondata", ondataAttuale);
        broadcast(jsonMessage.toString());
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
                        //System.out.println("GameServer > "+client.getPlayerType()+": "+jsonMessage.toString());
                        client.sendMessage(jsonMessage.toString());
                    }
                }
            }
        }
    }
    
    public synchronized void broadcast(String message, String excludePlayerType) {
        for (Handler client : clients) {
            if (!client.getPlayerType().equals(excludePlayerType)) {
            	//System.out.println("GameServer > "+client.getPlayerType()+" :"+message);
                client.sendMessage(message);
            }
        }
    }
    
    public synchronized void broadcast(String message) {
        for (Handler client : clients) {
            client.sendMessage(message);
            //System.out.println("Server: Broadcasting message to " + client.getPlayerType());
        }
    }

    private boolean proiettileValido(Proiettile proiettile) {
        if (proiettile.getX() >= 0 && proiettile.getX() <= Conf.FRAME_WIDTH &&
            proiettile.getY() >= 0 && proiettile.getY() <= Conf.FRAME_HEIGHT) {
            return true;
        }
        return false;
    }
    
    public void start() {
    	int playerCount = 0;
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
                	
                	startGame();  // Avvia il gioco quando entrambi i giocatori sono connessi
                	scheduleAsteroidCreation();
                	
            	    for(Handler h : clients) {
            	    	new Thread(h).start();
            	    }
            	    
            	    
            	    iniziaTimerAggiornamentiProiettili();
                }
                else {
                	System.out.println("In attesa secondo giocatore...");
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
 // Metodo nel server per gestire l'inizio e la fine del movimento della finestra
    public void handleWindowMoveStart() {
        isPaused = true; // Imposta il timer su pausa
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "suspendUpdates");
        //System.out.println("Server: Sending suspendUpdates to all clients.");
        broadcast(jsonMessage.toString());
    }

    public void handleWindowMoveEnd() {
        isPaused = false; // Riprende il timer
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "resumeUpdates");
        //System.out.println("Server: Sending resumeUpdates to all clients.");
        broadcast(jsonMessage.toString());
    }


    public synchronized void startGame() {
        if (clients.size() == 2) { // Assumi che il gioco richieda esattamente due client
            JsonObject jsonMessage = new JsonObject();
            jsonMessage.addProperty("tipo", "startGame");
            broadcast(jsonMessage.toString());  // Invia il messaggio di inizio gioco a tutti i client
        }
    }

        
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
        
        
    }

}

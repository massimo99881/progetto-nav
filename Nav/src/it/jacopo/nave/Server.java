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
    private int ondataAttuale = 0;
    private final int numeroOndate = 5;  // Numero totale di ondate
    private Map<String, Integer> destroiedAsteroidsCount = new HashMap<>();

    public Server() throws IOException {
    	ntpTime = getNtpTime();
        serverSocket = new ServerSocket(8080);
        System.out.println("Server avviato sulla porta 8080");
        
        destroiedAsteroidsCount.put("navicella1", 0);
        destroiedAsteroidsCount.put("navicella2", 0);
        
        programmaOndate();
        
    }
    
    public Nav getNavicella(String playerType) {
        for (Handler client : clients) {
            if (client.getPlayerType().equals(playerType)) {
                return client.getNavicella();
            }
        }
        return null; // Return null if no matching navicella is found
    }
    
    private void checkAsteroidWavesCompletion() {
        if (ondataAttuale >= numeroOndate) {
            // Assume che il gioco abbia esattamente due navicelle: navicella1 e navicella2
            Nav nav1 = getNavicella("navicella1");
            Nav nav2 = getNavicella("navicella2");
            
            System.out.println("asteroidi nav1 "+nav1.getAsteroidiDistrutti()+ ", "+nav2.getAsteroidiDistrutti());

            // Calcola il vincitore e il perdente
            String winner, loser;
            if (nav1.getAsteroidiDistrutti() > nav2.getAsteroidiDistrutti()) {
                winner = "navicella1";
                loser = "navicella2";
            } else if (nav1.getAsteroidiDistrutti() < nav2.getAsteroidiDistrutti()) {
                winner = "navicella2";
                loser = "navicella1";
            } else {
                // In caso di parità, potresti decidere di inviare un messaggio di pareggio
            	sendEndGameMessageToNav("navicella1", "Pareggio! Entrambe le navicelle hanno distrutto lo stesso numero di asteroidi.");
                sendEndGameMessageToNav("navicella2", "Pareggio! Entrambe le navicelle hanno distrutto lo stesso numero di asteroidi.");
                return;
            }

            // Invia il messaggio di vittoria al vincitore
            sendEndGameMessageToNav(winner, "Hai vinto per aver distrutto più asteroidi!");
            sendEndGameMessageToNav(loser,"Hai perso per aver distrutto meno asteroidi.");
        }
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

    
    private void checkAndEndGame(String deceased) {
        // Increment death count
        int deaths = destroiedAsteroidsCount.get(deceased) + 1;
        destroiedAsteroidsCount.put(deceased, deaths);

        // Check if it's the second death
        if (deaths == 1) {
            // Determine the winner
            String winner = deceased.equals("navicella1") ? "navicella2" : "navicella1";
            sendEndGameMessageToNav(winner, "Hai vinto perché l'altro giocatore è morto");
        }
    }
    
    public void processDeath(String playerType) {
        // Logic to process player death
        System.out.println(playerType + " has died.");

        // Update death count and check for game end condition
        checkAndEndGame(playerType);
    }

    
    private void programmaOndate() {
    	Timer ondateTimer = new Timer();
        ondateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (ondataAttuale < numeroOndate) {
                        scheduleAsteroidCreation();
                        ondataAttuale++;
                    } else {
                        ondateTimer.cancel();  // Fermare il timer dopo l'ultima ondata
                        checkAsteroidWavesCompletion();
                    }
                } catch (IOException e) {
                    System.err.println("Errore nella programmazione degli asteroidi: " + e.getMessage());
                }
            }
        }, 0, 30000);  // Schedula una nuova ondata ogni 30 secondi
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
            client.sendMessage(message);
            System.out.println("Server: Broadcasting message to " + client.getPlayerType());
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
    public synchronized void handleWindowMoveStart() {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "suspendUpdates");
        System.out.println("Server: Sending suspendUpdates to all clients.");
        broadcast(jsonMessage.toString());
    }

    public synchronized void handleWindowMoveEnd() {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "resumeUpdates");
        System.out.println("Server: Sending resumeUpdates to all clients.");
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

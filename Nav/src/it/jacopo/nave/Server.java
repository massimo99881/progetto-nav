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
        Asteroide.precaricaImmagini();
        iniziaTimerAggiornamentiProiettili();
	    
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
        Server server = new Server();
        server.start();
        
        
    }

}

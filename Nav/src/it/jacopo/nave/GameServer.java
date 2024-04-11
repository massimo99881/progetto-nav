package it.jacopo.nave;

import java.awt.Dimension;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GameServer {
    private ServerSocket serverSocket;
    private final int port = 8080;
    private final List<Handler> clients = new CopyOnWriteArrayList<>();
    private int playerCount = 0;
    private final Map<String, Dimension> gameDimensions = new HashMap<>();
    private List<Proiettile> proiettiliAttivi = new CopyOnWriteArrayList<>();
    private Timer timerAggiornamentoProiettili;

    public GameServer() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);
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
        timerAggiornamentoProiettili = new Timer(true);
        timerAggiornamentoProiettili.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            	//System.out.println("Invio aggiornamenti proiettili...");
                inviaAggiornamentiProiettili();
            }
        }, 0, 100); // aggiorna ogni 100ms
    }

    private void inviaAggiornamentiProiettili() {
        synchronized (proiettiliAttivi) {
            Iterator<Proiettile> iterator = proiettiliAttivi.iterator();
            while (iterator.hasNext()) {
                Proiettile proiettile = iterator.next();
                proiettile.aggiorna();

                if (!proiettileValido(proiettile)) {
                    iterator.remove();
                } else {
                    
                    for (Handler client : clients) {
                        if (!client.getPlayerType().equals(proiettile.getMittente())) {
                        	JsonObject jsonMessage = new JsonObject();
                            jsonMessage.addProperty("tipo", "sparo");
                            jsonMessage.addProperty("mittente", proiettile.getMittente());
                            jsonMessage.addProperty("x", proiettile.getX());
                            jsonMessage.addProperty("y", proiettile.getY());
                            jsonMessage.addProperty("angolo", proiettile.angolo);
                        	System.out.println("GameServer > "+client.getPlayerType()+" sparo:"+jsonMessage.toString());
                            client.sendMessage(jsonMessage.toString());
                        }
                    }
                }
            }
        }
    }
    
    public synchronized void broadcast(String message, String excludePlayerType) {
        for (Handler client : clients) {
            if (!client.getPlayerType().equals(excludePlayerType)) {
            	System.out.println("GameServer > "+client.getPlayerType()+" posizione:"+message);
                client.sendMessage(message);
            }
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
    
    public synchronized void aggiungiProiettileAttivo(Proiettile proiettile) {
        proiettiliAttivi.add(proiettile);
        inviaAggiornamentiProiettili();
    }


    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                playerCount++;
                String playerType = playerCount == 1 ? "navicella1" : "navicella2";
                System.out.println("Giocatore " + playerCount + " connesso, assegnato " + playerType);

                Handler handler = new Handler(clientSocket, this, playerType);
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

    class Handler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private GameServer server;
        private String playerType;

        public Handler(Socket socket, GameServer server, String playerType) throws IOException {
            this.clientSocket = socket;
            this.server = server;
            this.playerType = playerType;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            try {
                // Initial setup messages to client
                informClientOfPlayerType();

                // Main loop for client communication
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    processReceivedData(inputLine);
                }
            } catch (IOException e) {
                System.out.println("Exception in handler: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void informClientOfPlayerType() {
            JsonObject message = new JsonObject();
            message.addProperty("tipo", "tipoNavicella");
            message.addProperty("navicella", playerType);
            sendMessage(message.toString());
        }

        private void processReceivedData(String data) {
            JsonObject receivedJson = JsonParser.parseString(data).getAsJsonObject();
            String tipo = receivedJson.get("tipo").getAsString();
            switch (tipo) {
                case "dimensioniGioco":
                    int larghezza = receivedJson.get("larghezza").getAsInt();
                    int altezza = receivedJson.get("altezza").getAsInt();
                    server.setGameDimensions(playerType, larghezza, altezza);
                    break;
                case "posizione":
                    // Assumendo che il messaggio contenga le chiavi "x", "y", e "angolo"
                    int x = receivedJson.get("x").getAsInt();
                    int y = receivedJson.get("y").getAsInt();
                    double angolo = receivedJson.get("angolo").getAsDouble();
                    // Qui potresti voler fare qualcosa con queste informazioni, come aggiornare la posizione di una navicella
                    // Ma per ora, semplicemente le inoltriamo agli altri client
                    server.broadcast(data, playerType);
                    break;
                case "sparo":
                    // Assumendo che il messaggio contenga le coordinate di partenza del proiettile e l'angolo
                    double startX = receivedJson.get("x").getAsDouble();
                    double startY = receivedJson.get("y").getAsDouble();
                    double angoloSparo = receivedJson.get("angolo").getAsDouble();
                    // Creare un nuovo proiettile e aggiungerlo alla lista dei proiettili attivi nel server
                    Proiettile proiettile = new Proiettile(startX, startY, angoloSparo, playerType); // Nota: assicurati che la classe Proiettile abbia un costruttore adeguato
                    System.out.println("GameServer:sparo: "+receivedJson);
                    server.aggiungiProiettileAttivo(proiettile);
                    break;
                // Aggiungi qui altri casi se necessario
                default:
                    System.err.println("GameServer: Tipo di evento sconosciuto: " + tipo);
                    break;
            }
            // Non facciamo il broadcast di ogni messaggio ricevuto; solo quelli che vogliamo condividere con altri client.
        }


        private void cleanup() {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.removeClient(this);
        }

        void sendMessage(String message) {
            out.println(message);
        }

        public String getPlayerType() {
            return playerType;
        }
    }

	public void removeClient(it.jacopo.nave.Handler clientHandler) {
		clients.remove(clientHandler);
	    System.out.println("Client disconnesso: " + clientHandler.getPlayerType());
	}

}

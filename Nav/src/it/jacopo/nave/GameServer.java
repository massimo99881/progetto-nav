package it.jacopo.nave;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.JsonObject;

public class GameServer {
    private ServerSocket serverSocket;
    private final int port = 8080;
    private final List<Handler> clients = new CopyOnWriteArrayList<>();
    private int playerCount = 0; // Variabile per tenere traccia del numero di giocatori connessi

    public GameServer() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                playerCount++; // Incrementa il conteggio dei giocatori
                String playerType = playerCount == 1 ? "navicella1" : "navicella2";
                System.out.println("Giocatore " + playerCount + " connesso, assegnato " + playerType);
                
                Handler handler = new Handler(clientSocket, this, playerType);
                clients.add(handler);
                new Thread(handler).start();
                
                if (playerCount >= 2) {
                    System.out.println("Massimo numero di giocatori connessi.");
                    // Opzionale: Potresti voler gestire la situazione in cui ci sono più di 2 giocatori
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void broadcast(String message, Handler excludeUser) {
        for (Handler aClient : clients) {
            if (aClient != excludeUser) {
                aClient.sendMessage(message);
            }
        }
    }

    public synchronized void removeClient(Handler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client disconnesso: " + clientHandler.clientSocket.getInetAddress().getHostAddress());
    }

    public static void main(String[] args) throws IOException {
        GameServer server = new GameServer();
        server.start();
    }

    private static class Handler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private GameServer server;
        private String playerType; // Tipo di navicella assegnato a questo handler

        public Handler(Socket socket, GameServer server, String playerType) {
            this.clientSocket = socket;
            this.server = server;
            this.playerType = playerType;
        }

        @Override
        public void run() {
            try {
                InputStream input = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Crea un messaggio JSON per inviare il tipo di navicella assegnato al client
                JsonObject jsonMessage = new JsonObject();
                jsonMessage.addProperty("tipo", "tipoNavicella");
                jsonMessage.addProperty("navicella", playerType);
                sendMessage(jsonMessage.toString()); // Utilizza il metodo già esistente per inviare il messaggio

                while (true) {
                    String receivedText = reader.readLine();
                    if (receivedText != null) {
                        System.out.println("Ricevuto: " + receivedText);
                        // Qui potresti voler elaborare il testo ricevuto prima di inoltrarlo
                        server.broadcast(receivedText, this);
                    } else {
                        // Potrebbe indicare che il client ha chiuso la connessione
                        System.out.println("Il client potrebbe aver chiuso la connessione");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Errore o disconnessione del client: " + e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.removeClient(this);
                System.out.println("Client rimosso: " + playerType);
            }
        }


        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}

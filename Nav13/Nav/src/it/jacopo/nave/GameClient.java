package it.jacopo.nave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String serverAddress = "127.0.0.1";
    private final int serverPort = 8080;
    private volatile boolean running = true; // Flag per controllare il ciclo di ricezione
    private String playerType;
    private ProiettilePool proiettilePool ;


    public GameClient(ProiettilePool proiettilePool) throws IOException {
    	this.proiettilePool = proiettilePool;
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new IOException("Impossibile stabilire la connessione con il server.", e);
        }
    }

    public void send(String message) {
        out.println(message);
    }

    public void close() {
        try {
            running = false; // Ferma il ciclo di ricezione
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClient(Consumer<String> onMessageReceived) {
        new Thread(() -> {
            while (running) {
                try {
                    String message = in.readLine();
                    if (message != null) {
                        handleIncomingMessage(message, onMessageReceived);
                    } else {
                        System.out.println("Connessione terminata dal server.");
                        break;
                    }
                } catch (IOException e) {
                    if (running) { // Errore inaspettato
                        System.err.println("Errore di connessione: " + e.getMessage());
                    }
                    break;
                }
            }
            close();
        }, "Client-Receiver").start();
    }
    
    public void sendGameDimensions(int width, int height) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("tipo", "dimensioniGioco");
        jsonMessage.addProperty("larghezza", width);
        jsonMessage.addProperty("altezza", height);
        send(jsonMessage.toString());
    }


    private void handleIncomingMessage(String message, Consumer<String> onMessageReceived) {
        JsonObject receivedJson = JsonParser.parseString(message).getAsJsonObject();
        String tipo = receivedJson.get("tipo").getAsString();

        switch (tipo) {
            case "tipoNavicella":
                playerType = receivedJson.get("navicella").getAsString();
                System.out.println("Tipo di navicella assegnato: " + playerType);
                break;
            case "aggiornamentoStato":
                // Gestisci l'aggiornamento dello stato del gioco qui
                break;
            case "posizione":
                // Gestisci qui l'aggiornamento della posizione
                // Ad esempio, potresti voler aggiornare la posizione di una navicella sul client
                int x = receivedJson.get("x").getAsInt();
                int y = receivedJson.get("y").getAsInt();
                // Assicurati di avere un metodo o un modo per aggiornare la posizione basandoti su queste informazioni
                //System.out.println("Aggiornamento posizione ricevuto: x=" + x + ", y=" + y);
                break;
            case "sparo":
                //String mittente = receivedJson.get("mittente").getAsString();
            	System.out.println("GameClient: "+receivedJson);
                double xP = receivedJson.get("x").getAsDouble();
                double yP = receivedJson.get("y").getAsDouble();
                double angoloP = receivedJson.has("angolo") ? receivedJson.get("angolo").getAsDouble() : 0; // Assumiamo che il server invii anche l'angolo
                String mittente = receivedJson.get("mittente").getAsString();
                
                proiettilePool.getProiettile(xP, yP, angoloP, mittente);
                
                
                break;

            default:
                System.err.println("GameClient: Tipo di messaggio sconosciuto: " + tipo);
                break;
        }

        onMessageReceived.accept(message);
    }


//    public static void main(String[] args) {
//        try {
//        	ProiettilePool proiettilePool = ProiettilePool.getInstance();
//            GameClient client = new GameClient(proiettilePool);
//            client.startClient(message -> {
//                // Qui puoi decidere come gestire i messaggi ricevuti
//            });
//        } catch (IOException e) {
//            System.err.println("Errore nell'avviare il client: " + e.getMessage());
//        }
//    }
}

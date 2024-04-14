package it.jacopo.nave;

import java.io.*;
import java.net.Socket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Handler implements Runnable {
	private Nav navicella;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Server server;
    private String playerType; // Tipo di navicella assegnato a questo handler

    public Handler(Socket socket, Server server, String playerType) {
        this.clientSocket = socket;
        this.server = server;
        this.playerType = playerType;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Invio al client il tipo di navicella assegnato
            JsonObject jsonMessage = new JsonObject();
            jsonMessage.addProperty("tipo", "tipoNavicella");
            jsonMessage.addProperty("navicella", playerType);
            sendMessage(jsonMessage.toString());

            String receivedText;
            while ((receivedText = in.readLine()) != null) {
                System.out.println("Ricevuto: " + receivedText);
                JsonObject receivedJson = JsonParser.parseString(receivedText).getAsJsonObject();
                String tipo = receivedJson.get("tipo").getAsString();

                switch (tipo) {
	                case "sparo":
	                    // Gestione di un messaggio di sparo
	                	System.out.println("Handler < "+this.playerType+" sparo: "+receivedJson);
	                    //double startX = receivedJson.get("x").getAsDouble();
	                    //double startY = receivedJson.get("y").getAsDouble();
	                    //double angolo = receivedJson.get("angolo").getAsDouble();
	                    //String id = receivedJson.get("id").getAsString();
	                    // Use ProiettilePool directly
	                    //Proiettile proiettile = ProiettilePool.getInstance().getProiettile(startX, startY, angolo, this.playerType);
	                    //server.broadcast(proiettile.toJson().toString(), this.playerType);
                    
                    //break;
                    case "posizione":
                        // Inoltra il messaggio di posizione agli altri client
                        server.broadcast(receivedText, this.playerType);
                        break;
                    case "dimensioniGioco":
                        // Estrai le dimensioni del gioco dal messaggio
                        int larghezza = receivedJson.get("larghezza").getAsInt();
                        int altezza = receivedJson.get("altezza").getAsInt();
                        // Chiama il metodo setGameDimensions del server
                        server.setGameDimensions(this.playerType, larghezza, altezza);
                        System.out.println("Dimensioni gioco ricevute da " + clientSocket.getInetAddress().getHostAddress() + ": " + larghezza + "x" + altezza);
                        break;
                    
                    default:
                        System.err.println("Tipo di evento sconosciuto: " + tipo);
                        break;
                }
            }

            // Se receive() restituisce null, significa che il client ha chiuso la connessione
            System.out.println("Il client potrebbe aver chiuso la connessione");
        } catch (IOException e) {
            System.out.println("Errore o disconnessione del client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    public String getPlayerType() {
        return playerType;
    }
    
    public Nav getNavicella() {
        return navicella;
    }

    public void setNavicella(Nav navicella) {
        this.navicella = navicella;
    }

    private void cleanup() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Correctly using server's method to remove this client handler
//        server.removeClient(this);
        System.out.println("Client rimosso: " + playerType);
    }
}

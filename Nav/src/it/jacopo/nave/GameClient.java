package it.jacopo.nave;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String serverAddress = "127.0.0.1";
    private final int serverPort = 8080;
    private boolean running = true; // Flag per controllare il ciclo di ricezione

    public GameClient() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void send(String message) {
        out.println(message);
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        running = false; // Imposta il flag a false per fermare il thread di ricezione
        in.close();
        out.close();
        socket.close();
    }

    // Metodo per iniziare a ricevere messaggi dal server e gestirli
    public void startClient(Consumer<String> onMessageReceived) {
        new Thread(() -> {
            try {
                while (running) {
                    String receivedMessage = receive();
                    if (receivedMessage != null) {
                        System.out.println("Server dice: " + receivedMessage);
                        onMessageReceived.accept(receivedMessage);
                    } else {
                        // Se receive() restituisce null, il server ha chiuso la connessione
                        System.out.println("Connessione chiusa dal server.");
                        break;
                    }
                }
            } catch (IOException e) {
                if (running) { // Se il flag è ancora true, si tratta di un errore non previsto
                    System.err.println("Errore nella connessione: " + e.getMessage());
                    e.printStackTrace();
                } else {
                    System.out.println("Connessione chiusa.");
                }
            }
        }).start();
    }

    // Metodo main per test
    public static void main(String[] args) {
        try {
            GameClient client = new GameClient();
            client.startClient(message -> {
                // Qui puoi decidere come gestire i messaggi ricevuti, es. aggiornare UI
                if (message.startsWith("tipoNavicella:")) {
                    // Estrai il tipo di navicella e gestiscilo
                    String tipoNavicella = message.substring("tipoNavicella:".length());
                    System.out.println("Mi e' stata assegnata la " + tipoNavicella);
                }
                // Aggiungi qui altre condizioni per gestire diversi tipi di messaggi
            });
        } catch (IOException e) {
            System.err.println("Impossibile connettersi al server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

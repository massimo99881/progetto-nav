# Space Defenders

## Introduzione
Space Defenders è un gioco arcade spaziale dove il giocatore controlla una navicella spaziale con l'obiettivo di distruggere gli asteroidi avvicinanti e evitare le collisioni.

## Funzionalità Principali

### Gameplay
Il giocatore può muovere la navicella spaziale su e giù e sparare proiettili per distruggere gli asteroidi. L'obiettivo è sopravvivere il più a lungo possibile evitando gli asteroidi e distruggendoli per guadagnare punti.

### Gestione delle Collisioni
Le collisioni tra proiettili e asteroidi sono gestite attraverso un sistema di rilevamento delle collisioni che utilizza sia verifiche di bounding box per un'ottimizzazione preliminare, sia controlli più dettagliati per la conferma delle collisioni.

- **Ottimizzazione della Distanza**: Abbiamo introdotto una soglia di distanza per ottimizzare ulteriormente la verifica delle collisioni, evitando controlli dettagliati quando gli oggetti sono lontani tra loro.

```java
if (distanza >= 30) {
	// Rimuovi il proiettile
    iterProiettili.remove(); 
    // Rilascia il proiettile nel pool per il riutilizzo
    proiettilePool.releaseProiettile(proiettile); 
    // Aggiorna lo stato dell'asteroide per il colpo ricevuto
    asteroide.colpito(); 

    if (asteroide.getColpiSubiti() >= 5) {
    	// Rimuovi l'asteroide se è stato distrutto
        iterObj.remove(); 
        // Stampa un messaggio in console
        System.out.println(asteroide.name + " è stato distrutto"); 
    }
    // Esci dal ciclo se una collisione è stata trovata
    break; 
}
```


### Rendering
Il gioco utilizza BufferedImage per un rendering efficiente degli oggetti del gioco. Le immagini degli asteroidi e della navicella sono precaricate e ridimensionate per ottimizzare le prestazioni di rendering.

```java
if (!imageCache.containsKey(path)) {
	try {
		Random rand = new Random();
        BufferedImage originalImage = ImageIO.read(new File(path));
        double scaleFactor = !path.contains("asteroide1.png") ? 0.2 + (0.45 - 0.2) * rand.nextDouble() : 0.2;
        int newWidth = (int) (originalImage.getWidth() * scaleFactor);
        int newHeight = (int) (originalImage.getHeight() * scaleFactor);
        Cache ac = new Cache(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
        imageCache.put(path, ac);
    } 
	catch (IOException e) {
        e.printStackTrace();
    }
	catch (Exception e) {
        System.err.println("Errore nel caricamento dell'immagine da: " + path);
        e.printStackTrace();
    }
}
```

### Pooling dei Proiettili
Per ridurre il carico sulla garbage collection e migliorare le prestazioni, implementiamo il pooling dei proiettili, riutilizzando gli oggetti proiettile invece di crearne di nuovi ad ogni sparo.

```java
// Metodo per ottenere un proiettile dal pool
public Proiettile getProiettile(double x, double y, double angolo) {
    Proiettile proiettile;
    if (pool.isEmpty()) {
        // Se il pool è vuoto, crea un nuovo proiettile
        proiettile = new Proiettile(x, y, angolo);
    } else {
        // Altrimenti, riutilizza un proiettile esistente e aggiorna la sua posizione e angolo
        proiettile = pool.poll();
        proiettile.reset(x, y, angolo);
    }
    return proiettile;
}
```

### Ottimizzazione della Memoria e della CPU
Utilizziamo tecniche come il caching delle trasformazioni e la riduzione dei calcoli nelle verifiche delle collisioni per migliorare ulteriormente le prestazioni del gioco.

```java
@Override
void draw(Graphics2D g) {
    // Prima di disegnare l'asteroide, controlliamo se la sua posizione o l'angolo di rotazione è cambiato
    if (x != prevX || y != prevY || angoloRotazione != prevAngoloRotazione || cachedTransform == null) {
        // Se ci sono stati cambiamenti, aggiorniamo la cachedTransform con i nuovi valori
        cachedTransform.setToIdentity();
        cachedTransform.translate(x, y);
        cachedTransform.rotate(angoloRotazione, image.getWidth(null) / 2, image.getHeight(null) / 2);
        
        // Aggiorna le variabili con i valori correnti per il prossimo controllo
        prevX = x;
        prevY = y;
        prevAngoloRotazione = angoloRotazione;
    }
    
    // Usa la trasformazione memorizzata nella cache per disegnare l'asteroide
    g.drawImage(image, cachedTransform, null);
}
```

## Requisiti di Sistema
- Java Runtime Environment 8 o superiore
- Risoluzione schermo consigliata: 1280x720

## Installazione e Esecuzione
Scarica l'ultima release dal nostro repository GitHub e avvia il gioco eseguendo il file JAR tramite il comando:
```bash
java -jar space-defenders.jar
```


## TODO LIST:
- [OK] ingrandire qualche immagine
- [OK] riposizionare le immagini in una cartella resources/img
- [OK] mettere l'immagine nav accesa/spenta
- [OK] (trasparenza) implementare le esplosioni (asteroide e magari quando il proiettile colpisce)
- [OK] proiettili seguono la direzione della linea che parte dalla punta della navicella.
- [WIP] ricerca ottimizzazioni memoria
- [OK] asteroide1 
- resetGame (ricomincia da capo? SI)
- sound on/OFF

## Contribuire
Siamo aperti a contributi! Se vuoi migliorare il gioco o aggiungere nuove funzionalità, sentiti libero di aprire una pull request. Per contribuire al progetto, segui questi passaggi:

1. Forka il repository.
2. Crea un nuovo branch per la tua feature (`git checkout -b feature/AmazingFeature`).
3. Effettua le tue modifiche e commit (`git commit -am 'Add some AmazingFeature'`).
4. Pusha al branch (`git push origin feature/AmazingFeature`).
5. Apri una Pull Request.

Ti invitiamo a discutere le modifiche che intendi apportare tramite issue prima di iniziare a lavorare su di esse, per coordinare meglio il lavoro con il team di sviluppo e assicurare che le modifiche possano essere integrate efficacemente.

## Licenza
Space Defenders è rilasciato sotto la licenza MIT. Questo significa che hai ampia libertà di utilizzare, modificare e distribuire il gioco, purché includi il copyright originale e la licenza nel tuo lavoro. La licenza MIT è una delle licenze open source più permissive, progettata per incoraggiare la condivisione e la collaborazione nel software.

Per maggiori dettagli, consulta il file LICENSE nel nostro repository GitHub.

## Supporto
Se incontri problemi durante l'installazione, l'avvio del gioco, o mentre giochi, ti invitiamo a segnalare il problema nella sezione Issues del nostro repository GitHub. Cercheremo di rispondere e risolvere i problemi nel minor tempo possibile.

## Ringraziamenti
- Ringraziamo tutti i contribuenti che hanno dedicato tempo per rendere questo gioco migliore.
- Un grazie speciale va alla community di sviluppatori e giocatori per il supporto e i feedback continui.


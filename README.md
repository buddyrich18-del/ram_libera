# RAM Libera

Blocca in background le app che scegli (Galaxy Tab A7 Lite SM-T220), senza root, tramite Shizuku.

## 1. Crea l'APK
1. Crea un repository su GitHub e carica il contenuto di questa cartella (inclusa `.github`).
2. Tab **Actions** → "Build APK" parte da solo (circa 3–4 min).
3. Apri l'esecuzione → **Artifacts** → scarica `RamLibera-apk` (zip con `app-debug.apk`).

## 2. Prepara il tablet
1. Installa **Shizuku** dal Play Store.
2. Impostazioni → Info tablet → Informazioni software → tocca 7 volte "Numero build".
3. Opzioni sviluppatore → attiva **Debug wireless**.
4. In Shizuku: "Avvia tramite debug wireless" → associa con il codice di accoppiamento → Avvia.
5. Installa `app-debug.apk` (consenti "origini sconosciute").

## 3. Usa l'app
- Apri RAM Libera → **Concedi permesso**.
- Spunta le app da lasciare libere (es. WhatsApp, sveglia, musica).
- **Blocca tutte**: blocca in background tutte le altre app utente.
- Interruttore sulla riga: blocca/sblocca la singola app.
- **Libera RAM**: chiude subito i processi in background.
- **Sblocca tutte**: annulla tutto.

## Cosa fa davvero un blocco
`RUN_ANY_IN_BACKGROUND` e `RUN_IN_BACKGROUND` su `ignore`, standby bucket `restricted`, poi `force-stop`.
L'app bloccata funziona normalmente quando la apri, ma non gira né riceve notifiche in background.

## Note
- I blocchi restano dopo il riavvio. Shizuku invece va riavviato dopo ogni riavvio, ma serve solo per cambiare le impostazioni.
- App di sistema, launcher, tastiera e Shizuku non vengono toccate da "Blocca tutte".
- Bloccare app di sistema singolarmente è possibile ma può causare malfunzionamenti.

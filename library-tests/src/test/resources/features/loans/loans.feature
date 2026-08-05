Feature: Gestione dei prestiti dei libri tramite l'applicazione
  L'amministratore della bibloteca può
  - confermare una richiesta di prestito
  - rifiutare una richiesta di prestito
  - eseguire la restituzione di un libro
  - visualizzare l'elenco di tutti i prestiti
  - visualizzare il dettaglio di tutti i prestiti

  Gli utenti possono
  - inserire una richiesta di prestito per un libro
  - visualizzare l'elenco dei propri prestiti
  - visualizzare il dettaglio dei propri prestiti
  
  Background:
    Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
    Given l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
     """

     """
    And l'amministratore aggiunge 2 copie del libro "9788804336327"
    And l'amministratore aggiunge un libro con isbn "9788415723356", autore "Italo Calvino", titolo "Il visconte dimezzato" e descrizione
     """

     """
    And l'amministratore aggiunge 1 copie del libro "9788415723356"
    And esiste l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678", nome "Mario", cognome "Rossi"
    
  Rule: Creazione di una richiesta di prestito

    Scenario: Creazione di una richiesta di prestito con successo
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente crea una richiesta di prestito con i seguenti dati:
        """
        {
          "isbn": "9788804336327",
          "startDate": "2026-02-23"
        }
        """
      Then la risposta ha status code 201
      And il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "RESERVED", numero tessera "${CARD_NUMBER}"
      
    Scenario: Creazione di una richiesta di prestito per un libro non presente
      Given il catalogo non contiene il libro con isbn "9788804776369"
      And l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente crea una richiesta di prestito con i seguenti dati:
        """
        {
          "isbn": "9788804776369",
          "userId": "${USER_ID}",
          "startDate": "2026-02-23"
        }
        """
      Then la risposta ha status code 201
      And il prestito ha isbn "9788804776369", userId "${USER_ID}", stato "FAILED", numero tessera "${CARD_NUMBER}"
          
    Scenario: Creazione di una richiesta di prestito con dati non validi
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente crea una richiesta di prestito con i seguenti dati:
        """
        {
          "userId": "${USER_ID}",
          "startDate": "2026-02-23"
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR" |
      | type    | "VALIDATION_ERROR" |
      
    Scenario: Creazione di una richiesta di prestito per un libro non disponibile
      Given l'amministratore rimuove una copia del libro "9788415723356"
      And l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente crea una richiesta di prestito con i seguenti dati:
        """
        {
          "isbn": "9788415723356",
          "startDate": "2026-02-23"
        }
        """
      Then la risposta ha status code 201
      And il prestito ha isbn "9788415723356", userId "${USER_ID}", stato "FAILED", numero tessera "${CARD_NUMBER}"
  
    Scenario: L'utente READER può creare prestiti solo per sè stesso
      Given esiste l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente crea una richiesta di prestito con i seguenti dati:
        """
        {
          "isbn": "9788804336327",
          "userId": "${USER_ID}",
          "startDate": "2026-02-23"
        }
        """
      Then la risposta ha status code 403
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_USER" |
      | type    | "LOAN_INVALID_USER" |
  
    Scenario: L'amministratore può creare prestiti per altri utenti
      Given esiste l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678", nome "Mario", cognome "Verdi"
      When l'amministratore crea una richiesta di prestito con i seguenti dati:
        """
        {
          "isbn": "9788804336327",
          "userId": "${USER_ID}",
          "startDate": "2026-02-23"
        }
        """
      Then la risposta ha status code 201
      And il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "RESERVED", numero tessera "${CARD_NUMBER}"
      
     Scenario: L'amministratore non può creare prestiti per un utente non presente
       Given l'utente con ID "123456" non esiste
       When l'amministratore crea una richiesta di prestito con i seguenti dati:
         """
        {
          "isbn": "9788804336327",
          "userId": "123456",
          "startDate": "2026-02-23"
        }
        """
      Then la risposta ha status code 404
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "USER_NOT_FOUND" |
      | type    | "RESOURCE_NOT_FOUND" |
      
  Rule: Conferma della prenotazione di un prestito
  
    Scenario: Conferma di una richiesta di prestito in stato reserved
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'amministratore conferma la richiesta del prestito
      Then la risposta ha status code 204
      And il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "CONFIRMED", numero tessera "${CARD_NUMBER}"
      
    Scenario: Conferma di una richiesta di prestito non esistente
      Given il prestito con ID "100" non esiste
      When l'amministratore conferma la richiesta del prestito
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "LOAN_NOT_CREATED"           |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Conferma di una richiesta di prestito non in stato reserved
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato annullato
      And il prestito è in stato "CANCELED"
      When l'amministratore conferma la richiesta del prestito
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_STATE_TRANSITION"   |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
  Rule: Annullo di una richiesta di prestito
    
    Scenario: Annullo di una richiesta di prestito in stato reserved
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'amministratore annulla la richiesta del prestito
      Then la risposta ha status code 204
      And il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "CANCELED", numero tessera "${CARD_NUMBER}"
      
    Scenario: Annullo di una richiesta di prestito non in stato reserved
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      When l'amministratore annulla la richiesta del prestito
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_STATE_TRANSITION"   |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
            
    Scenario: Annullo di una richiesta di prestito inesistente
      Given il prestito con ID "100" non esiste
      When l'amministratore annulla la richiesta del prestito
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "LOAN_NOT_CREATED"           |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
  Rule: Restituzione di un libro prestato
  
    Scenario: Registrazione del reso di un prestito confermato
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      When l'amministratore esegue l'operazione di reso del prestito con i seguenti dati:
         """
        {
          "returnAt": "2026-03-23"
        }
        """
      Then la risposta ha status code 204
      And il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "RETURNED", numero tessera "${CARD_NUMBER}"
    
    Scenario: Registrazione del reso di un prestito, con dati non validi
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      When l'amministratore esegue l'operazione di reso del prestito con i seguenti dati:
         """
        {
          "returned": "2026-03-23"
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR" |
      | type    | "VALIDATION_ERROR" |
      
    Scenario: Conferma restituzione di un libro prestato, con richiesta in stato non "confirmed"
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'amministratore esegue l'operazione di reso del prestito con i seguenti dati:
         """
        {
          "returnAt": "2026-03-23"
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_STATE_TRANSITION"   |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Conferma restituzione di un libro prestato, con richiesta di prestito non esistente
      Given il prestito con ID "100" non esiste
      When l'amministratore esegue l'operazione di reso del prestito con i seguenti dati:
         """
        {
          "returnAt": "2026-03-23"
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "LOAN_NOT_CREATED"           |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
  Rule: Consultazione delle richieste di prestito
  
    Scenario: Consultazione elenco prestiti con nessun prestito presente
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente visualizza l'elenco dei prestiti
      Then la risposta ha status code 200
      And la risposta contiene il campo "loans"
      And eventualmente "loans" è una lista vuota
      
    Scenario: Consultazione elenco prestiti con prestiti presenti, senza applicare filtro di ricerca
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      When l'utente visualizza l'elenco dei prestiti
      Then la risposta ha status code 200
      And la risposta contiene il campo "loans"
      And eventualmente "loans" contiene 1 elementi
      And eventualmente "loans" ha un elemento con i campi:
        | id         | ${LOAN_ID}       |
        | isbn       | "9788804336327"  |
        | status  	 | "CONFIRMED"      |
        | userId     | ${USER_ID}       |
        | cardNumber | ${CARD_NUMBER}   | 
        
    Scenario: Consultazione elenco prestiti, filtrato per ISBN non presente
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'utente visualizza l'elenco dei prestiti, con filtro di ricerca
      | isbn | "9788804336322" |
      Then la risposta ha status code 200
      And la risposta contiene il campo "loans"
      And eventualmente "loans" è una lista vuota
      
    Scenario: Consultazione elenco prestiti, filtrato per ISBN presente
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788415723356", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'utente visualizza l'elenco dei prestiti, con filtro di ricerca
      | isbn   | "9788804336327"    |
      Then la risposta ha status code 200
      And la risposta contiene il campo "loans"
      And eventualmente "loans" contiene 1 elementi
      And eventualmente "loans" ha un elemento con i campi:
        | id         | ${LOAN_ID}      |
        | isbn       | "9788804336327" |
        | status  	 | "RESERVED"      |
        | userId     | ${USER_ID}      |
            
    Scenario: Consultazione elenco prestiti, filtrato per numero tessera
      Given esiste l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And esiste l'utente con credenziali "mario.bianchi@mail.it", "MarioBianchi12345678", nome "Mario", cognome "Bianchi"
      And l'utente con credenziali "mario.bianchi@mail.it", "MarioBianchi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788415723356", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'amministratore visualizza l'elenco dei prestiti, con filtro di ricerca
      | cardNumber   | ${CARD_NUMBER}  |
      Then la risposta ha status code 200
      And la risposta contiene il campo "loans"
      And eventualmente "loans" contiene 1 elementi
      And eventualmente "loans" ha un elemento con i campi:
        | id         | ${LOAN_ID}       |
        | isbn       | "9788415723356"  |
        | status  	 | "RESERVED"       |
        | userId     | ${USER_ID}       |
               
    Scenario: Consultazione elenco prestiti, un utente può vedere solo i suoi prestiti
      Given esiste l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente visualizza l'elenco dei prestiti
      Then la risposta ha status code 200
      And la risposta contiene il campo "loans"
      And eventualmente "loans" è una lista vuota
      
  Rule: Visualizzazione dettaglio prestito
  
    Scenario: Dettaglio di un prestito esistente
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'utente visualizza il dettaglio del prestito
      Then la risposta ha status code 200
      And il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "RESERVED", numero tessera "${CARD_NUMBER}"
      
     Scenario: Dettaglio di un prestito non presente nel catalogo
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And il prestito con ID "100" non esiste
      When l'utente visualizza il dettaglio del prestito
      Then la risposta ha status code 404
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "LOAN_NOT_FOUND"    |
      | type    | "APPLICATION_ERROR" |
      
     Scenario: Un utente READER può vedere solo il dettaglio dei suoi prestiti
      Given esiste l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@mail.it", "MarioVerdi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      When l'utente visualizza il dettaglio del prestito
      Then la risposta ha status code 404
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "LOAN_NOT_FOUND"    |
      | type    | "APPLICATION_ERROR" |
      
     Scenario: L'amministratore può vedere il dettaglio di tutti i prestiti
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'amministratore visualizza il dettaglio del prestito
      Then la risposta ha status code 200
      And il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "RESERVED", numero tessera "${CARD_NUMBER}"
      
  Rule: Completamento del processo di prestito
  
    Scenario: Dopo la conferma, il prestito raggiunge lo stato finale "confirmed" e la disponibilità del libro è aggiornata
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      When l'amministratore conferma la richiesta del prestito
      Then il prestito ha isbn "9788804336327", userId "${USER_ID}", stato "CONFIRMED", numero tessera "${CARD_NUMBER}"
      And il libro "9788804336327" ha totalCopies = 2, borrowedCopies = 1, reservedCopies = 0
      
  Rule: Dashboard amministrativa
    
    Scenario: Visualizzazione prestiti scaduti, con nessun prestito scaduto
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And esiste un prestito dell'utente per il libro ISBN "9788415723356", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      And il prestito del libro è stato reso in data "2026-03-20"
      And il prestito è in stato "RETURNED"
      When l'amministratore visualizza l'elenco dei prestiti scaduti
      Then la risposta ha status code 200
      And la risposta contiene 0 elementi
      
    Scenario: Visualizzazione prestiti scaduti quando esistono prestiti scaduti
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And esiste un prestito dell'utente per il libro ISBN "9788415723356", con data inizio "2026-02-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      When l'amministratore visualizza l'elenco dei prestiti scaduti
      Then la risposta ha status code 200
      And la risposta contiene 1 elementi
      And la risposta contiene un elemento con i campi:
      | loanId      | ${LOAN_ID}      |
      | isbn        | "9788415723356" |
      | dueDate     | "2026-03-25"    |
      
    Scenario: Visualizzazione statistiche utenti-prestiti
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-06-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      And esiste un prestito dell'utente per il libro ISBN "9788415723356", con data inizio "2026-04-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      And il prestito del libro è stato reso in data "2026-05-30"
      And il prestito è in stato "RETURNED"
      When l'amministratore visualizza la dashboard statistiche utenti-prestiti
      Then la risposta ha status code 200
      And la risposta contiene 1 elementi
      And la risposta contiene un elemento con i campi:
      | userId                  | ${USER_ID}   |
      | overdueLoansCount       | 1            |
      | activeOverdueLoansCount | 1            |
      | lastOverdueDate         | "2026-05-30" |
      
    Scenario: Statistiche giornaliere dei prestiti
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-06-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      And esiste un prestito dell'utente per il libro ISBN "9788415723356", con data inizio "2026-04-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      And il prestito del libro è stato reso in data "2026-05-30"
      And il prestito è in stato "RETURNED"
      When l'amministratore visualizza le statistiche giornaliere dei prestiti degli ultimi 30 giorni
      Then la risposta ha status code 200
      And la risposta contiene 1 elementi
      And la risposta contiene un elemento con i campi:
      | loansCreated    | 2            |
      | loansConfirmed  | 2            |
      | loansCanceled   | 0            |
      | loansReturned   | 1            |
      
    Scenario: Statistiche giornaliere dei prestiti con intervallo date non valido
      When l'amministratore visualizza le statistiche giornaliere dei prestiti, per il periodo "2026-07-30"-"2026-07-28"
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_DATE_RANGE" |
      | type    | "VALIDATION_ERROR" |
      
    Scenario: Visualizzazione libri con più prestiti
      Given l'utente con credenziali "mario.rossi@mail.it", "MarioRossi12345678" è autenticato
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-06-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      And esiste un prestito dell'utente per il libro ISBN "9788804336327", con data inizio "2026-07-23", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      And esiste un prestito dell'utente per il libro ISBN "9788415723356", con data inizio "2026-07-25", in attesa di conferma
      And il prestito è in stato "RESERVED"
      And il prestito del libro è stato confermato
      And il prestito è in stato "CONFIRMED"
      When l'amministratore visualizza l'elenco dei libri con più prestiti
      Then la risposta ha status code 200
      And la risposta contiene 2 elementi
      And la risposta contiene un elemento con i campi:
      | isbn      | "9788804336327"       |
      | author    | "Italo Calvino"       |
      | title     | "Il barone rampante"  |
      | loanCount | 2                     |
      And la risposta contiene un elemento con i campi:
      | isbn      | "9788415723356"          |
      | author    | "Italo Calvino"          |
      | title     | "Il visconte dimezzato"  |
      | loanCount | 1                        |
      
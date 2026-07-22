Feature: Gestione degli utenti
L'amministratore della bibloteca può
  - sospendere un utente
  - riattivare un utente sospeso
  - visualizzare l'elenco degli utenti
  - visualizzare il profilo degli utenti

  Gli utenti possono
  - registrarsi
  - cancellare la propria registrazione
  - visualizzare il proprio profilo
  
  Background:
    Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
    
  Rule: Registrazione di un utente
  
    Scenario: Registrazione di un utente con successo
      Given l'utente "mario.rossi@gmail.com" non è registrato
      When l'utente si registra con i seguenti dati:
        """
        {
          "name": "Mario",
          "lastname": "Rossi",
          "email": "mario.rossi@gmail.com",
          "password": "MarioRossi12345678"
        }
        """
      Then la risposta ha status code 200
      And l'utente ha email "mario.rossi@gmail.com", ruolo "READER", stato "ACTIVE", numero tessera "-"
      
    Scenario: Registrazione di un utente con mail già registrata
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
      When l'utente si registra con i seguenti dati:
        """
        {
          "name": "Mario",
          "lastname": "Rossi",
          "email": "mario.rossi@gmail.com",
          "password": "MarioRossi12345678"
        }
        """
      Then la risposta ha status code 409
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "USER_ALREADY_EXISTS" |
      | type    | "CONFLICT"            |
      
    Scenario: Registrazione di un utente con dati non validi
      When l'utente si registra con i seguenti dati:
        """
        {
          "email": "mario.rossi@gmail.com"
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR" |
      | type    | "VALIDATION_ERROR" |
      
  Rule: Disiscrizione di un utente
  
    Scenario: Disiscrizione di un utente con successo  
	  Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
	  And l'utente ha stato "ACTIVE"
	  And l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678" è autenticato
	  When l'utente si disiscrive con i seguenti dati:
	    """
        {
          "reason": "Unsubscribed"
        }
        """
	  Then la risposta ha status code 200
	  And l'utente ha email "mario.rossi@gmail.com", ruolo "READER", stato "DISABLED", numero tessera "${CARD_NUMBER}"
	        
  Rule: Sospensione di un utente
    
    Scenario: Sospensione di un utente con successo
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
	  And l'utente ha stato "ACTIVE"
	  When l'amministratore sospende l'utente:
	     """
        {
          "userId": "${USER_ID}",
          "reason": "Suspended"
        }
        """
      Then la risposta ha status code 200
	  And l'utente ha email "mario.rossi@gmail.com", ruolo "READER", stato "SUSPENDED", numero tessera "${CARD_NUMBER}"
	  
	Scenario: Sospensione di un utente con dati non validi
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
	  And l'utente ha stato "ACTIVE"
	  When l'amministratore sospende l'utente:
	     """
        {
          "reason": "Suspended"
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR" |
      | type    | "VALIDATION_ERROR" |
      
    Scenario: Sospensione di un utente non registrato
      Given l'utente con ID "100" non esiste
	  When l'amministratore sospende l'utente:
	     """
        {
          "userId": "100",
          "reason": "Suspended"
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "USER_NOT_CREATED"           |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
  Rule: Riattivazione di un utente sospeso
  
    Scenario: Riattivazione di un utente con successo
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
	  And l'utente ha stato "ACTIVE"
	  And l'amministratore sospende l'utente
	  And l'utente ha stato "SUSPENDED"
	  When l'amministratore riattiva l'utente:
	    """
        {
          "userId": "${USER_ID}",
          "reason": "Unsuspend"
        }
        """
     Then la risposta ha status code 200
	 And l'utente ha email "mario.rossi@gmail.com", ruolo "READER", stato "ACTIVE", numero tessera "${CARD_NUMBER}"
	 
	Scenario: Riattivazione di un utente con dati non validi
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
	  And l'utente ha stato "ACTIVE"
	  When l'amministratore riattiva l'utente:
	     """
        {
          "reason": "Unsuspend"
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR" |
      | type    | "VALIDATION_ERROR" |
      
    Scenario: Riattivazione di un utente non registrato
      Given l'utente con ID "100" non esiste
	  When l'amministratore riattiva l'utente:
	     """
        {
          "userId": "100",
          "reason": "Unsuspend"
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "USER_NOT_CREATED"           |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
  Rule: visualizzazione profilo utente
  
    Scenario: Un utente READER può visualizzare il suo profilo
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
      And l'utente ha stato "ACTIVE"
      And l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678" è autenticato
      When l'utente visualizza il suo profilo
      Then la risposta ha status code 200
      And l'utente ha email "mario.rossi@gmail.com", ruolo "READER", stato "ACTIVE", numero tessera "${CARD_NUMBER}"
     
    Scenario: L'amministratore può vedere il profilo di tutti gli utenti
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
      And l'utente ha stato "ACTIVE"
      When l'amministratore visualizza il profilo di "mario.rossi@gmail.com"
      Then la risposta ha status code 200
      And l'utente ha email "mario.rossi@gmail.com", ruolo "READER", stato "ACTIVE", numero tessera "${CARD_NUMBER}"
      
     Scenario: Visualizzazione del profilo di un utente non registrato
      Given l'utente con ID "100" non esiste
      When l'amministratore visualizza il profilo dell'utente
      Then la risposta ha status code 404
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "USER_NOT_FOUND"     |
      | type    | "RESOURCE_NOT_FOUND" |
      
  Rule: Visualizzazione elenco utenti
  
    Scenario: L'amministratore può visualizzare l'elenco degli utenti
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
      And l'utente ha stato "ACTIVE"
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "MarioVerdi12345678"
      And l'utente ha stato "ACTIVE"
      And l'amministratore sospende l'utente
	  And l'utente ha stato "SUSPENDED"
	  When l'amministratore visualizza l'elenco degli utenti
	  Then la risposta ha status code 200
	  And la risposta contiene il campo "users"
	  And eventualmente "users" ha un elemento con i campi:
        | email  | "mario.rossi@gmail.com" |
        | role   | "READER"                |
        | status | "ACTIVE"                |
      And eventualmente "users" ha un elemento con i campi:
        | email  | "mario.verdi@gmail.com" |
        | role   | "READER"                |
        | status | "SUSPENDED"             |
  
    Scenario: Consultazione elenco utenti, filtrato per mail non presente
      Given l'utente "mario.rossi@gmail.com" non è registrato
      When l'amministratore visualizza l'elenco degli utenti, con filtro di ricerca
      | email | "mario.verdi@gmail.com" |
      Then la risposta ha status code 200
      And la risposta contiene il campo "users"
      And eventualmente "users" contiene 1 elementi
      And eventualmente "users" ha un elemento con i campi:
        | email  | "admin@gmail.com" |
        | role   | "ADMIN"           |
        | status | "ACTIVE"          |
    
    Scenario: Consultazione elenco utenti, filtrato per stato presente
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
      And l'utente ha stato "ACTIVE"
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "MarioVerdi12345678"
      And l'utente ha stato "ACTIVE"
      And l'amministratore sospende l'utente
	  And l'utente ha stato "SUSPENDED"
	  When l'amministratore visualizza l'elenco degli utenti, con filtro di ricerca
	  | status | "SUSPENDED" |
	  Then la risposta ha status code 200
	  And la risposta contiene il campo "users"
	  And eventualmente "users" contiene 1 elementi
      And eventualmente "users" ha un elemento con i campi:
        | email  | "mario.verdi@gmail.com" |
        | role   | "READER"                |
        | status | "SUSPENDED"             |
        
    Scenario: Consultazione elenco utenti, filtrato per numero tessera
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
      And l'utente ha stato "ACTIVE"
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "MarioVerdi12345678"
      And l'utente ha stato "ACTIVE"
      And l'amministratore sospende l'utente
	  And l'utente ha stato "SUSPENDED"
	  When l'amministratore visualizza l'elenco degli utenti, con filtro di ricerca
	  | cardNumber | ${CARD_NUMBER} |
	  Then la risposta ha status code 200
	  And la risposta contiene il campo "users"
	  And eventualmente "users" contiene 1 elementi
      And eventualmente "users" ha un elemento con i campi:
        | email      | "mario.verdi@gmail.com" |
        | role       | "READER"                |
        | status     | "SUSPENDED"             |
        | cardNumber | ${CARD_NUMBER}          |
        
    Scenario: Un utente READER non può visualizzare l'elenco degli utenti
      Given esiste l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678"
      And l'utente ha stato "ACTIVE"
      And l'utente con credenziali "mario.rossi@gmail.com", "MarioRossi12345678" è autenticato
      When l'utente visualizza l'elenco degli utenti
      Then la risposta ha status code 403
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "ACCESS_DENIED"     |
      | type    | "SECURITY" |
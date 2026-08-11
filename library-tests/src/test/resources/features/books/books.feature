Feature: Gestione del catalogo della biblioteca tramite l'applicazione
  L'amministratore della bibloteca può
  - aggiungere nuovi libri
  - aggiungere/rimuovere copie dei libri
  - visualizzare i libri presenti nel catalogo
  - visualizzare il dettaglio dei libri

  Gli utenti possono
  - visualizzare i libri presenti nel catalogo
  - visualizzare il dettaglio dei libri
  
  Rule: Inserimento di un nuovo libro nel catalogo

    Scenario: Aggiunta di un nuovo libro con successo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      When l'amministratore aggiunge un libro al catalogo con i seguenti dati:
        """
        {
          "isbn": "9788804336327",
          "author": "Italo Calvino",
          "title": "Il barone rampante"
        }
        """
      Then la risposta ha status code 201
      And la risposta contiene il campo "isbn"
	  And il libro "9788804336327" ha totalCopies = 0, borrowedCopies = 0, reservedCopies = 0
	  
	Scenario: Aggiunta di un libro con richiesta non conforme al contratto API
	  Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      When l'amministratore aggiunge un libro al catalogo con i seguenti dati:
        """
        {
          "isbn": "9788804336327",
          "autore": "Italo Calvino",
          "title": "Il barone rampante"
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR"     |
      | type    | "VALIDATION_ERROR"     |
      
     Scenario: Aggiunta di un libro con dati non validi secondo le regole di dominio
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      When l'amministratore aggiunge un libro al catalogo con i seguenti dati:
        """
        {
          "isbn": "123"
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR" |
      | type    | "VALIDATION_ERROR" |
      
     Scenario: Aggiunta di un libro già presente nel catalogo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804772729", autore "Italo Calvino", titolo "Il visconte dimezzato" e descrizione
        """
        Il visconte dimezzato (1952) è il primo libro della trilogia I nostri antenati
        """
      When l'amministratore aggiunge un libro al catalogo con i seguenti dati:
        """
        {
          "isbn": "9788804772729",
          "author": "Italo Calvino",
          "title": "Il visconte dimezzato"
        }
        """
      Then la risposta ha status code 409
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_ALREADY_EXISTS" |
      | type    | "CONFLICT"            |
      
  Rule: Consultazione del catalogo libri
  
    Scenario: Consultazione del catalogo vuoto
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And eventualmente "books" è una lista vuota
      
        
    Scenario: Consultazione del catalogo con libri presenti
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And l'amministratore aggiunge un libro con isbn "978-8804776369", autore "Italo Calvino", titolo "Il visconte dimezzato" e descrizione
        """

        """
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"  
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And eventualmente "books" contiene 2 elementi
      And eventualmente "books" ha un elemento con i campi:                                                         
        | isbn        | "9788804336327"                                                                 |
        | author      | "Italo Calvino"                                                                 |
        | title       | "Il barone rampante"                                                            |
        | description | "Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati" |
      And eventualmente "books" ha un elemento con i campi:
        | isbn        | "9788804776369"         |
        | author      | "Italo Calvino"         |
        | title       | "Il visconte dimezzato" |
        | description | EMPTY                   |
        
    Scenario: Consultazione del catalogo filtrata per autore non presente
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri, con filtro di ricerca
        | author | "Shakespeare" |
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And eventualmente "books" è una lista vuota
      
    Scenario: Consultazione del catalogo filtrata per autore presente
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri, con filtro di ricerca
        | author | Italo Calvino |
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And eventualmente "books" contiene 1 elementi
      And eventualmente "books" ha un elemento con i campi:
        | isbn        | "9788804336327"                                                                 |
        | author      | "Italo Calvino"                                                                 |
        | title       | "Il barone rampante"                                                            |
        | description | "Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati" |
        
    Scenario: Consultazione del catalogo filtrata per autore e solo disponibili
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      And l'amministratore aggiunge 2 copie del libro "9788804336327"
      And l'amministratore aggiunge un libro con isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato" e descrizione
        """

        """
      And l'amministratore aggiunge 1 copie del libro "9788804776369"
      And l'amministratore rimuove una copia del libro "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri, con filtro di ricerca
        | author        | Italo Calvino |
        | onlyAvailable | true          |
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And eventualmente "books" contiene 1 elementi
      And eventualmente "books" ha un elemento con i campi:
        | isbn        | "9788804336327"         |
        | author      | "Italo Calvino"         |
        | title       | "Il barone rampante"    |
        | description | EMPTY                   |
    
  Rule: Gestione delle copie di un libro
  
    Scenario: Aggiunta di una copia di un libro con successo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      When l'amministratore aggiunge una copia del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": 1
        }
        """
      Then la risposta ha status code 204
      And il libro "9788804336327" ha totalCopies = 1, borrowedCopies = 0, reservedCopies = 0
      
    Scenario: Aggiunta di una copia di un libro con quantità negativa
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      When l'amministratore aggiunge una copia del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": -1
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_BOOK_COPY_QUANTITY" |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Aggiunta di una copia di un libro non presente
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      When l'amministratore aggiunge una copia del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": 1
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_NOT_REGISTERED"        |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Rimozione di 2 copie di un libro con successo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      And l'amministratore aggiunge 2 copie del libro "9788804336327"
      When l'amministratore rimuove copie del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": 2,
          "reason": "Copies lost"
        }
        """
      Then la risposta ha status code 204
      And il libro "9788804336327" ha totalCopies = 0, borrowedCopies = 0, reservedCopies = 0
      
    Scenario: Rimozione di una copia di un libro non presente
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      When l'amministratore rimuove copie del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": 2,
          "reason": "Copies lost"
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_NOT_REGISTERED"        |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Rimozione di una copia di un libro con quantità non valida
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      When l'amministratore rimuove copie del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": -1
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_BOOK_COPY_QUANTITY" |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Rimozione di una copia di libro non disponibile
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      And l'amministratore aggiunge 2 copie del libro "9788804336327"
      When l'amministratore rimuove copie del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": 3,
          "reason": "Copies lost"
        }
        """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "CANNOT_REMOVE_BOOK_COPIES"  |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
  
  Rule: Visualizzazione dettaglio libro
    
    Scenario: Dettaglio di in libro presente nel catalogo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      And l'amministratore aggiunge 3 copie del libro "9788804336327"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il dettaglio del libro isbn "9788804336327"
      Then la risposta ha status code 200
      And eventualmente la risposta contiene i seguenti campi:
      | isbn           | "9788804336327"         |
      | author         | "Italo Calvino"         |
      | title          | "Il barone rampante"    |
      | description    | ""                      |
      | totalCopies    | 3                       |
      | borrowedCopies | 0                       |
      | reservedCopies | 0                       |
      | available      | true                    |
      
    Scenario: Dettaglio di un libro non presente nel catalogo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il dettaglio del libro isbn "9788804336327"
      Then la risposta ha status code 404
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_NOT_FOUND"    |
      | type    | "APPLICATION_ERROR" |
      
  Rule: Sottoscrizione per libri non disponibili
  
  	Scenario: L'utente riceve una notifica quando il libro torna disponibile
  	  Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And l'utente si sottoscrive alla disponibilità del libro ISBN "9788804336327"
      When l'amministratore aggiunge una copia del libro "9788804336327", con i seguenti dati:
        """
        {
          "quantity": 1
        }
        """
      Then la risposta ha status code 204
      And il libro "9788804336327" ha totalCopies = 1, borrowedCopies = 0, reservedCopies = 0
      And eventualmente la sottoscrizione con ISBN "9788804336327" risulta notificata
      
  Rule: Creazione richiesta libri
  
    Scenario: Inserimento di una richiesta con successo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente crea una richiesta per un libro, con i seguenti dati:
     	"""
        {
          "isbn": "9788804776369",
          "author": "Italo Calvino",
          "title": "Il visconte dimezzato",
          "notes": ""
        }
        """
      Then la risposta ha status code 201
      And la richiesta ha ISBN "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato", stato "PENDING", voti 1
    
    Scenario: Inserimento di una richiesta con dati non validi
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente crea una richiesta per un libro, con i seguenti dati:
     	"""
        {
          "isbn": "",
          "author": "",
          "title": "Il barone rampante",
          "notes": ""
        }
        """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR" |
      | type    | "VALIDATION_ERROR" |
      
    Scenario: Inserimento di una richiesta per un libro già presente nel catalogo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente crea una richiesta per un libro, con i seguenti dati:
     	"""
        {
          "isbn": "",
          "author": "Italo Calvino",
          "title": "Il barone rampante",
          "notes": ""
        }
        """
      Then la risposta ha status code 409
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_ALREADY_REGISTERED"    |
      | type    | "CONFLICT" |
      
    Scenario: Inserimento di una richiesta per un libro con richiesta già presente
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato 
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      When l'utente crea una richiesta per un libro, con i seguenti dati:
     	"""
        {
          "isbn": "",
          "author": "Italo Calvino",
          "title": "Il visconte dimezzato",
          "notes": ""
        }
        """
      Then la risposta ha status code 409
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_ALREADY_EXIST" |
      | type    | "CONFLICT"                   | 
     
  Rule: Visualizzazione elenco richieste libri
  
    Scenario: Richieste libri non presenti
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato 
      When l'utente visualizza le richieste libri
      Then la risposta ha status code 200
      And la risposta contiene 0 elementi
      
    Scenario: Richieste libri presenti
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And il catalogo non contiene il libro con isbn "9788804336327"
      And esiste una richiesta per il libro isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante"
      And la richiesta è in stato "PENDING"
      When l'utente visualizza le richieste libri
      Then la risposta ha status code 200
      And la risposta contiene 2 elementi
      And la risposta contiene un elemento con i campi:
      | author  | "Italo Calvino"         |
      | title   | "Il visconte dimezzato" |
      | isbn    | "9788804776369"         |
      | status  | "PENDING"               |
      | votes   | 1                       |
      And la risposta contiene un elemento con i campi:
      | author  | "Italo Calvino"         |
      | title   | "Il barone rampante"    |
      | isbn    | "9788804336327"         |
      | status  | "PENDING"               |
      | votes   | 1                       |
      
  Rule: Approvazione di una richiesta
  
    Scenario: Approvazione di una richiesta con successo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      When l'amministratore approva la richiesta
      Then la risposta ha status code 204
      And la richiesta ha ISBN "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato", stato "APPROVED", voti 1
      
    Scenario: Approvazione di una richiesta in stato non pending
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And la richiesta è stata rigettata
      And la richiesta è in stato "REJECTED"
      When l'amministratore approva la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_REQUEST_STATE_TRANSITION" |
      | type    | "AGGREGATE_INVARIANT_FAILED" | 
    
    Scenario: Approvazione di una richiesta che non esiste
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And la richiesta con ID "BR-123" non esiste
      When l'amministratore approva la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_NOT_EXIST"     |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
  Rule: Rigetto di una richiesta  
  
    Scenario: Rigetto di una richiesta con successo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      When l'amministratore rigetta la richiesta
      Then la risposta ha status code 204
      And la richiesta ha ISBN "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato", stato "REJECTED", voti 1
      
    Scenario: Rigetto di una richiesta in stato non pending
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And la richiesta è stata approvata
      And la richiesta è in stato "APPROVED"
      When l'amministratore rigetta la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "INVALID_REQUEST_STATE_TRANSITION"   |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Rigetto di una richiesta che non esiste
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And la richiesta con ID "BR-123" non esiste
      When l'amministratore rigetta la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_NOT_EXIST"     |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
   Rule: Votazione di una richiesta
   
    Scenario: Votazione di una richiesta con successo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "Test12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@gmail.com", "Test12345678" è autenticato
      When l'utente vota la richiesta
      Then la risposta ha status code 204
      And la richiesta ha ISBN "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato", stato "PENDING", voti 2
      
    Scenario: Votazione di una richiesta che non esiste
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And la richiesta con ID "BR-123" non esiste
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente vota la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_NOT_EXIST"     |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
     Scenario: Votazione di una richiesta non in stato pending
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And la richiesta è stata approvata
      And la richiesta è in stato "APPROVED"
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "Test12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@gmail.com", "Test12345678" è autenticato
      When l'utente vota la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_ALREADY_CLOSED"  |
      | type    | "AGGREGATE_INVARIANT_FAILED"   |
      
    Scenario: Un utente vota la richiesta che ha creato
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      When l'utente vota la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "USER_REQUESTER_CANNOT_VOTE"  |
      | type    | "AGGREGATE_INVARIANT_FAILED"  |
      
    Scenario: Un utente vota 2 volte la stessa richiesta
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "Test12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@gmail.com", "Test12345678" è autenticato
      And esiste un voto dell'utente per la richiesta
      When l'utente vota la richiesta
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "USER_ALREADY_VOTED"  		   |
      | type    | "AGGREGATE_INVARIANT_FAILED"     |
      
  Rule: Dettaglio di una richiesta
  
    Scenario: Dettaglio di una richiesta esistente
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "Test12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@gmail.com", "Test12345678" è autenticato
      And esiste un voto dell'utente per la richiesta
      When l'utente visualizza il dettaglio della richiesta
      Then la risposta ha status code 200
      And eventualmente la risposta contiene i seguenti campi:
      | author  | "Italo Calvino"         |
      | title   | "Il visconte dimezzato" |
      | isbn    | "9788804776369"         |
      | status  | "PENDING"               |
      | votes   | 2                       |
      And la lista dei voti della richiesta contiene 2 elementi
      
    Scenario: Dettaglio di una richiesta che non esiste
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And la richiesta con ID "BR-123" non esiste
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il dettaglio della richiesta
      Then la risposta ha status code 404
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_NOT_FOUND" |
      | type    | "APPLICATION_ERROR"      |
    
  Rule: Aggiornamento prezzo stimato libro richiesto
  
    Scenario: Aggiornamento con successo del prezzo stimato
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      When l'amministratore aggiorna il prezzo del libro richiesto con i seguenti dati:
      """
        {
          "estimatedPrice": 12.82
        }
      """
      Then la risposta ha status code 204
      And la richiesta ha ISBN "9788804776369", prezzo stimato "12.82"
      
    Scenario: Aggiornamento del prezzo stimato con richiesta non conforme al contratto API
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      When l'amministratore aggiorna il prezzo del libro richiesto con i seguenti dati:
      """
        {
          "price": 12.82
        }
      """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR"     |
      | type    | "VALIDATION_ERROR"     |
      
    Scenario: Aggiornamento del prezzo stimato con dati non validi
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      When l'amministratore aggiorna il prezzo del libro richiesto con i seguenti dati:
      """
        {
          "estimatedPrice": null
        }
      """
      Then la risposta ha status code 400
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "VALIDATION_ERROR"     |
      | type    | "VALIDATION_ERROR"     |
      
    Scenario: Aggiornamento del prezzo stimato per una richiesta che non esiste
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And la richiesta con ID "BR-123" non esiste
      When l'amministratore aggiorna il prezzo del libro richiesto con i seguenti dati:
      """
        {
          "estimatedPrice": 12.82
        }
      """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_NOT_EXIST"     |
      | type    | "AGGREGATE_INVARIANT_FAILED" |
      
    Scenario: Aggiornamento del prezzo stimato per una richiesta non in stato pending
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And la richiesta è stata approvata
      And la richiesta è in stato "APPROVED"
      When l'amministratore aggiorna il prezzo del libro richiesto con i seguenti dati:
      """
        {
          "estimatedPrice": 12.82
        }
      """
      Then la risposta ha status code 422
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_REQUEST_ALREADY_CLOSED"  |
      | type    | "AGGREGATE_INVARIANT_FAILED"   |
      
  Rule: Visualizzazione proposta di acquisto libri
  
    Scenario: Non ci sono richieste libri
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      When l'amministratore visuallizza le proposte di acquisto con budget "100.00" euro
      Then la risposta ha status code 200
      And la risposta contiene i seguenti campi:
      | budget  	| 100  |
      | totalCost   | 0    |
      | totalScore  | 0    |
      | status      | "NO_PENDING_REQUESTS" |
      And la lista dei libri suggeriti contiene 0 elementi
      
    Scenario: Non ci sono richieste libri in stato pending e con prezzo settato
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And il catalogo non contiene il libro con isbn "9788804336327"
      And esiste una richiesta per il libro isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante"
      And la richiesta è in stato "PENDING"
      And la richiesta è stata approvata
      And la richiesta è in stato "APPROVED"
      When l'amministratore visuallizza le proposte di acquisto con budget "100.00" euro
      Then la risposta ha status code 200
      And la risposta contiene i seguenti campi:
      | budget  	| 100                      |
      | totalCost   | 0                        |
      | totalScore  | 0                        |
      | status      | "NO_REQUESTS_WITH_PRICE" |
      And la lista dei libri suggeriti contiene 0 elementi
      
    Scenario: Le richieste in stato pending hanno prezzo maggiore del budget previsto
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And la richiesta ha prezzo stimato "20.90" euro
      And il catalogo non contiene il libro con isbn "9788804336327"
      And esiste una richiesta per il libro isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante"
      And la richiesta è in stato "PENDING"
      And la richiesta ha prezzo stimato "22.50" euro
      When l'amministratore visuallizza le proposte di acquisto con budget "20.00" euro
      Then la risposta ha status code 200
      And la risposta contiene i seguenti campi:
      | budget  	| 20               |
      | totalCost   | 0                |
      | totalScore  | 0    			   |
      | status      | "BUDGET_TOO_LOW" |
      And la lista dei libri suggeriti contiene 0 elementi
      
    Scenario: Le richieste in stato pending soddisfano il budget a disposizione
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And il catalogo non contiene il libro con isbn "9788804776369"
      And esiste l'utente con credenziali "reader@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      And esiste una richiesta per il libro isbn "9788804776369", autore "Italo Calvino", titolo "Il visconte dimezzato"
      And la richiesta è in stato "PENDING"
      And la richiesta ha prezzo stimato "20.90" euro
      And il catalogo non contiene il libro con isbn "9788804336327"
      And esiste una richiesta per il libro isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante"
      And la richiesta è in stato "PENDING"
      And la richiesta ha prezzo stimato "22.50" euro
      And esiste l'utente con credenziali "mario.verdi@gmail.com", "Test12345678", nome "Mario", cognome "Verdi"
      And l'utente con credenziali "mario.verdi@gmail.com", "Test12345678" è autenticato
      And esiste un voto dell'utente per la richiesta
      And il catalogo non contiene il libro con isbn "9780439139595"
      And esiste una richiesta per il libro isbn "9780439139595", autore "J. K. Rowling", titolo "Harry Potter e il calice di fuoco"
      And la richiesta è in stato "PENDING"
      And la richiesta ha prezzo stimato "25.80" euro
      And esiste l'utente con credenziali "mario.rossi@gmail.com", "Test12345678", nome "Mario", cognome "Rossi"
      And l'utente con credenziali "mario.rossi@gmail.com", "Test12345678" è autenticato
      And esiste un voto dell'utente per la richiesta
      When l'amministratore visuallizza le proposte di acquisto con budget "50.00" euro
      Then la risposta ha status code 200
      And la risposta contiene i seguenti campi:
      | budget  	| 50        |
      | totalCost   | 48.30     |
      | totalScore  | 8         |
      | status      | "SUCCESS" |
      And la lista dei libri suggeriti contiene 2 elementi
      And la lista dei libri suggeriti ha un elemento con i campi:
      | title  	       | "Il barone rampante" |
      | author         | "Italo Calvino"      |
      | estimatedPrice | 22.50                |
      | votes          | 2                    |
      | score          | 4                    |
      And la lista dei libri suggeriti ha un elemento con i campi:
      | title  	       | "Harry Potter e il calice di fuoco" |
      | author         | "J. K. Rowling"                     |
      | estimatedPrice | 25.80                               |
      | votes          | 2                                   |
      | score          | 4                                   |
    
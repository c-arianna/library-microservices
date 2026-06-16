Feature: Gestione del catalogo della biblioteca tramite l'applicazione
  L'amministratore della bibloteca può
  - aggiungere nuovi libri
  - visualizzare i libri presenti nel catalogo

  Gli utenti possono
  - visualizzare i libri presenti nel catalogo
   
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
      | code    | "VALIDATION_ERROR"     |
      | type    | "VALIDATION_ERROR" |
      
     Scenario: Aggiunta di un libro già presente nel catalogo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      Given l'amministratore aggiunge un libro con isbn "9788804772729", autore "Italo Calvino", titolo "Il visconte dimezzato" e descrizione
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
      Given l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And "books" è una lista vuota
      
        
    Scenario: Consultazione del catalogo con libri presenti
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And l'amministratore aggiunge un libro con isbn "978-8804776369", autore "Italo Calvino", titolo "Il visconte dimezzato" e descrizione
        """

        """
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And "books" contiene 2 elementi
      And "books" ha un elemento con i campi:                                                         
        | isbn        | "9788804336327"                                                                 |
        | author      | "Italo Calvino"                                                                 |
        | title       | "Il barone rampante"                                                            |
        | description | "Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati" |
      And "books" ha un elemento con i campi:
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
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri, con filtro di ricerca
        | author | "Shakespeare" |
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And "books" è una lista vuota
      
    Scenario: Consultazione del catalogo filtrata per autore presente
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """
        Il barone rampante (1957) è il secondo libro della trilogia I nostri antenati
        """
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri, con filtro di ricerca
        | author | Italo Calvino |
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And "books" contiene 1 elementi
      And "books" ha un elemento con i campi:
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
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il catalogo dei libri, con filtro di ricerca
        | author        | Italo Calvino |
        | onlyAvailable | true          |
      Then la risposta ha status code 200
      And la risposta contiene il campo "books"
      And "books" contiene 1 elementi
      And "books" ha un elemento con i campi:
        | isbn        | "9788804336327"         |
        | author      | "Italo Calvino"         |
        | title       | "Il barone rampante"    |
        | description | EMPTY                   |
    
  Rule: Gestione delle copie di un libro
  
    Scenario: aggiunta di una copia di un libro con successo
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
  
  Rule: visualizzazione dettaglio libro
    
    Scenario: Dettaglio di in libro presente nel catalogo
      Given l'amministratore con credenziali "admin@gmail.com", "admin12345678" è autenticato
      And l'amministratore aggiunge un libro con isbn "9788804336327", autore "Italo Calvino", titolo "Il barone rampante" e descrizione
        """

        """
      And l'amministratore aggiunge 3 copie del libro "9788804336327"
      And l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il dettaglio del libro isbn "9788804336327"
      Then la risposta ha status code 200
      And la risposta contiene i seguenti campi:
      | isbn           | "9788804336327"         |
      | author         | "Italo Calvino"         |
      | title          | "Il barone rampante"    |
      | description    | ""                      |
      | totalCopies    | 3                       |
      | borrowedCopies | 0                       |
      | reservedCopies | 0                       |
      | available      | true                    |
      
    Scenario: Dettaglio di un libro non presente nel catalogo
      Given l'utente con credenziali "reader@gmail.com", "Test12345678" è autenticato
      When l'utente visualizza il dettaglio del libro isbn "9788804336327"
      Then la risposta ha status code 404
      And la risposta contiene il campo "message"
      And la risposta contiene i seguenti campi:
      | code    | "BOOK_NOT_FOUND"    |
      | type    | "APPLICATION_ERROR" |
      
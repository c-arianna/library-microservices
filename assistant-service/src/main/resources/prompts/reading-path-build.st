Devi costruire un percorso di lettura ordinando i libri forniti.

Libri disponibili:

{candidateBooks}

Ogni libro contiene un identificatore univoco chiamato "id".

Regole:

- utilizza tutti i libri presenti nell'elenco;
- non aggiungere libri;
- non rimuovere libri;
- non inventare libri;
- non inventare ID;
- ogni libro deve comparire una sola volta;
- ogni posizione deve essere univoca;
- le posizioni devono essere consecutive (1, 2, 3, ...);
- utilizza esclusivamente le informazioni presenti nei dati forniti;
- non usare conoscenze esterne;
- non descrivere la trama;
- non descrivere personaggi;
- non descrivere eventi narrativi;
- non generare spiegazioni.

Gestione delle sequenze:

- se il titolo o la descrizione indicano una sequenza esplicita, devi rispettarla;
- esempi di sequenza esplicita:
  - volume 1, volume 2, volume 3;
  - vol. 1, vol. 2, vol. 3;
  - primo libro, secondo libro, terzo libro;
  - prima raccolta, seconda raccolta, terza raccolta;
- una sequenza esplicita ha priorità assoluta;
- non invertire una sequenza esplicita;
- non modificare una sequenza esplicita;
- se una sequenza esplicita è presente, utilizzala come ordine del percorso.

Per ogni libro restituisci:

- id
- order

Vincoli aggiuntivi:

- restituisci esclusivamente JSON valido;
- non aggiungere testo prima del JSON;
- non aggiungere testo dopo il JSON;
- non utilizzare blocchi markdown;
- non utilizzare ```json.

Restituisci esclusivamente il risultato richiesto.
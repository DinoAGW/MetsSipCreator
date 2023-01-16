# MetsSipCreator

Version: 0.1
implementiert für Rosetta Version: 7.3.0.2

## Obligatorisch

* Dateien, die zur SIP hinzugefügt werden, werden sofort in ein Arbeitsverzeichnis kopiert
* Hinzufügen von dc/dcterms-Metadaten zur ie-dmd-Section
* Validierung von xPathKey (Whitelist aus resources)
* SIP wird erst vorbereitet, am Ende validiert auf Erfüllung von gewissen Kriterien und am Ende finalisiert. Kriterien sind:
  * Es gibt einen 'PRESERVATION_MASTER'
  * Jede Repräsentation hat eine Datei
  * Es gibt mindestens ein dc/dcterms-Metadatum für die ie-dmd-Section
* Bei der Auslieferung werden alle Dateien zum Zielort verschoben, danach kann die SIP-Instanz nicht mehr weiter verwendet werden (egal ob erfolgreich oder nicht)

## Optional implementiert

* hinzufügen von UserDefinedA & B & C
* selbstständige Wahl eines Arbeitsverzeichnisses

## Optional nicht implementiert

* alternativer Modus um Dateien nicht direkt kopiert werden, sondern bei der SIP-finalisierung erst kopiert oder verschoben werden
* Datei aus dem Web, statt von der Festplatte
* hinzufügen von Metadaten zur ie-dmd-Section, die nicht dc/dcterms sind
* ändern des Default SIP-Verzeichnisses (ist hard-coded)
* Löschen von Repräsentationen
* Löschen von Dateien
* Laden einer fertigen SIP-Struktur in eine SIP-Instanz
* Weiternutzung der SIP-Instanz nach (erfolgreicher oder fehlerhafter) Auslieferung
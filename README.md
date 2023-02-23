# MetsSipCreator

Version: 0.2.5
implementiert für Rosetta Version: 7.3.0.2

## Obligatorisch

* Hinzufügen von dc/dcterms-Metadaten zur ie-dmd-Section
* SIP wird erst vorbereitet, am Ende validiert auf Erfüllung von gewissen Kriterien und am Ende finalisiert. Kriterien sind:
  * Es gibt einen 'PRESERVATION_MASTER'
  * Jede Repräsentation hat eine Datei
  * Es gibt mindestens ein dc/dcterms-Metadatum für die ie-dmd-Section
* Bei der Auslieferung werden alle Dateien zum Zielort kopiert
* Repräsentationen und Dateien kriegen automatisch ein Label
* Gebe MimeType zur Datei an

## Optional implementiert

* Hinzufügen von UserDefinedA & B & C
* Hinzufügen von CMS
* Hinzufügen von SourceMD
* Hinzufügen von AR Policy auf IE Ebene
* Hinzufügen von AR Policy auf File Ebene
* Modus zum verschieben statt kopieren der Dateien
* Man kann das Label der Repräsentation oder der Datei ändern
* Hinzufügen von dc-Metadaten auf FileLevel
* Lasse MimeType automatisch anhand Dateiendung erkennen
* Setze md5-Summe (wird bei Auslieferung geprüft)

## Optional nicht implementiert

* definiere StructMap
* Hinzufügen einer AR Policy auf REP Ebene
* Setzen einer anderen RevisionNumer als 1
* Datei aus dem Web, statt von der Festplatte
* hinzufügen von Metadaten zur ie-dmd-Section, die nicht dc/dcterms sind
* Löschen von Repräsentationen
* Löschen von Dateien
* Auswahl eines alternativen PreservationLevels
* Laden einer fertigen SIP-Struktur in eine SIP-Instanz
* Validierung von xPathKey
* Modus um Dateien in einem Arbeitsverzeichnis zwischenzuspeichern
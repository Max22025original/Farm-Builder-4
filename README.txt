FarmBuilder – erweiterter Quellcode-Entwurf
==========================================

Projekt: Paper-Plugin, Java 21, Paper API 1.21.4

Enthaltene Änderungen in diesem Entwurf:
- 121 Katalogeinträge in 11 Kategorien (darunter 20 Zusatz-Einträge).
- Neue Auswahl „Große Trading Hall – alle Berufe“.
- Die Halle erzeugt Villager mit den 13 handelnden Berufen und setzt einfache, feste Startangebote.
- Die Vorschau bleibt: Partikel-Rahmen, Rechtsklick platzieren, Linksklick abbrechen.
- Größen bleiben Klein 15, Mittel 31, Mega 51, Riesig 75.

WICHTIGE EINSCHRÄNKUNGEN – bitte vor dem Servereinsatz beachten:
- Dies ist ein Quellcode-Entwurf und wurde hier nicht kompiliert oder auf einem Server getestet.
- Die Trading Hall setzt feste Startangebote, aber NICHT jeden einzelnen möglichen Villager-Trade und NICHT alle verzauberten Bücher um.
- Die vorhandenen TRAP/BONEMEAL-Farmen verwenden weiterhin Spawner. Damit ist die Forderung „ohne Spawner“ noch NICHT umgesetzt.
- Viele normale Farmen sind weiterhin plugin-gesteuerte Ausgabekisten und keine echten Vanilla-Automatikfarmen. Die Holzfarm und eine vollständig automatische Raketen-Produktionskette sind noch nicht fertig umgesetzt.
- Einige neue Zusatz-Einträge sind nur Katalog-/Ausgabe-Einträge, keine realen Produktionsanlagen.

Bauen:
1. Java 21 und Maven installieren.
2. Im Projektordner ausführen: mvn package
3. Das Ergebnis sollte unter target/FarmBuilder.jar liegen.

Server-Befehl: /farm
Vorschau abbrechen: /farm cancel
Berechtigung: farmbuilder.use (oder OP)

Bitte zuerst auf einem Testserver testen und vor dem Platzieren großer Strukturen ein Welt-Backup erstellen.

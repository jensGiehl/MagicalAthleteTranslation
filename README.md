# Magical Athlete PDF Generator

This tool generates a PDF with character cards for the board game "Magical Athlete".

**The Idea:**
Print the generated PDF on overhead transparency film. Cut out the individual character rectangles and place them into card sleeves along with the original cards. The translated text overlays the original English card, allowing you to play the game in another language without altering the original components.

**Important Printing Instruction:**
When printing the PDF, ensure that you select **"Actual Size"** (or "100%") in your printer settings. Do **not** select "Fit to Page" or "Shrink to Fit", as this will alter the dimensions of the cards, and they may not fit the original cards correctly.

## Acknowledgements

A big thank you to **[brettspielelust](https://boardgamegeek.com/profile/brettspielelust)** for providing the [German translations of the abilities](https://boardgamegeek.com/filepage/311951/german-translation-of-atheletes-powers).

## Prerequisites

*   Java 17 or higher
*   Maven

## Running the Application

### Via Maven (Recommended)

You can run the application directly using Maven. Specify the desired language code with the `-l` parameter (e.g., `de` for German).

```bash
mvn clean compile exec:java -Dexec.mainClass="de.agiehl.bgg.MagicalAthlete" -Dexec.args="-l de"
```

The program will create a file named `characters_de.pdf` in the root directory.

### Parameters

*   `-l` or `--language` (required): Language code (e.g., `de`).
*   `-n` or `--show-name` (optional): If set, the character name is printed on the card overlay. Default: Name is hidden.
*   `-so` or `--skip-overview` (optional): If set, the overview page at the end of the PDF is skipped. Default: Overview page is printed.
*   `-d` or `--dry-run` (optional): If set, only one character card is printed and the overview page is skipped. Useful for testing layout.

**Example with all parameters:**

```bash
mvn clean compile exec:java -Dexec.mainClass="de.agiehl.bgg.MagicalAthlete" -Dexec.args="-l de -n -so"
```

## License Information

### Font: Farro

This project uses the font **Farro**.

*   **Font:** Farro
*   **Designer:** Aart Stuurman
*   **License:** SIL Open Font License, Version 1.1
*   **Source:** [Google Fonts - Farro](https://fonts.google.com/specimen/Farro)

The font file is included in the `src/main/resources` folder.

### Libraries

The project uses the following open-source libraries:
*   **Apache Commons CLI** (Apache 2.0)
*   **Jackson** (Apache 2.0)
*   **OpenPDF** (LGPL / MPL)

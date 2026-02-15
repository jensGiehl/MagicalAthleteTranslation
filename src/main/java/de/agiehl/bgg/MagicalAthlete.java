package de.agiehl.bgg;

import static com.lowagie.text.Element.ALIGN_CENTER;
import static com.lowagie.text.PageSize.A4;
import static com.lowagie.text.pdf.BaseFont.CP1252;
import static com.lowagie.text.pdf.BaseFont.EMBEDDED;
import static com.lowagie.text.pdf.BaseFont.HELVETICA;
import static com.lowagie.text.pdf.BaseFont.IDENTITY_H;
import static com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED;
import static com.lowagie.text.pdf.BaseFont.createFont;
import static com.lowagie.text.pdf.PdfWriter.getInstance;
import static java.awt.Color.BLACK;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.apache.commons.cli.Option.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;

public class MagicalAthlete {

    private static final Logger LOGGER = getLogger(MagicalAthlete.class.getName());

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(builder("l")
                .longOpt("language")
                .hasArg()
                .desc("Language code (e.g. de)")
                .required()
                .build());
        options.addOption(builder("n")
                .longOpt("show-name")
                .desc("Show character name on card")
                .build());
        options.addOption(builder("so")
                .longOpt("skip-overview")
                .desc("Do not print overview page")
                .build());
        options.addOption(builder("d")
                .longOpt("dry-run")
                .desc("Dry run: print only one card and no overview")
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String language = cmd.getOptionValue("l");
            boolean showName = cmd.hasOption("n");
            boolean printOverview = !cmd.hasOption("so");
            boolean dryRun = cmd.hasOption("d");
            new MagicalAthlete().createPdf(language, showName, printOverview, dryRun);
        } catch (ParseException e) {
            LOGGER.log(SEVERE, "Error parsing arguments: {0}", e.getMessage());
            new HelpFormatter().printHelp("MagicalAthlete", options);
            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private void createPdf(String language, boolean showName, boolean printOverview, boolean dryRun) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode charactersNode;
        try (InputStream is = getClass().getResourceAsStream("/characters.json")) {
            if (is == null) throw new RuntimeException("characters.json not found");
            charactersNode = mapper.readTree(is);
        }

        JsonNode langNode;
        String langFile = "/" + language + ".json";
        try (InputStream is = getClass().getResourceAsStream(langFile)) {
            if (is == null) throw new RuntimeException(langFile + " not found");
            langNode = mapper.readTree(is);
        }

        Document document = new Document(A4, 20, 20, 20, 20);
        PdfWriter writer = getInstance(document, new FileOutputStream("characters_" + language + ".pdf"));

        document.open();
        PdfContentByte canvas = writer.getDirectContent();

        // Fonts
        BaseFont bf;
        try {
            String fontPath = getClass().getResource("/Farro-Regular.ttf").toString();
            bf = createFont(fontPath, IDENTITY_H, EMBEDDED);
        } catch (Exception e) {
            LOGGER.log(WARNING, "Could not load Farro-Regular.ttf from resources: {0}", e.getMessage());
            LOGGER.info("Using Helvetica as fallback.");
            bf = createFont(HELVETICA, CP1252, NOT_EMBEDDED);
        }
        
        Font nameFont = new Font(bf, 12, Font.BOLD);
        Font abilityFont = new Font(bf, 8, Font.NORMAL);

        // 1 cm = 28.3465 points
        float cm = 28.3465f;

        // Outer rectangle: 6.1 x 8.7 cm
        float outerWidth = 6.1f * cm;
        float outerHeight = 8.7f * cm;

        // Inner rectangle: 5.2 x 2.5 cm
        float innerWidth = 5.2f * cm;
        float innerHeight = 2.5f * cm;

        // Inner rectangle positioning
        // 0.9 cm bottom margin relative to outer
        // User requested to move it down by 2mm (0.2 cm)
        // Original: 0.9 cm
        // New: 0.9 - 0.2 = 0.7 cm
        float innerMarginBottom = 0.7f * cm;

        // Grid layout
        float horizontalGap = 0.2f * cm;
        float verticalGap = 0.2f * cm;

        // Calculate grid dimensions
        // 3 columns
        float totalGridWidth = (3 * outerWidth) + (2 * horizontalGap);
        // 3 rows
        float totalGridHeight = (3 * outerHeight) + (2 * verticalGap);

        // Centering the grid on the page
        float startX = (A4.getWidth() - totalGridWidth) / 2;
        float startY = (A4.getHeight() - totalGridHeight) / 2 + totalGridHeight - outerHeight;

        float currentX = startX;
        float currentY = startY;

        int col = 0;
        int row = 0;

        if (charactersNode.isArray()) {
            Iterator<JsonNode> elements = charactersNode.elements();
            while (elements.hasNext()) {
                JsonNode charNode = elements.next();
                String id = charNode.get("id").asText();

                String name = id;
                String ability = "";
                if (langNode.has(id)) {
                    JsonNode details = langNode.get(id);
                    if (details.has("name")) name = details.get("name").asText();
                    if (details.has("ability")) ability = details.get("ability").asText();
                }

                // Draw
                drawCharacter(canvas, currentX, currentY, outerWidth, outerHeight, innerWidth, innerHeight, innerMarginBottom, name, ability, nameFont, abilityFont, showName);

                if (dryRun) {
                    break;
                }

                // Move to next position
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                    currentX = startX;
                    currentY -= (outerHeight + verticalGap);
                } else {
                    currentX += (outerWidth + horizontalGap);
                }

                // New page if needed
                if (row >= 3 && elements.hasNext()) {
                    document.newPage();
                    col = 0;
                    row = 0;
                    currentX = startX;
                    currentY = startY;
                }
            }
        }

        if (printOverview && !dryRun) {
            printOverviewPage(document, charactersNode, langNode, bf);
        }

        document.close();
        LOGGER.info("PDF created: characters_" + language + ".pdf");
    }

    private void drawCharacter(PdfContentByte canvas, float x, float y, float w, float h, float iw, float ih, float ibm,
                               String name, String ability, Font nameFont, Font abilityFont, boolean showName) throws Exception {
        canvas.saveState();

        // Outer rectangle
        canvas.setLineWidth(0.5f); // Thin black line
        canvas.setColorStroke(BLACK);
        canvas.rectangle(x, y, w, h);
        canvas.stroke();

        // Inner rectangle with rounded corners
        // Centered horizontally
        float ix = x + (w - iw) / 2;
        float iy = y + ibm;
        
        // Background color for inner rectangle
        // Using a light beige/cream color to be readable but not white (since white can't be printed on transparency)
        // Color: #F5F5DC (Beige) -> RGB: 245, 245, 220
        canvas.setColorFill(new Color(245, 245, 220));
        canvas.roundRectangle(ix, iy, iw, ih, 5);
        canvas.fill();
        
        // Border for inner rectangle
        canvas.setColorStroke(BLACK);
        canvas.roundRectangle(ix, iy, iw, ih, 5); // 5 points radius
        canvas.stroke();

        // Reset fill color to black for text
        canvas.setColorFill(BLACK);

        // Text
        // Name
        if (showName) {
            ColumnText.showTextAligned(canvas, ALIGN_CENTER, new Paragraph(name, nameFont), x + w / 2, y + h - 20, 0);
        }

        // Ability
        ColumnText ct = new ColumnText(canvas);
        float padding = 3;
        ct.setSimpleColumn(ix + padding, iy + padding, ix + iw - padding, iy + ih - padding);
        Paragraph p = new Paragraph(ability, abilityFont);
        p.setAlignment(ALIGN_CENTER);
        ct.addElement(p);
        ct.go();
        
        canvas.restoreState();
    }

    private void printOverviewPage(Document document, JsonNode charactersNode, JsonNode langNode, BaseFont bf) throws Exception {
        document.newPage();

        Font nameFont = new Font(bf, 10, Font.BOLD);
        Font abilityFont = new Font(bf, 6, Font.NORMAL);

        PdfPTable table = new PdfPTable(new float[]{1, 3});
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        if (charactersNode.isArray()) {
            Iterator<JsonNode> elements = charactersNode.elements();
            while (elements.hasNext()) {
                JsonNode charNode = elements.next();
                String id = charNode.get("id").asText();

                String name = id;
                String ability = "";
                if (langNode.has(id)) {
                    JsonNode details = langNode.get(id);
                    if (details.has("name")) name = details.get("name").asText();
                    if (details.has("ability")) ability = details.get("ability").asText();
                }

                PdfPCell nameCell = new PdfPCell(new Paragraph(name, nameFont));
                nameCell.setPadding(5);
                table.addCell(nameCell);

                PdfPCell abilityCell = new PdfPCell(new Paragraph(ability, abilityFont));
                abilityCell.setPadding(5);
                table.addCell(abilityCell);
            }
        }

        document.add(table);
    }
}

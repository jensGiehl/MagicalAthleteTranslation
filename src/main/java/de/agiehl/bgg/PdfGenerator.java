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
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PdfGenerator {

    private static final Logger LOGGER = getLogger(PdfGenerator.class.getName());
    private static final float CM = 28.3465f;
    private static final float OUTER_WIDTH = 6.1f * CM;
    private static final float OUTER_HEIGHT = 8.7f * CM;
    private static final float INNER_WIDTH = 5.2f * CM;
    private static final float INNER_HEIGHT = 2.5f * CM;
    private static final float INNER_MARGIN_BOTTOM = 0.7f * CM;
    private static final float HORIZONTAL_GAP = 0.2f * CM;
    private static final float VERTICAL_GAP = 0.2f * CM;

    private final PdfConfig config;
    private final List<CharacterData> characters;
    private BaseFont baseFont;

    public PdfGenerator(PdfConfig config, List<CharacterData> characters) {
        this.config = config;
        this.characters = characters;
        initFont();
    }

    private void initFont() {
        try {
            String fontPath = requireNonNull(getClass().getResource("/Farro-Regular.ttf")).toString();
            baseFont = createFont(fontPath, IDENTITY_H, EMBEDDED);
        } catch (Exception e) {
            LOGGER.log(WARNING, "Could not load Farro-Regular.ttf from resources: {0}", e.getMessage());
            LOGGER.info("Using Helvetica as fallback.");
            try {
                baseFont = createFont(HELVETICA, CP1252, NOT_EMBEDDED);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void generate() throws Exception {
        Document document = new Document(A4, 20, 20, 20, 20);
        PdfWriter writer = getInstance(document, new FileOutputStream("characters_" + config.language() + ".pdf"));

        document.open();
        PdfContentByte canvas = writer.getDirectContent();

        Font nameFont = new Font(baseFont, 12, Font.BOLD);
        nameFont.setColor(config.textColor());
        Font abilityFont = new Font(baseFont, 8, Font.NORMAL);
        abilityFont.setColor(config.textColor());

        float totalGridWidth = (3 * OUTER_WIDTH) + (2 * HORIZONTAL_GAP);
        float totalGridHeight = (3 * OUTER_HEIGHT) + (2 * VERTICAL_GAP);
        float startX = (A4.getWidth() - totalGridWidth) / 2;
        float startY = (A4.getHeight() - totalGridHeight) / 2 + totalGridHeight - OUTER_HEIGHT;

        float currentX = startX;
        float currentY = startY;
        int col = 0;
        int row = 0;

        for (CharacterData character : characters) {
            drawCharacter(canvas, currentX, currentY, character, nameFont, abilityFont);

            if (config.dryRun()) {
                break;
            }

            col++;
            if (col >= 3) {
                col = 0;
                row++;
                currentX = startX;
                currentY -= (OUTER_HEIGHT + VERTICAL_GAP);
            } else {
                currentX += (OUTER_WIDTH + HORIZONTAL_GAP);
            }

            if (row >= 3 && characters.indexOf(character) < characters.size() - 1) {
                document.newPage();
                col = 0;
                row = 0;
                currentX = startX;
                currentY = startY;
            }
        }

        if (config.printOverview() && !config.dryRun()) {
            printOverviewPage(document);
        }

        document.close();
        LOGGER.info("PDF created: characters_" + config.language() + ".pdf");
    }

    private void drawCharacter(PdfContentByte canvas, float x, float y, CharacterData character, Font nameFont, Font abilityFont) {
        canvas.saveState();

        // Outer rectangle
        canvas.setLineWidth(0.5f);
        canvas.setColorStroke(BLACK);
        canvas.rectangle(x, y, OUTER_WIDTH, OUTER_HEIGHT);
        canvas.stroke();

        // Inner rectangle
        float ix = x + (OUTER_WIDTH - INNER_WIDTH) / 2;
        float iy = y + INNER_MARGIN_BOTTOM;

        canvas.setColorFill(config.bgColor());
        canvas.roundRectangle(ix, iy, INNER_WIDTH, INNER_HEIGHT, 5);
        canvas.fill();

        canvas.setColorStroke(config.textColor());
        canvas.roundRectangle(ix, iy, INNER_WIDTH, INNER_HEIGHT, 5);
        canvas.stroke();

        canvas.setColorFill(config.textColor());

        if (config.showName()) {
            ColumnText.showTextAligned(canvas, ALIGN_CENTER, new Paragraph(character.name(), nameFont), x + OUTER_WIDTH / 2, y + OUTER_HEIGHT - 20, 0);
        }

        drawAbilityText(canvas, ix, iy, character.ability(), abilityFont);

        canvas.restoreState();
    }

    private void drawAbilityText(PdfContentByte canvas, float ix, float iy, String ability, Font abilityFont) throws DocumentException {
        float padding = 3;
        float fontSize = abilityFont.getSize();

        while (fontSize > 4) {
            if (doesTextFit(canvas, ix, iy, ability, abilityFont, fontSize, padding)) {
                break;
            }
            fontSize -= 0.5f;
        }

        drawText(canvas, ix, iy, ability, abilityFont, fontSize, padding);
    }

    private boolean doesTextFit(PdfContentByte canvas, float ix, float iy, String text, Font originalFont, float fontSize, float padding) {
        try {
            ColumnText ct = createColumnText(canvas, ix, iy, padding);
            Paragraph p = createParagraph(text, originalFont, fontSize);
            ct.addElement(p);
            return ct.go(true) != ColumnText.NO_MORE_COLUMN;
        } catch (DocumentException e) {
            return false;
        }
    }

    private void drawText(PdfContentByte canvas, float ix, float iy, String text, Font originalFont, float fontSize, float padding) throws DocumentException {
        ColumnText ct = createColumnText(canvas, ix, iy, padding);
        Paragraph p = createParagraph(text, originalFont, fontSize);
        ct.addElement(p);
        ct.go();
    }

    private ColumnText createColumnText(PdfContentByte canvas, float ix, float iy, float padding) {
        ColumnText ct = new ColumnText(canvas);
        ct.setSimpleColumn(ix + padding, iy + padding, ix + INNER_WIDTH - padding, iy + INNER_HEIGHT - padding);
        return ct;
    }

    private Paragraph createParagraph(String text, Font originalFont, float fontSize) {
        Font font = new Font(originalFont.getBaseFont(), fontSize, originalFont.getStyle());
        font.setColor(config.textColor());
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(ALIGN_CENTER);
        return p;
    }

    private void printOverviewPage(Document document) {
        document.newPage();

        Font nameFont = new Font(baseFont, 10, Font.BOLD);
        Font abilityFont = new Font(baseFont, 6, Font.NORMAL);

        PdfPTable table = new PdfPTable(new float[]{1, 3});
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        List<CharacterData> sortedCharacters = characters.stream()
                .sorted(comparing(CharacterData::name))
                .toList();

        for (CharacterData character : sortedCharacters) {
            PdfPCell nameCell = new PdfPCell(new Paragraph(character.name(), nameFont));
            nameCell.setPadding(5);
            table.addCell(nameCell);

            PdfPCell abilityCell = new PdfPCell(new Paragraph(character.ability(), abilityFont));
            abilityCell.setPadding(5);
            table.addCell(abilityCell);
        }

        document.add(table);
    }
}

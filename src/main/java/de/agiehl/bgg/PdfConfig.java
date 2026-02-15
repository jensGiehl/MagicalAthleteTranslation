package de.agiehl.bgg;

import java.awt.Color;

public record PdfConfig(
        String language,
        boolean showName,
        boolean printOverview,
        boolean dryRun,
        Color bgColor,
        Color textColor
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String language;
        private boolean showName;
        private boolean printOverview;
        private boolean dryRun;
        private Color bgColor;
        private Color textColor;

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder showName(boolean showName) {
            this.showName = showName;
            return this;
        }

        public Builder printOverview(boolean printOverview) {
            this.printOverview = printOverview;
            return this;
        }

        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder bgColor(Color bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Builder textColor(Color textColor) {
            this.textColor = textColor;
            return this;
        }

        public PdfConfig build() {
            return new PdfConfig(language, showName, printOverview, dryRun, bgColor, textColor);
        }
    }
}

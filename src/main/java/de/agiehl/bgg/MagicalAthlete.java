package de.agiehl.bgg;

import static java.awt.Color.BLACK;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static org.apache.commons.cli.Option.builder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.awt.Color;
import java.util.List;
import java.util.logging.Logger;

public class MagicalAthlete {

    private static final Logger LOGGER = getLogger(MagicalAthlete.class.getName());

    public static void main(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            PdfConfig config = createConfig(cmd);
            
            List<CharacterData> characters = new CharacterLoader().loadCharacters(config.language());
            new PdfGenerator(config, characters).generate();
            
        } catch (ParseException e) {
            LOGGER.log(SEVERE, "Error parsing arguments: {0}", e.getMessage());
            new HelpFormatter().printHelp("MagicalAthlete", options);
            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private static Options createOptions() {
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
        options.addOption(builder("bg")
                .longOpt("background-color")
                .hasArg()
                .desc("Hex color for inner rectangle background (default: #F5F5DC)")
                .build());
        options.addOption(builder("tc")
                .longOpt("text-color")
                .hasArg()
                .desc("Hex color for text and inner rectangle border (default: #000000)")
                .build());
        return options;
    }

    private static PdfConfig createConfig(CommandLine cmd) {
        String language = cmd.getOptionValue("l");
        boolean showName = cmd.hasOption("n");
        boolean printOverview = !cmd.hasOption("so");
        boolean dryRun = cmd.hasOption("d");

        String bgColorStr = cmd.getOptionValue("bg", "#F5F5DC");
        String textColorStr = cmd.getOptionValue("tc", "#000000");

        Color bgColor;
        Color textColor;
        try {
            bgColor = Color.decode(bgColorStr);
            textColor = Color.decode(textColorStr);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid color format. Using defaults.");
            bgColor = new Color(245, 245, 220);
            textColor = BLACK;
        }

        return PdfConfig.builder()
                .language(language)
                .showName(showName)
                .printOverview(printOverview)
                .dryRun(dryRun)
                .bgColor(bgColor)
                .textColor(textColor)
                .build();
    }
}

package de.agiehl.bgg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CharacterLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<CharacterData> loadCharacters(String language) throws Exception {
        JsonNode charactersNode = loadJson("/characters.json");
        JsonNode langNode = loadJson("/" + language + ".json");

        List<CharacterData> characters = new ArrayList<>();
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
                characters.add(CharacterData.builder()
                        .id(id)
                        .name(name)
                        .ability(ability)
                        .build());
            }
        }
        return characters;
    }

    private JsonNode loadJson(String path) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException(path + " not found");
            return mapper.readTree(is);
        }
    }
}

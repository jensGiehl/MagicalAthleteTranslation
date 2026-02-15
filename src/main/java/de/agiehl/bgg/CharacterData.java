package de.agiehl.bgg;

public record CharacterData(
        String id,
        String name,
        String ability
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String ability;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ability(String ability) {
            this.ability = ability;
            return this;
        }

        public CharacterData build() {
            return new CharacterData(id, name, ability);
        }
    }
}

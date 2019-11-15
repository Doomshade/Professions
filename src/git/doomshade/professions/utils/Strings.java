package git.doomshade.professions.utils;

public final class Strings {

    public enum ITrainableEnum {
        TRAINABLE("trainable"), COST("trainable-cost"), TRAINABLE_ID("trainable-id"), VAR_TRAINABLE_COST("\\{trainable-cost\\}");

        public final String s;

        ITrainableEnum(String s) {
            this.s = s;
        }
    }

    public enum ICraftableEnum {
        ITEM_REQUIREMENTS("item-requirements"),
        RESULT("result"),
        CRAFTING_TIME("crafting-time"),
        INVENTORY_REQUIREMENTS("inventory-requirements");

        public final String s;

        ICraftableEnum(String s) {
            this.s = s;
        }
    }

    public enum ItemTypeEnum {
        LEVEL_REQ("level-req"),
        PROFTYPE("type-unchangable"),
        EXP("exp"),
        OBJECT("object"),
        NAME("name"),
        DESCRIPTION("description"),
        MATERIAL("gui-material"),
        RESTRICTED_WORLDS("restricted-worlds"),
        HIDDEN("hidden-when-unavailable"),
        LEVEL_REQ_COLOR("level-req-color"),
        IGNORE_SKILLUP_COLOR("ignore-skillup-color");

        public final String s;

        ItemTypeEnum(String s) {
            this.s = s;
        }

        @Override
        @Deprecated
        public String toString() {
            return s;
        }
    }
}

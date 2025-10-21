package yuuki1293.ae2peat.definisions;

import appeng.core.localization.LocalizationEnum;
import yuuki1293.ae2peat.AE2PEAT;

public enum PEATText implements LocalizationEnum {
    ModName("AE2 Pattern Encoding Access Terminal", Type.GUI);

    private final String englishText;
    private final Type type;

    PEATText(String englishText, Type type) {
        this.englishText = englishText;
        this.type = type;
    }

    @Override
    public String getEnglishText() {
        return englishText;
    }

    @Override
    public String getTranslationKey() {
        return String.format("%s.%s.%s", type.root, AE2PEAT.MODID, name());
    }

    private enum Type {
        GUI("gui"),
        TOOLTIP("gui.tooltips"),
        EMI_CATEGORY("emi.category"),
        EMI_TEXT("emi.text");

        private final String root;

        Type(String root) {
            this.root = root;
        }
    }
}

package yuuki1293.ae2peat.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATText;

public class PEATLanguageProvider extends LanguageProvider {
    public PEATLanguageProvider(PackOutput output){
        super(output, AE2PEAT.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (var item : PEATItems.getItems()) {
            add(item.asItem(), item.getEnglishName());
        }

        for (var translation : PEATText.values()) {
            add(translation.getTranslationKey(), translation.getEnglishText());
        }

        generateLocalizations();
    }

    private void generateLocalizations() {
        add("key." + AE2PEAT.MOD_ID + ".category", "AE2 Pattern Encoding Access Terminal");
    }
}

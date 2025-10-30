package yuuki1293.ae2peat.datagen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import yuuki1293.ae2peat.AE2PEAT;

@EventBusSubscriber(modid = AE2PEAT.MOD_ID, value = Dist.CLIENT)
public class PEATDataGen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var out = gen.getPackOutput();
        var fileHelper = event.getExistingFileHelper();
        var lookup = event.getLookupProvider();
        var languageProvider = new PEATLanguageProvider(out);

        gen.addProvider(event.includeClient(), new PEATModelProvider(out, fileHelper));
        gen.addProvider(event.includeServer(), new PEATRecipeProvider(out, lookup));

        gen.addProvider(event.includeClient(), languageProvider);
    }
}

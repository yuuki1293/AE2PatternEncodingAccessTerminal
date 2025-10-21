package yuuki1293.ae2peat.datagen;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yuuki1293.ae2peat.AE2PEAT;

@Mod.EventBusSubscriber(modid = AE2PEAT.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PEATDataGen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var out = gen.getPackOutput();
        var fileHelper = event.getExistingFileHelper();
        var lookup = event.getLookupProvider();
        var languageProvider = new PEATLanguageProvider(out);

//        gen.addProvider(event.includeClient(), new PEATModelProvider(out, fileHelper));
//        gen.addProvider(event.includeServer(), new PEATRecipeProvider(out, lookup));
//
//        var blockTags = new PEATTagProvider.PEATBlockTagProvider(out, lookup, fileHelper);
//        var itemTags = new PEATTagProvider.PEATItemTagProvider(out, lookup, blockTags.contentsGetter(), fileHelper);
//        gen.addProvider(event.includeServer(), blockTags);
//        gen.addProvider(event.includeServer(), itemTags);

        gen.addProvider(event.includeClient(), languageProvider);
    }
}

package yuuki1293.ae2peat.integration.modules.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.integration.modules.jei.transfer.EncodePatternTransferHandler;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AE2PEAT.MODID, "core");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Universal handler for processing to try and handle all IRecipe
        registration.addUniversalRecipeTransferHandler(new EncodePatternTransferHandler<>(
            PatternEncodingAccessTermMenu.TYPE,
            PatternEncodingAccessTermMenu.class,
            registration.getTransferHelper()));
    }
}

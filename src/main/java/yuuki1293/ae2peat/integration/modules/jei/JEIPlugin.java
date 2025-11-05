package yuuki1293.ae2peat.integration.modules.jei;

import appeng.core.AppEng;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.integration.modules.jei.transfer.EncodePatternTransferHandler;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.wireless.WPEATMenu;
import yuuki1293.ae2peat.xmod.Addons;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    private static final ResourceLocation ID = AE2PEAT.makeId("core");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
        if (!Addons.AE2_JEI_INTEGRATION.isLoaded()) return;

        var jeiHelpers = registration.getJeiHelpers();
        var ingredientVisibility = jeiHelpers.getIngredientVisibility();
        var transferHelper = registration.getTransferHelper();

        // Universal handler for processing to try and handle all IRecipe
        registration.addUniversalRecipeTransferHandler(new EncodePatternTransferHandler<>(
                PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                PatternEncodingAccessTermMenu.class,
                transferHelper,
                ingredientVisibility));

        if (Addons.AE2WTLIB.isLoaded()) {
            registration.addUniversalRecipeTransferHandler(new EncodePatternTransferHandler<>(
                    WPEATMenu.TYPE, WPEATMenu.class, transferHelper, ingredientVisibility));
        }
    }
}

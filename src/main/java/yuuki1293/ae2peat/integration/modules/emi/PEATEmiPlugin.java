package yuuki1293.ae2peat.integration.modules.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.wireless.WPEATMenu;
import yuuki1293.ae2peat.xmod.Addons;

@EmiEntrypoint
public class PEATEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        // Recipe transfer
        registry.addRecipeHandler(
                PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                new EmiEncodePatternHandler<>(PatternEncodingAccessTermMenu.class));
        if (Addons.AE2WTLIB.isLoaded()) {
            registry.addRecipeHandler(WPEATMenu.TYPE, new EmiEncodePatternHandler<>(WPEATMenu.class));
        }
    }
}

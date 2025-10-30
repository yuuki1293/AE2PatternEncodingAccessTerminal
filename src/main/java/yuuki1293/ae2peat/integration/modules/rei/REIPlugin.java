package yuuki1293.ae2peat.integration.modules.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.forge.REIPluginClient;
import mezz.jei.api.JeiPlugin;
import yuuki1293.ae2peat.integration.modules.rei.transfer.EncodePatternTransferHandler;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.wireless.WPEATMenu;

@SuppressWarnings("unused")
@REIPluginClient
public class REIPlugin implements REIClientPlugin {
    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new EncodePatternTransferHandler<>(PatternEncodingAccessTermMenu.class));
        registry.register(new EncodePatternTransferHandler<>(WPEATMenu.class));
    }
}

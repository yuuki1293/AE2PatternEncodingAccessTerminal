package yuuki1293.ae2peat.client;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;
import net.minecraft.client.color.item.ItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.wireless.WPEATMenu;
import yuuki1293.ae2peat.wireless.WPEATScreen;

@Mod(value = AE2PEAT.MOD_ID, dist = Dist.CLIENT)
public class AE2PEATClient {
    public AE2PEATClient(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(AE2PEATClient::initScreen);
        eventBus.addListener(AE2PEATClient::initItemColours);
    }

    private static void initScreen(RegisterMenuScreensEvent event) {
        InitScreens
                .<PatternEncodingAccessTermMenu, PatternEncodingAccessTermScreen<PatternEncodingAccessTermMenu>>
                        register(
                                event,
                                PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                                PatternEncodingAccessTermScreen::new,
                                "/screens/terminals/pattern_encoding_access_terminal.json");
        InitScreens.<WPEATMenu, WPEATScreen>register(
                event,
                WPEATMenu.TYPE,
                WPEATScreen::new,
                "/screens/wtlib/wireless_pattern_encoding_access_terminal.json");
    }

    @SubscribeEvent
    private static void initItemColours(RegisterColorHandlersEvent.Item event) {
        event.register(
                makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)),
                PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL.asItem());
    }

    private static ItemColor makeOpaque(ItemColor itemColor) {
        return (stack, tintIndex) -> itemColor.getColor(stack, tintIndex) | 0xFF000000;
    }
}

package yuuki1293.ae2peat;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.core.AELog;
import appeng.init.client.InitScreens;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import yuuki1293.ae2peat.definisions.PEATCreativeTab;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.wireless.WPEATMenu;
import yuuki1293.ae2peat.wireless.WPEATScreen;
import yuuki1293.ae2peat.xmod.Addons;
import yuuki1293.ae2peat.xmod.ae2wtlib.AE2WtLibPlugin;

@Mod(value = AE2PEAT.MOD_ID)
public class AE2PEAT {
    public static final String MOD_ID = "ae2peat";

    public AE2PEAT(IEventBus modEventBus, ModContainer modContainer) {
        PEATItems.INSTANCE.register(modEventBus);
        PEATMenus.INSTANCE.register(modEventBus);
        PEATCreativeTab.INSTANCE.register(modEventBus);
    }

    public static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    @Mod(value = MOD_ID, dist = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(RegisterMenuScreensEvent event) {
            InitScreens
                    .<PatternEncodingAccessTermMenu, PatternEncodingAccessTermScreen<PatternEncodingAccessTermMenu>>
                            register(
                                    event,
                                    PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                                    PatternEncodingAccessTermScreen::new,
                                    "/screens/terminals/pattern_encoding_access_terminal.json");
            if (Addons.AE2WTLIB.isLoaded()) {
                InitScreens.<WPEATMenu, WPEATScreen>register(
                    event,
                    WPEATMenu.TYPE,
                    WPEATScreen::new,
                    "/screens/wtlib/wireless_pattern_encoding_access_terminal.json");
            }
        }

        @SubscribeEvent
        public static void initItemColours(RegisterColorHandlersEvent.Item event) {
            event.register(
                    makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)),
                    PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL.asItem());
        }

        private static ItemColor makeOpaque(ItemColor itemColor) {
            return (stack, tintIndex) -> itemColor.getColor(stack, tintIndex) | 0xFF000000;
        }
    }
}

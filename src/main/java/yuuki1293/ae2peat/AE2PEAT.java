package yuuki1293.ae2peat;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.core.AELog;
import appeng.init.client.InitScreens;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import yuuki1293.ae2peat.definisions.PEATCreativeTab;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.xmod.Addons;
import yuuki1293.ae2peat.xmod.ae2wtlib.AE2WtLibPlugin;

@Mod(AE2PEAT.MOD_ID)
public class AE2PEAT {
    public static final String MOD_ID = "ae2peat";

    public AE2PEAT() {
        var context = FMLJavaModLoadingContext.get();
        var eventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        PEATItems.INSTANCE.register(eventBus);
        PEATMenus.INSTANCE.register(eventBus);
        PEATCreativeTab.INSTANCE.register(eventBus);

        eventBus.addListener(this::commonSetup);
        eventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey() == Registries.ITEM) {
                if (Addons.AE2WTLIB.isLoaded()) {
                    AE2WtLibPlugin.initMenuType();
                }
            } else if (event.getRegistryKey() == ForgeRegistries.MENU_TYPES) {
                if (Addons.AE2WTLIB.isLoaded()) {
                    AE2WtLibPlugin.initMenu();
                }
            }
        });
    }

    public static ResourceLocation makeId(String id) {
        return new ResourceLocation(MOD_ID, id);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (Addons.AE2WTLIB.isLoaded()) {
            AE2WtLibPlugin.initGridLinkables();
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SuppressWarnings("RedundantTypeArguments")
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            InitScreens
                    .<PatternEncodingAccessTermMenu, PatternEncodingAccessTermScreen<PatternEncodingAccessTermMenu>>
                            register(
                                    PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                                    PatternEncodingAccessTermScreen::new,
                                    "/screens/terminals/pattern_encoding_access_terminal.json");
            if (Addons.AE2WTLIB.isLoaded()) {
                AE2WtLibPlugin.initScreen();
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

package yuuki1293.ae2peat;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;
import com.mojang.logging.LogUtils;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import yuuki1293.ae2peat.definisions.PEATCreativeTab;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mod(AE2PEAT.MOD_ID)
public class AE2PEAT {
    public static final String MOD_ID = "ae2peat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AE2PEAT(FMLJavaModLoadingContext context) {
        var eventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        PEATItems.INSTANCE.register(eventBus);
        PEATMenus.INSTANCE.register(eventBus);
        PEATCreativeTab.INSTANCE.register(eventBus);
    }

    public static ResourceLocation makeId(String id){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            InitScreens.<PatternEncodingAccessTermMenu, PatternEncodingAccessTermScreen<PatternEncodingAccessTermMenu>>register(
                PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                PatternEncodingAccessTermScreen::new,
                "/screens/terminals/pattern_encoding_access_terminal.json"
            );
        }

        @SubscribeEvent
        public static void initItemColours(RegisterColorHandlersEvent.Item event) {
            event.register(makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)), PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL.asItem());
        }

        private static ItemColor makeOpaque(ItemColor itemColor) {
            return (stack, tintIndex) -> itemColor.getColor(stack, tintIndex) | 0xFF000000;
        }
    }
}

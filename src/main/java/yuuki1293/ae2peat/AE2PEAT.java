package yuuki1293.ae2peat;

import appeng.init.client.InitScreens;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mod(AE2PEAT.MODID)
public class AE2PEAT {
    public static final String MODID = "ae2peat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AE2PEAT(FMLJavaModLoadingContext context) {
        var eventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        PEATItems.DR.register(eventBus);
        PEATMenus.DR.register(eventBus);
    }

    public static ResourceLocation makeId(String id){
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            InitScreens.<PatternEncodingAccessTermMenu, PatternEncodingAccessTermScreen<PatternEncodingAccessTermMenu>>register(
                PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                PatternEncodingAccessTermScreen::new,
                "/screens/terminals/pattern_encoding_access_terminal.json"
            );
        }
    }
}

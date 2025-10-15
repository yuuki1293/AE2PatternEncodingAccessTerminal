package yuuki1293.ae2peat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import yuuki1293.ae2peat.init.InitItems;
import yuuki1293.ae2peat.init.InitMenuTypes;

@Mod(AE2PEAT.MODID)
public class AE2PEAT {
    public static final String MODID = "ae2peat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AE2PEAT(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        PEATParts.init();

        InitItems.init(ForgeRegistries.ITEMS);
        InitMenuTypes.init(ForgeRegistries.MENU_TYPES);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

    }
}

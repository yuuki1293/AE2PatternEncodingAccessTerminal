package yuuki1293.ae2peat;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import yuuki1293.ae2peat.definisions.PEATCreativeTab;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATMenus;

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
}

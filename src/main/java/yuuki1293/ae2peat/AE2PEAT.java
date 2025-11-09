package yuuki1293.ae2peat;

import appeng.api.features.GridLinkables;
import appeng.core.AELog;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.pedroksl.ae2addonlib.api.IGridLinkedItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yuuki1293.ae2peat.definisions.PEATCreativeTab;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.definisions.PEATMenus;

@Mod(value = AE2PEAT.MOD_ID)
public class AE2PEAT {
    public static final String MOD_ID = "ae2peat";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    static AE2PEAT INSTANCE;

    public AE2PEAT(IEventBus modEventBus, ModContainer modContainer) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        PEATItems.INSTANCE.register(modEventBus);
        PEATMenus.INSTANCE.register(modEventBus);
        PEATCreativeTab.INSTANCE.register(modEventBus);

        modEventBus.addListener(AE2PEAT::initUpgrades);

        modEventBus.addListener(this::commonSetup);
    }

    public static AE2PEAT instance() {
        return INSTANCE;
    }

    public static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization).whenComplete((res, err) -> {
            if (err != null) {
                AELog.warn(err);
            }
        });
    }

    public void postRegistrationInitialization() {
        GridLinkables.register(PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL, IGridLinkedItem.LINKABLE_HANDLER);
    }

    private static void initUpgrades(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {});
    }
}

package yuuki1293.ae2peat.definisions;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.pedroksl.ae2addonlib.registry.ConfigRegistry;
import yuuki1293.ae2peat.AE2PEAT;

@Mod.EventBusSubscriber(modid = AE2PEAT.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PEATConfig extends ConfigRegistry {
    private final PEATConfig.ClientConfig client = new PEATConfig.ClientConfig();
    private final PEATConfig.CommonConfig common = new PEATConfig.CommonConfig();

    private static PEATConfig INSTANCE;

    PEATConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, client.spec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, common.spec);
    }

    public void save() {
        common.spec.save();
        client.spec.save();
    }

    public static void register(String mod_id) {
        if (!mod_id.equals(AE2PEAT.MOD_ID)) {
            throw new IllegalArgumentException();
        }
        INSTANCE = new PEATConfig();
    }

    public static PEATConfig instance() {
        return INSTANCE;
    }

    private static class ClientConfig {
        private final ForgeConfigSpec spec;

        public ClientConfig() {
            var builder = new ForgeConfigSpec.Builder();

            this.spec = builder.build();
        }
    }

    private static class CommonConfig {
        private final ForgeConfigSpec spec;

        public CommonConfig() {
            var builder = new ForgeConfigSpec.Builder();

            this.spec = builder.build();
        }
    }
}

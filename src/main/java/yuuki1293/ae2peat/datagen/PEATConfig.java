package yuuki1293.ae2peat.datagen;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.pedroksl.ae2addonlib.registry.ConfigRegistry;
import yuuki1293.ae2peat.AE2PEAT;

public class PEATConfig extends ConfigRegistry {
    private final PEATConfig.ClientConfig client = new PEATConfig.ClientConfig();
    private final PEATConfig.CommonConfig common = new PEATConfig.CommonConfig();

    private static PEATConfig INSTANCE;

    PEATConfig(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, client.spec);
        modContainer.registerConfig(ModConfig.Type.COMMON, common.spec);
    }

    public void save() {
        common.spec.save();
        client.spec.save();
    }

    public static void register(ModContainer modContainer) {
        if (!modContainer.getModId().equals(AE2PEAT.MOD_ID)) {
            throw new IllegalArgumentException();
        }
        INSTANCE = new PEATConfig(modContainer);
    }

    public static PEATConfig instance() {
        return INSTANCE;
    }

    private static class ClientConfig {
        private final ModConfigSpec spec;

        public ClientConfig() {
            var builder = new ModConfigSpec.Builder();

            this.spec = builder.build();
        }
    }

    private static class CommonConfig {
        private final ModConfigSpec spec;

        public CommonConfig() {
            var builder = new ModConfigSpec.Builder();

            this.spec = builder.build();
        }
    }
}

package yuuki1293.ae2peat.api.config;

import appeng.api.config.Setting;
import net.pedroksl.ae2addonlib.api.SettingsRegistry;
import yuuki1293.ae2peat.AE2PEAT;

public class PEATSettings extends SettingsRegistry {
    public static final Setting<AccessSearchMode> ACCESS_SEARCH_MODE =
            register(AE2PEAT.MOD_ID, "access_search_mode", AccessSearchMode.class);
}

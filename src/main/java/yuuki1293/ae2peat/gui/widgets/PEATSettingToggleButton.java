package yuuki1293.ae2peat.gui.widgets;

import appeng.api.config.Setting;
import java.util.function.Predicate;
import net.pedroksl.ae2addonlib.client.widgets.AddonSettingToggleButton;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.api.config.AccessSearchMode;
import yuuki1293.ae2peat.api.config.PEATSettings;
import yuuki1293.ae2peat.definisions.PEATText;

public class PEATSettingToggleButton<T extends Enum<T>> extends AddonSettingToggleButton<T> {
    public PEATSettingToggleButton(
        Setting<T> setting, T val, AddonSettingToggleButton.IHandler<AddonSettingToggleButton<T>> onPress) {
        super(setting, val, t -> true, onPress);
    }

    public PEATSettingToggleButton(
        Setting<T> setting,
        T val,
        Predicate<T> isValidValue,
        AddonSettingToggleButton.IHandler<AddonSettingToggleButton<T>> onPress) {
        super(setting, val, isValidValue, onPress);
    }

    public static <T extends Enum<T>> PEATSettingToggleButton<T> serverButton(Setting<T> setting, T val) {
        return AddonSettingToggleButton.serverButton(setting, val, AE2PEAT.MOD_ID, PEATSettingToggleButton::new);
    }

    @Override
    protected void registerAppearances() {
        registerApp(
            PEATIcon.ACCESS_SEARCH_MODE_BOTH,
            PEATSettings.ACCESS_SEARCH_MODE,
            AccessSearchMode.BOTH,
            PEATText.AccessSearchModeCategory,
            PEATText.AccessSearchModeBoth);
        registerApp(
            PEATIcon.ACCESS_SEARCH_MODE_PATTERN,
            PEATSettings.ACCESS_SEARCH_MODE,
            AccessSearchMode.PATTERN,
            PEATText.AccessSearchModeCategory,
            PEATText.AccessSearchModePattern);
        registerApp(
            PEATIcon.ACCESS_SEARCH_MODE_MACHINE,
            PEATSettings.ACCESS_SEARCH_MODE,
            AccessSearchMode.MACHINE,
            PEATText.AccessSearchModeCategory,
            PEATText.AccessSearchModeMachine);
    }
}

package yuuki1293.ae2peat.wireless;

import appeng.api.config.*;
import appeng.api.util.IConfigManager;
import appeng.util.ConfigManager;
import de.mari_023.ae2wtlib.terminal.IUniversalWirelessTerminalItem;
import de.mari_023.ae2wtlib.terminal.ItemWT;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.ae2addonlib.api.IGridLinkedItem;
import org.jetbrains.annotations.NotNull;

public class ItemWPEAT extends ItemWT implements IUniversalWirelessTerminalItem, IGridLinkedItem {
    @Override
    public @NotNull MenuType<?> getMenuType(@NotNull ItemStack stack) {
        return WPEATMenu.TYPE;
    }

    public @NotNull IConfigManager getConfigManager(@NotNull ItemStack target) {
        var configManager = new ConfigManager((manager, settingName) -> manager.writeToNBT(target.getOrCreateTag()));

        configManager.registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE);
        configManager.readFromNBT(target.getOrCreateTag().copy());
        return configManager;
    }
}

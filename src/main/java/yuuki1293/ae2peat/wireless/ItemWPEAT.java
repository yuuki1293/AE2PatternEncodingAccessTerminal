package yuuki1293.ae2peat.wireless;

import appeng.api.config.*;
import appeng.api.util.IConfigManager;
import appeng.menu.locator.ItemMenuHostLocator;
import de.mari_023.ae2wtlib.api.terminal.AE2wtlibConfigManager;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.ae2addonlib.api.IGridLinkedItem;
import org.jetbrains.annotations.NotNull;

public class ItemWPEAT extends ItemWT implements IGridLinkedItem {
    @Override
    public @NotNull MenuType<?> getMenuType(@NotNull ItemMenuHostLocator locator, @NotNull Player player) {
        return WPEATMenu.TYPE;
    }

    public @NotNull IConfigManager getConfigManager(@NotNull Supplier<ItemStack> target) {
        return AE2wtlibConfigManager.builder(target)
                .registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE)
                .build();
    }
}

package yuuki1293.ae2peat.wireless;

import appeng.helpers.IPatternTerminalLogicHost;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.ISubMenu;
import appeng.parts.encoding.PatternEncodingLogic;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import yuuki1293.ae2peat.definisions.PEATItems;

import java.util.function.BiConsumer;

public class WPEATMenuHost extends WTMenuHost
    implements IPatternTerminalMenuHost, IPatternTerminalLogicHost {

    private final PatternEncodingLogic logic = new PatternEncodingLogic(this);

    public WPEATMenuHost(Player player, @Nullable Integer inventorySlot, ItemStack is, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(player, inventorySlot, is, returnToMainMenu);
        readFromNbt();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL.stack();
    }

    @Override
    protected void readFromNbt() {
        super.readFromNbt();
        logic.readFromNBT(getItemStack().getOrCreateTag());
    }

    @Override
    public void saveChanges() {
        super.saveChanges();
        logic.writeToNBT(getItemStack().getOrCreateTag());
    }

    @Override
    public PatternEncodingLogic getLogic() {
        return logic;
    }

    @Override
    public Level getLevel() {
        return getPlayer().level();
    }

    @Override
    public void markForSave() {
        saveChanges();
    }
}

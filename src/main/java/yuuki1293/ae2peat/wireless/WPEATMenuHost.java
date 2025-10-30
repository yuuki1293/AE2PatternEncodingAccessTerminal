package yuuki1293.ae2peat.wireless;

import appeng.api.networking.IGridNode;
import appeng.api.storage.IPatternAccessTermMenuHost;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.parts.encoding.PatternEncodingLogic;
import de.mari_023.ae2wtlib.api.AE2wtlibComponents;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WPEATMenuHost extends WTMenuHost
        implements IPatternTerminalMenuHost, IPatternTerminalLogicHost, IPatternAccessTermMenuHost {

    private final PatternEncodingLogic logic = new PatternEncodingLogic(this);

    public WPEATMenuHost(
            ItemWT item, Player player, ItemMenuHostLocator locator, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
        this.logic.readFromNBT(
                this.getItemStack().getOrDefault(AE2wtlibComponents.PATTERN_ENCODING_LOGIC, new CompoundTag()),
                player.registryAccess());
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
        CompoundTag tag =
                this.getItemStack().getOrDefault(AE2wtlibComponents.PATTERN_ENCODING_LOGIC, new CompoundTag());
        this.logic.writeToNBT(tag, this.getPlayer().registryAccess());
        this.getItemStack().set(AE2wtlibComponents.PATTERN_ENCODING_LOGIC, tag);
    }

    public @Nullable IGridNode getGridNode() {
        return this.getActionableNode();
    }
}

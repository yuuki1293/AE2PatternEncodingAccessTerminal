package yuuki1293.ae2peat.wireless;

import appeng.api.storage.ITerminalHost;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import de.mari_023.ae2wtlib.api.terminal.ItemWUT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.definisions.PEATIds;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

public class WPEATMenu extends PatternEncodingAccessTermMenu {
    public static final MenuType<WPEATMenu> TYPE = MenuTypeBuilder.create(WPEATMenu::new, WPEATMenuHost.class)
            .buildUnregistered(AE2PEAT.makeId(PEATIds.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL));

    private final WPEATMenuHost wpeatMenuHost;
    private final ToolboxMenu toolboxMenu;

    public WPEATMenu(int id, Inventory ip, WPEATMenuHost anchor) {
        super(TYPE, id, ip, anchor, true);
        wpeatMenuHost = anchor;
        toolboxMenu = new ToolboxMenu(this);
        IUpgradeInventory upgrades = this.wpeatMenuHost.getUpgrades();

        for (int i = 0; i < upgrades.size(); ++i) {
            RestrictedInputSlot slot =
                    new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, i);
            slot.setNotDraggable();
            this.addSlot(slot, SlotSemantics.UPGRADE);
        }

        this.addSlot(
                new RestrictedInputSlot(
                        RestrictedInputSlot.PlacableItemType.QE_SINGULARITY,
                        this.wpeatMenuHost.getSubInventory(WTMenuHost.INV_SINGULARITY),
                        0),
                de.mari_023.ae2wtlib.api.gui.AE2wtlibSlotSemantics.SINGULARITY);
    }

    @Override
    public void broadcastChanges() {
        toolboxMenu.tick();
        super.broadcastChanges();
    }

    public boolean isWUT() {
        return this.wpeatMenuHost.getItemStack().getItem() instanceof ItemWUT;
    }

    public ITerminalHost getHost() {
        return wpeatMenuHost;
    }

    public ToolboxMenu getToolbox() {
        return toolboxMenu;
    }
}

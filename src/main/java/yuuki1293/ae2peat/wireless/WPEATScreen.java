package yuuki1293.ae2peat.wireless;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToolboxPanel;
import de.mari_023.ae2wtlib.api.gui.ScrollingUpgradesPanel;
import de.mari_023.ae2wtlib.api.terminal.IUniversalTerminalCapable;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;

public class WPEATScreen extends PatternEncodingAccessTermScreen<WPEATMenu> implements IUniversalTerminalCapable {
    private final ScrollingUpgradesPanel upgradesPanel;

    public WPEATScreen(WPEATMenu container, Inventory playerInventory, Component title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        if (this.getMenu().isWUT()) {
            this.addToLeftToolbar(this.cycleTerminalButton());
        }

        this.upgradesPanel = this.addUpgradePanel(this.widgets, this.getMenu());
        if (this.getMenu().getToolbox().isPresent()) {
            this.widgets.add(
                    "toolbox",
                    new ToolboxPanel(style, this.getMenu().getToolbox().getName()));
        }
    }

    public void init() {
        super.init();
        this.upgradesPanel.setMaxRows(Math.max(2, this.getVisibleRows()));
    }

    public @NotNull WTMenuHost getHost() {
        return (WPEATMenuHost) this.getMenu().getHost();
    }

    public void storeState() {}

    public boolean keyPressed(int keyCode, int scanCode, int keyPressed) {
        boolean value = super.keyPressed(keyCode, scanCode, keyPressed);
        return !value ? this.checkForTerminalKeys(keyCode, scanCode) : true;
    }
}

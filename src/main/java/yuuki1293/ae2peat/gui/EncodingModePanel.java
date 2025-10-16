package yuuki1293.ae2peat.gui;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

public abstract class EncodingModePanel implements ICompositeWidget {
    protected final PatternEncodingAccessTermScreen<?> screen;
    protected final PatternEncodingAccessTermMenu menu;
    protected final WidgetContainer widgets;
    protected boolean visible = false;
    protected int x;
    protected int y;

    public EncodingModePanel(PatternEncodingAccessTermScreen<?> screen, WidgetContainer widgets) {
        this.screen = screen;
        this.menu = screen.getMenu();
        this.widgets = widgets;
    }

    abstract ItemStack getTabIconItem();

    abstract Component getTabTooltip();

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, 126, 68);
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

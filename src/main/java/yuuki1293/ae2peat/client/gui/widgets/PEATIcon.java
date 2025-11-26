package yuuki1293.ae2peat.client.gui.widgets;

import appeng.client.gui.style.Blitter;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.pedroksl.ae2addonlib.client.widgets.IBlitterIcon;
import yuuki1293.ae2peat.AE2PEAT;

public enum PEATIcon implements IBlitterIcon {
    ACCESS_SEARCH_MODE_BOTH(0, 0),
    ACCESS_SEARCH_MODE_PATTERN(16, 0),
    ACCESS_SEARCH_MODE_MACHINE(32, 0),
    AUTO_FILTER_DISABLED(48, 0),
    AUTO_FILTER_ENABLED(64, 0);

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public static final ResourceLocation TEXTURE = AE2PEAT.makeId("textures/guis/states.png");
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    PEATIcon(int x, int y) {
        this(x, y, 16, 16);
    }

    PEATIcon(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public Size getTextureSize() {
        return new Size(TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public Rect2i getRect() {
        return new Rect2i(x, y, width, height);
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT).src(x, y, width, height);
    }
}

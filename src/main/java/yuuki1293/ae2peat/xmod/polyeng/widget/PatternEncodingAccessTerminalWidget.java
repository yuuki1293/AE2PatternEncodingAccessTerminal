package yuuki1293.ae2peat.xmod.polyeng.widget;

import appeng.menu.SlotSemantics;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.client.gui.GuiGraphics;
import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

public class PatternEncodingAccessTerminalWidget<M extends PatternEncodingAccessTermMenu>
        extends BaseTerminalWidget<M, PatternEncodingAccessTermScreen<M>> {

    public PatternEncodingAccessTerminalWidget(PatternEncodingAccessTermScreen<M> screen) {
        super(screen, screen.getMenu().getSlots(SlotSemantics.CRAFTING_RESULT).getFirst());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float renderPartialTicks) {
        if (menu.mode == EncodingMode.CRAFTING) {
            super.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return menu.mode == EncodingMode.CRAFTING && super.mouseClicked(mouseX, mouseY, button);
    }
}

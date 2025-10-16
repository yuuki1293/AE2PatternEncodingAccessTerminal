package yuuki1293.ae2peat.gui;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import com.google.common.primitives.Longs;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

import java.util.function.Consumer;

public class SetProcessingPatternAmountScreen<C extends PatternEncodingAccessTermMenu>
    extends AESubScreen<C, PatternEncodingAccessTermScreen<C>> {

    private final NumberEntryWidget amount;

    private final GenericStack currentStack;

    private final Consumer<GenericStack> setter;

    public SetProcessingPatternAmountScreen(PatternEncodingAccessTermScreen<C> parentScreen,
                                            GenericStack currentStack,
                                            Consumer<GenericStack> setter) {
        super(parentScreen, "/screens/set_processing_pattern_amount.json");

        this.currentStack = currentStack;
        this.setter = setter;

        widgets.addButton("save", GuiText.Set.text(), this::confirm);

        var icon = getMenu().getHost().getMainMenuIcon();
        var button = new TabButton(icon, icon.getHoverName(), btn -> returnToParent());
        widgets.add("back", button);

        this.amount = widgets.addNumberEntryWidget("amountToStock", NumberEntryType.of(currentStack.what()));
        this.amount.setLongValue(currentStack.amount());
        this.amount.setMaxValue(getMaxAmount());
        this.amount.setTextFieldStyle(style.getWidget("amountToStockInput"));
        this.amount.setMinValue(0);
        this.amount.setHideValidationIcon(true);
        this.amount.setOnConfirm(this::confirm);

        addClientSideSlot(new ClientDisplaySlot(currentStack), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void init() {
        super.init();

        // The screen JSON includes the toolbox, but we don't actually have a need for it here
        setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private void confirm() {
        this.amount.getLongValue().ifPresent(newAmount -> {
            newAmount = Longs.constrainToRange(newAmount, 0, getMaxAmount());

            if (newAmount <= 0) {
                setter.accept(null);
            } else {
                setter.accept(new GenericStack(currentStack.what(), newAmount));
            }
            returnToParent();
        });
    }

    private long getMaxAmount() {
        return 999999 * (long) currentStack.what().getAmountPerUnit();
    }

}

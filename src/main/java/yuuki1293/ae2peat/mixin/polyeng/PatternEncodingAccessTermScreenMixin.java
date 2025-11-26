package yuuki1293.ae2peat.mixin.polyeng;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import gripe._90.polyeng.mixin.AbstractRecipesWidgetAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;

@Mixin(value = PatternEncodingAccessTermScreen.class, remap = false)
public class PatternEncodingAccessTermScreenMixin {
    @Inject(method = "toggleTerminalStyle", at = @At("RETURN"))
    private void moveWidget(CallbackInfo ci) {
        if (PolymorphWidgets.getInstance().getCurrentWidget() instanceof AbstractRecipesWidgetAccessor widget) {
            widget.callResetWidgetOffsets();
        }
    }
}

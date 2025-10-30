package yuuki1293.ae2peat.mixin;

import appeng.core.network.serverbound.QuickMovePatternPacket;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(value = QuickMovePatternPacket.class, remap = false)
public abstract class MixinQuickMovePatternPacket {
    @Final
    @Shadow
    private int containerId;

    @Final
    @Shadow
    private int clickedSlot;

    @Final
    @Shadow
    private List<Long> allowedPatternContainers;

    @Inject(method = "handleOnServer", at = @At("RETURN"))
    private void handleOnServer(ServerPlayer player, CallbackInfo ci) {
        if (player.containerMenu.containerId == containerId
                && player.containerMenu instanceof PatternEncodingAccessTermMenu menu) {
            menu.quickMovePattern(player, clickedSlot, allowedPatternContainers);
        }
    }
}

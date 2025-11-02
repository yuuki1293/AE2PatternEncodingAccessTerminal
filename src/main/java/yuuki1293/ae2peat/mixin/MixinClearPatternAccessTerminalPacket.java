package yuuki1293.ae2peat.mixin;

import appeng.core.network.clientbound.ClearPatternAccessTerminalPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;

@Mixin(value = ClearPatternAccessTerminalPacket.class, remap = false)
public abstract class MixinClearPatternAccessTerminalPacket {
    @Inject(method = "handleOnClient", at = @At("RETURN"))
    private void handleOnClient(Player player, CallbackInfo ci) {
        if (Minecraft.getInstance().screen
                instanceof PatternEncodingAccessTermScreen<?> patternEncodingAccessTerminal) {
            patternEncodingAccessTerminal.clear();
        }
    }
}

package yuuki1293.ae2peat.mixin;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.core.network.clientbound.PatternAccessTerminalPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;

@Mixin(value = PatternAccessTerminalPacket.class, remap = false)
public abstract class MixinPatternAccessTerminalPacket {
    @Final
    @Shadow
    private boolean fullUpdate;

    @Final
    @Shadow
    private long inventoryId;

    @Final
    @Shadow
    private int inventorySize;

    @Final
    @Shadow
    private long sortBy;

    @Final
    @Shadow
    private PatternContainerGroup group;

    @Final
    @Shadow
    private Int2ObjectMap<ItemStack> slots;

    @Inject(method = "handleOnClient", at = @At("RETURN"))
    private void handleOnClient(Player player, CallbackInfo ci) {
        if (Minecraft.getInstance().screen
                instanceof PatternEncodingAccessTermScreen<?> patternEncodingAccessTerminal) {
            if (fullUpdate) {
                patternEncodingAccessTerminal.postFullUpdate(this.inventoryId, sortBy, group, inventorySize, slots);
            } else {
                patternEncodingAccessTerminal.postIncrementalUpdate(this.inventoryId, slots);
            }
        }
    }
}

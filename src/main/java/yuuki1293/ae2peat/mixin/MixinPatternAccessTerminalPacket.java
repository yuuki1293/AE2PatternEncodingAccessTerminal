package yuuki1293.ae2peat.mixin;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.core.sync.packets.PatternAccessTerminalPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;

@Mixin(value = PatternAccessTerminalPacket.class, remap = false)
public abstract class MixinPatternAccessTerminalPacket {
    @Shadow
    private boolean fullUpdate;

    @Shadow
    private long inventoryId;

    @Shadow
    private int inventorySize;

    @Shadow
    private long sortBy;

    @Shadow
    private PatternContainerGroup group;

    @Shadow
    private Int2ObjectMap<ItemStack> slots;

    @Inject(method = "clientPacketData", at = @At("RETURN"))
    private void clientPacketData(Player player, CallbackInfo ci) {
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

package yuuki1293.ae2peat.mixin;

import appeng.core.AELog;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.menu.me.common.GridInventoryEntry;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(value = MEInventoryUpdatePacket.class, remap = false)
public abstract class MixinMEInventoryUpdatePacket {
    @Shadow
    @Final
    private List<GridInventoryEntry> entries;

    @Final
    @Shadow
    private boolean fullUpdate;

    @Final
    @Shadow
    private int containerId;

    @Inject(method = "handleOnClient", at = @At("TAIL"))
    private void handleOnClient(Player player, CallbackInfo ci) {
        if (player.containerMenu.containerId == containerId
                && player.containerMenu instanceof PatternEncodingAccessTermMenu meMenu) {
            var clientRepo = meMenu.getClientRepo();
            if (clientRepo == null) {
                AELog.info("Ignoring ME inventory update packet because no client repo is available.");
                return;
            }

            clientRepo.handleUpdate(fullUpdate, entries);
        }
    }
}

package yuuki1293.ae2peat.mixin.polyeng;

import appeng.api.storage.ITerminalHost;
import appeng.menu.AEBaseMenu;
import com.illusivesoulworks.polymorph.api.PolymorphApi;
import gripe._90.polyeng.PolymorphicEnergistics;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.menu.IPEATMenuHost;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin(value = PatternEncodingAccessTermMenu.class, remap = false)
public abstract class PatternEncodingAccessTermMenuMixin extends AEBaseMenu {
    @Shadow
    private RecipeHolder<CraftingRecipe> currentRecipe;

    public PatternEncodingAccessTermMenuMixin(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Shadow
    protected abstract ItemStack getAndUpdateOutput();

    // spotless:off
    @Inject(
        method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lyuuki1293/ae2peat/menu/IPEATMenuHost;Z)V",
        at = @At("RETURN"))
    // spotless:off
    private void registerAction(MenuType<?> menuType, int id, Inventory ip, IPEATMenuHost host, boolean bindInventory, CallbackInfo callbackInfo) {
        registerClientAction(PolymorphicEnergistics.ACTION, () -> {
            currentRecipe = null;
            getAndUpdateOutput();
        });
    }

    // spotless:off
    @Redirect(
        method = "getAndUpdateOutput",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    // spotless:on
    private <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> getRecipe(
            RecipeManager manager, RecipeType<R> type, I input, Level level) {
        return PolymorphApi.getInstance().getRecipeManager().getPlayerRecipe(this, type, input, level, getPlayer());
    }

    @Inject(method = "setItem", at = @At("HEAD"))
    private void resetRecipe(int slotID, int stateId, ItemStack stack, CallbackInfo ci) {
        currentRecipe = null;
    }
}

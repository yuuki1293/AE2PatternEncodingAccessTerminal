package yuuki1293.ae2peat.itemlists;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class REIAdapter implements IItemListsAdapter {

    private static final REIAdapter INSTANCE = new REIAdapter();

    public static REIAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull List<? extends ItemLike> machinesFromRecipeType(@NotNull Object category) {
        if (category instanceof CategoryIdentifier<?>categoryId) {
            var workstations = CategoryRegistry.getInstance()
                .get(categoryId)
                .getWorkstations();
            return workstations.stream()
                .map(List::getFirst)
                .filter(
                    x -> x.getValueType()
                        .equals(ItemStack.class))
                .map(
                    x -> x.<ItemStack>castValue()
                        .getItem())
                .toList();
        }

        return List.of();
    }
}

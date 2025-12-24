package yuuki1293.ae2peat.itemlists;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiRuntime;

public class JEIAdapter implements IItemListsAdapter {

    private final IJeiRuntime jeiRuntime;

    public JEIAdapter(IJeiRuntime jeiRuntime) {
        this.jeiRuntime = jeiRuntime;
    }

    @Override
    public @NotNull List<? extends ItemLike> machinesFromRecipeType(@NotNull Object category) {
        if (category instanceof ResourceLocation recipeId) {
            var recipeType = jeiRuntime.getRecipeManager()
                .getRecipeType(recipeId);
            if (recipeType.isEmpty()) return List.of();

            var catalyst = jeiRuntime.getRecipeManager()
                .createRecipeCatalystLookup(recipeType.get());

            return catalyst.get()
                .map(ITypedIngredient::getItemStack)
                .flatMap(Optional::stream)
                .map(ItemStack::getItem)
                .toList();
        }

        return List.of();
    }
}

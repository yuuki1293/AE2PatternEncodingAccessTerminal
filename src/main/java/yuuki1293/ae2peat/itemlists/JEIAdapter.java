package yuuki1293.ae2peat.itemlists;

import java.util.List;
import java.util.Optional;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class JEIAdapter implements IItemListsAdapter {
    private final IJeiRuntime jeiRuntime;

    public JEIAdapter(IJeiRuntime jeiRuntime) {
        this.jeiRuntime = jeiRuntime;
    }

    @Override
    public @NotNull List<? extends ItemLike> machinesFromRecipeType(@NotNull ResourceLocation category) {
        var recipeType = jeiRuntime.getRecipeManager().getRecipeType(category);
        if (recipeType.isEmpty()) return List.of();

        var catalyst = jeiRuntime.getRecipeManager().createRecipeCatalystLookup(recipeType.get());

        return catalyst.get()
                .map(ITypedIngredient::getItemStack)
                .flatMap(Optional::stream)
                .map(ItemStack::getItem)
                .toList();
    }
}

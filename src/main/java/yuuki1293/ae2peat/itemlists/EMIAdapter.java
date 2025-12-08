package yuuki1293.ae2peat.itemlists;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class EMIAdapter implements IItemListsAdapter {
    private static final EMIAdapter INSTANCE = new EMIAdapter();

    public static EMIAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull List<? extends ItemLike> machinesFromRecipeType(@NotNull Object category) {
        if (category instanceof EmiRecipeCategory emiCategory) {
            var workstations = EmiApi.getRecipeManager().getWorkstations(emiCategory);

            return workstations.stream()
                    .map(x -> x.getEmiStacks().getFirst().getKeyOfType(Item.class))
                    .filter(Objects::nonNull)
                    .toList();
        }

        return List.of();
    }
}

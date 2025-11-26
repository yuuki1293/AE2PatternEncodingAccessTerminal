package yuuki1293.ae2peat.itemlists;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

/**
 * Interface that standardizes JEI/REI/EMI processing
 */
public interface IItemListsAdapter {
    /**
     * Returns a list of machines corresponding to the input RecipeType.
     * JEI returns the item that appears on the left when the recipe is opened.
     * @param recipeType RecipeType (such as "ae2:inscriber")
     * @return item list. include non block.
     */
    @NotNull
    List<? extends ItemLike> machinesFromRecipeType(@NotNull ResourceLocation recipeType);

    default Optional<PatternContainerGroup> findFirst(List<PatternContainerGroup> groups, ResourceLocation recipeType) {
        var machines = machinesFromRecipeType(recipeType);

        if (machines.isEmpty()) return Optional.empty();

        // if group name is same as recipe id, return early.
        for (var group : groups) {
            if (group.name().getString().equals(recipeType.toString())) {
                return Optional.of(group);
            }
        }

        return findFirst(groups, machines);
    }

    /**
     * Returns the first element from the list of {@link PatternContainerGroup} that corresponds to the list of machines.
     */
    default Optional<PatternContainerGroup> findFirst(
            List<PatternContainerGroup> groups, List<? extends ItemLike> machines) {
        var localizedNames = machines.stream()
                .map(m -> m.asItem()
                        .getDefaultInstance()
                        .getHoverName()
                        .getString()
                        .toLowerCase())
                .toList();

        for (var group : groups) {
            var searchName = group.name().getString().toLowerCase();

            var matched =
                    localizedNames.stream().filter(m -> m.equals(searchName)).findFirst();

            if (matched.isPresent()) {
                return Optional.of(group);
            }
        }

        return Optional.empty();
    }
}

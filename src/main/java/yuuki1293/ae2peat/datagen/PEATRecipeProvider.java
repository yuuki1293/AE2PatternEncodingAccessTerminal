package yuuki1293.ae2peat.datagen;

import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import org.jetbrains.annotations.NotNull;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.definisions.PEATItems;

public class PEATRecipeProvider extends RecipeProvider {
    public PEATRecipeProvider(PackOutput p, CompletableFuture<HolderLookup.Provider> provider) {
        super(p);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> c) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL)
                .requires(AEParts.PATTERN_ACCESS_TERMINAL)
                .requires(AEParts.PATTERN_ENCODING_TERMINAL)
                .unlockedBy("has_pattern_provider", has(ConventionTags.PATTERN_PROVIDER))
                .unlockedBy("has_crafting_terminal", has(AEParts.CRAFTING_TERMINAL))
                .save(c, AE2PEAT.makeId("network/parts/terminals_pattern_encoding"));
    }
}

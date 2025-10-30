package yuuki1293.ae2peat.datagen;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import org.jetbrains.annotations.NotNull;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.xmod.Addons;

public class PEATRecipeProvider extends RecipeProvider {
    public PEATRecipeProvider(PackOutput p, CompletableFuture<HolderLookup.Provider> provider) {
        super(p, provider);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    protected void buildRecipes(@NotNull RecipeOutput c) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL)
                .requires(AEParts.PATTERN_ACCESS_TERMINAL)
                .requires(AEParts.PATTERN_ENCODING_TERMINAL)
                .unlockedBy("has_pattern_provider", has(ConventionTags.PATTERN_PROVIDER))
                .unlockedBy("has_crafting_terminal", has(AEParts.CRAFTING_TERMINAL))
                .save(c, AE2PEAT.makeId("network/parts/pattern_encoding_access_terminal"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL)
                .pattern("a")
                .pattern("b")
                .pattern("c")
                .define('a', AEItems.WIRELESS_RECEIVER)
                .define('b', PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL)
                .define('c', AEBlocks.DENSE_ENERGY_CELL)
                .unlockedBy("has_peat_terminal", has(PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL))
                .unlockedBy("has_dense_energy_cell", has(AEBlocks.DENSE_ENERGY_CELL))
                .unlockedBy("has_wireless_receiver", has(AEItems.WIRELESS_RECEIVER))
                .save(
                        Addons.AE2WTLIB.conditionalRecipe(c),
                        AE2PEAT.makeId("network/wireless_pattern_encoding_access_terminal"));
    }
}

package yuuki1293.ae2peat.definisions;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.items.AEBaseItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import yuuki1293.ae2peat.AE2PEAT;

import java.util.ArrayList;

public class PEATCreativeTab {
    public static final DeferredRegister<CreativeModeTab> DR = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AE2PEAT.MODID);

    static {
        DR.register("tab", () -> CreativeModeTab.builder()
            .title(PEATText.ModName.text())
            .icon(PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL::stack)
            .displayItems(PEATCreativeTab::populateTab)
            .build());
    }

    private static void populateTab(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        var itemDefs = new ArrayList<>(PEATItems.getItems());

        for (var itemDef : itemDefs) {
            var item = itemDef.asItem();

            // For block items, the block controls the creative tab
            if (item instanceof AEBaseBlockItem baseItem && baseItem.getBlock() instanceof AEBaseBlock baseBlock) {
                baseBlock.addToMainCreativeTab(output);
            } else if (item instanceof AEBaseItem baseItem) {
                baseItem.addToMainCreativeTab(output);
            } else {
                output.accept(itemDef);
            }
        }
    }
}

package yuuki1293.ae2peat.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import yuuki1293.ae2peat.PEATItems;

public final class InitItems {
    private InitItems() {
    }

    public static void init(IForgeRegistry<Item> registry) {
        for (var definition : PEATItems.getItems()) {
            registry.register(definition.id(), definition.asItem());
        }
    }
}

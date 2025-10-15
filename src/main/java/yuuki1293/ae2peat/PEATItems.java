package yuuki1293.ae2peat;

import appeng.api.ids.AECreativeTabIds;
import appeng.core.MainCreativeTab;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PEATItems {
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
                                                   Function<Item.Properties, T> factory) {
        return item(name, id, factory, AECreativeTabIds.MAIN);
    }

    static <T extends Item> ItemDefinition<T> item(String name, ResourceLocation id,
                                                   Function<Item.Properties, T> factory,
                                                   ResourceKey<CreativeModeTab> group) {

        Item.Properties p = new Item.Properties();

        T item = factory.apply(p);

        ItemDefinition<T> definition = new ItemDefinition<>(name, id, item);

        if (group.equals(AECreativeTabIds.MAIN)) {
            MainCreativeTab.add(definition);
        } else {
            MainCreativeTab.addExternal(group, definition);
        }

        ITEMS.add(definition);

        return definition;
    }
}

package yuuki1293.ae2peat.definisions;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.pedroksl.ae2addonlib.registry.ItemRegistry;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.parts.PatternEncodingAccessTerminalPart;
import yuuki1293.ae2peat.wireless.ItemWPEAT;
import yuuki1293.ae2peat.xmod.ae2wtlib.AE2WtLibPlugin;

public class PEATItems extends ItemRegistry {
    public static PEATItems INSTANCE = new PEATItems();

    PEATItems() {
        super(AE2PEAT.MOD_ID);
    }

    public static final ItemDefinition<PartItem<PatternEncodingAccessTerminalPart>> PATTERN_ENCODING_ACCESS_TERMINAL =
            part(
                    "ME Pattern Encoding Access Terminal",
                    PEATIds.PATTERN_ENCODING_ACCESS_TERMINAL,
                    PatternEncodingAccessTerminalPart.class,
                    PatternEncodingAccessTerminalPart::new);
    public static final ItemDefinition<ItemWPEAT> WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL = item(
            "Wireless Pattern Encoding Access Terminal",
            PEATIds.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL,
            p -> AE2WtLibPlugin.TERMINAL);

    protected static <T extends Item> ItemDefinition<T> item(
            String englishName, String id, Function<Item.Properties, T> factory) {
        return item(AE2PEAT.MOD_ID, englishName, id, factory);
    }

    protected static <T extends IPart> ItemDefinition<PartItem<T>> part(
            String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        return part(AE2PEAT.MOD_ID, englishName, id, partClass, factory);
    }
}

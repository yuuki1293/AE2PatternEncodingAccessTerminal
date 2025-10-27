package yuuki1293.ae2peat.definisions;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.items.parts.PartItem;
import net.minecraft.world.item.Item;
import net.pedroksl.ae2addonlib.registry.ItemRegistry;
import net.pedroksl.ae2addonlib.registry.helpers.LibItemDefinition;
import net.pedroksl.ae2addonlib.util.AddonEnum;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.wireless.ItemWPEAT;
import yuuki1293.ae2peat.parts.PatternEncodingAccessTerminalPart;
import yuuki1293.ae2peat.xmod.Addons;

import java.util.function.Function;

public class PEATItems extends ItemRegistry {
    public static PEATItems INSTANCE = new PEATItems();

    PEATItems() {
        super(AE2PEAT.MOD_ID);
    }

    public static final LibItemDefinition<PartItem<PatternEncodingAccessTerminalPart>> PATTERN_ENCODING_ACCESS_TERMINAL = part(
        "ME Pattern Encoding Access Terminal",
        PEATIds.PATTERN_ENCODING_ACCESS_TERMINAL,
        PatternEncodingAccessTerminalPart.class,
        PatternEncodingAccessTerminalPart::new
    );
    public static final LibItemDefinition<ItemWPEAT> WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL = conditionalItem(
        Addons.AE2WTLIB,
        "Wireless Pattern Encoding Access Terminal",
        PEATIds.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL,
        "yuuki1293.ae2peat.wireless.ItemWPEAT"
    );

    @SuppressWarnings("unchecked")
    private static <T extends Item> LibItemDefinition<T> conditionalItem(
        AddonEnum addon, String englishName, String id, String itemClass) {
        if (addon.isLoaded()) {
            try {
                var instance =
                    (T) Class.forName(itemClass).getDeclaredConstructor().newInstance();
                return item(AE2PEAT.MOD_ID, englishName, id, p -> instance);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    protected static <T extends Item> LibItemDefinition<T> item(
        String englishName, String id, Function<Item.Properties, T> factory) {
        return item(AE2PEAT.MOD_ID, englishName, id, factory);
    }

    protected static <T extends IPart> LibItemDefinition<PartItem<T>> part(
        String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        return part(AE2PEAT.MOD_ID, englishName, id, partClass, factory);
    }
}

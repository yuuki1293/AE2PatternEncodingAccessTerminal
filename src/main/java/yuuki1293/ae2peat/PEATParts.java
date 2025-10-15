package yuuki1293.ae2peat;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.resources.ResourceLocation;
import yuuki1293.ae2peat.parts.PatternEncodingAccessTerminalPart;

import java.util.function.Function;

import static yuuki1293.ae2peat.PEATItems.item;

public class PEATParts {
    public static final ItemDefinition<PartItem<PatternEncodingAccessTerminalPart>> PATTERN_ENCODING_ACCESS_TERMINAL = createPart("ME Pattern Encoding Access Terminal", PEATPartIds.PATTERN_ENCODING_ACCESS_TERMINAL, PatternEncodingAccessTerminalPart.class, PatternEncodingAccessTerminalPart::new);

    private static <T extends IPart> ItemDefinition<PartItem<T>> createPart(
        String englishName,
        ResourceLocation id,
        Class<T> partClass,
        Function<IPartItem<T>, T> factory) {

        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, props -> new PartItem<>(props, partClass, factory));
    }

    public static void init() {
    }
}

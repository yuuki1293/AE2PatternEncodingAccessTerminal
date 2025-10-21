package yuuki1293.ae2peat.definisions;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.parts.PatternEncodingAccessTerminalPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PEATItems {
    public static final DeferredRegister<Item> DR = DeferredRegister.create(ForgeRegistries.ITEMS, AE2PEAT.MODID);

    private static final List<PEATItemDefinition<?>> ITEMS = new ArrayList<>();

    public static List<PEATItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static final PEATItemDefinition<PartItem<PatternEncodingAccessTerminalPart>> PATTERN_ENCODING_ACCESS_TERMINAL = part(
        "ME Pattern Encoding Access Terminal", PEATPartIds.PATTERN_ENCODING_ACCESS_TERMINAL, PatternEncodingAccessTerminalPart.class, PatternEncodingAccessTerminalPart::new
    );

    private static <T extends Item> PEATItemDefinition<T> item(
        String englishName, String id, Function<Item.Properties, T> factory) {
        PEATItemDefinition<T> definition =
            new PEATItemDefinition<>(englishName, DR.register(id, () -> factory.apply(new Item.Properties())));
        ITEMS.add(definition);
        return definition;
    }

    private static <T extends IPart> PEATItemDefinition<PartItem<T>> part(
        String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, p -> new PartItem<>(p, partClass, factory));
    }
}

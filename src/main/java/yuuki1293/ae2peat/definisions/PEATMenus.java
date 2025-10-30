package yuuki1293.ae2peat.definisions;

import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.pedroksl.ae2addonlib.registry.MenuRegistry;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.parts.PatternEncodingAccessTerminalPart;
import yuuki1293.ae2peat.wireless.WPEATMenu;

public class PEATMenus extends MenuRegistry {
    public static final PEATMenus INSTANCE = new PEATMenus();

    public PEATMenus() {
        super(AE2PEAT.MOD_ID);
    }

    public static final Supplier<MenuType<PatternEncodingAccessTermMenu>> PATTERN_ENCODING_ACCESS_TERMINAL = create(
            "pattern_encoding_access_terminal",
            PatternEncodingAccessTermMenu::new,
            PatternEncodingAccessTerminalPart.class);
    public static final Supplier<MenuType<WPEATMenu>> WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL =
            create("wireless_pattern_encoding_access_terminal", () -> WPEATMenu.TYPE);

    private static <M extends AEBaseMenu, H> Supplier<MenuType<M>> create(
            String id, MenuTypeBuilder.MenuFactory<M, H> factory, Class<H> host) {
        return create(AE2PEAT.MOD_ID, id, factory, host);
    }

    private static <T extends AEBaseMenu> Supplier<MenuType<T>> create(String id, Supplier<MenuType<T>> supplier) {
        return create(AE2PEAT.MOD_ID, id, supplier);
    }
}

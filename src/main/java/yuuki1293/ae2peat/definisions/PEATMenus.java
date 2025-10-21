package yuuki1293.ae2peat.definisions;

import appeng.core.AppEng;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import yuuki1293.ae2peat.parts.PatternEncodingAccessTerminalPart;

import java.util.function.Supplier;

public class PEATMenus {
    public static final DeferredRegister<MenuType<?>> DR = DeferredRegister.create(Registries.MENU, AppEng.MOD_ID);

    public static final Supplier<MenuType<PatternEncodingAccessTermMenu>> PATTERN_ENCODING_ACCESS_TERMINAL =
        create("pattern_encoding_access_terminal", PatternEncodingAccessTermMenu::new, PatternEncodingAccessTerminalPart.class);

    private static <M extends AEBaseMenu, H> Supplier<MenuType<M>> create(
        String id, MenuTypeBuilder.MenuFactory<M, H> factory, Class<H> host) {
        return DR.register(
            id, () -> MenuTypeBuilder.create(factory, host).build(id));
    }
}

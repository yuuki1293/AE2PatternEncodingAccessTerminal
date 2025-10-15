package yuuki1293.ae2peat.init;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.AEBaseMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import yuuki1293.ae2peat.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

public class InitScreens {
    public static void init() {
        InitScreens.<PatternEncodingAccessTermMenu, PatternEncodingAccessTermScreen<PatternEncodingAccessTermMenu>>register(
            PatternEncodingAccessTermMenu.TYPE,
            PatternEncodingAccessTermScreen::new,
            "/screens/terminals/pattern_encoding_access_terminal.json"
        );
    }

    /**
     * Registers a screen for a given menu and ensures the given style is applied after opening the screen.
     */
    public static <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(MenuType<M> type,
                                                                                  appeng.init.client.InitScreens.StyledScreenFactory<M, U> factory,
                                                                                  String stylePath) {
        MenuScreens.<M, U>register(type, (menu, playerInv, title) -> {
            var style = StyleManager.loadStyleDoc(stylePath);

            return factory.create(menu, playerInv, title, style);
        });
    }
}

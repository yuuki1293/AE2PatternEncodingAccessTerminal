package yuuki1293.ae2peat.xmod.ae2wtlib;

import appeng.api.features.GridLinkables;
import appeng.init.client.InitScreens;
import de.mari_023.ae2wtlib.api.gui.Icon;
import de.mari_023.ae2wtlib.api.registration.AddTerminalEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.pedroksl.ae2addonlib.api.IGridLinkedItem;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.wireless.WPEATMenu;
import yuuki1293.ae2peat.wireless.WPEATMenuHost;
import yuuki1293.ae2peat.wireless.WPEATScreen;

public class AE2WtLibPlugin {
    public static final String TERMINAL_ID = "pattern_encoding_access";

    static {
        AddTerminalEvent.register(e -> e.builder(
                        TERMINAL_ID,
                        WPEATMenuHost::new,
                        WPEATMenu.TYPE,
                        PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                        Icon.PATTERN_ACCESS)
                .addTerminal());
    }

    public static void initUpgrades() {}

    public static void initGridLinkables() {
        GridLinkables.register(PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL, IGridLinkedItem.LINKABLE_HANDLER);
    }

    public static void initMenu() {}

    public static void initScreen(RegisterMenuScreensEvent event) {
        InitScreens.<WPEATMenu, WPEATScreen>register(
                event,
                WPEATMenu.TYPE,
                WPEATScreen::new,
                "/screens/wtlib/wireless_pattern_encoding_access_terminal.json");
    }
}

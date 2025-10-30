package yuuki1293.ae2peat.xmod.ae2wtlib;

import de.mari_023.ae2wtlib.api.gui.Icon;
import de.mari_023.ae2wtlib.api.registration.AddTerminalEvent;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.wireless.WPEATMenu;
import yuuki1293.ae2peat.wireless.WPEATMenuHost;

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
}

package yuuki1293.ae2peat.xmod.ae2wtlib;

import appeng.api.features.GridLinkables;
import appeng.api.upgrades.Upgrades;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.init.client.InitScreens;
import de.mari_023.ae2wtlib.AE2wtlib;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.pedroksl.ae2addonlib.api.IGridLinkedItem;
import yuuki1293.ae2peat.definisions.PEATIds;
import yuuki1293.ae2peat.definisions.PEATItems;
import yuuki1293.ae2peat.wireless.WPEATMenu;
import yuuki1293.ae2peat.wireless.WPEATMenuHost;
import yuuki1293.ae2peat.wireless.WPEATScreen;

public class AE2WtLibPlugin {
    public static final String TERMINAL_ID = "pattern_encoding_access";

    public static void initMenuType() {
        WUTHandler.addTerminal(
                TERMINAL_ID,
                PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL.get()::tryOpen,
                WPEATMenuHost::new,
                WPEATMenu.TYPE,
                PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL.get(),
                PEATIds.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL,
                PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL.asItem().getDescriptionId());
    }

    public static void initUpgrades() {
        if (PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL != null) {
            Upgrades.add(
                    AE2wtlib.QUANTUM_BRIDGE_CARD,
                    PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL,
                    1,
                    PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL.asItem().getDescriptionId());
            Upgrades.add(
                    AEItems.ENERGY_CARD,
                    PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL,
                    2,
                    PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL.asItem().getDescriptionId());
        }
    }

    public static void initGridLinkables() {
        GridLinkables.register(PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL, IGridLinkedItem.LINKABLE_HANDLER);
    }

    public static void initMenu() {
        ForgeRegistries.MENU_TYPES.register(
                AppEng.makeId(PEATIds.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL), WPEATMenu.TYPE);
    }

    public static void initScreen() {
        InitScreens.<WPEATMenu, WPEATScreen>register(
                WPEATMenu.TYPE, WPEATScreen::new, "/screens/wtlib/wireless_pattern_encoding_access_terminal.json");
    }
}

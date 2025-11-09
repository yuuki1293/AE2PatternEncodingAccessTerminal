package yuuki1293.ae2peat.itemlists;

import yuuki1293.ae2peat.integration.modules.jei.JEIPlugin;
import yuuki1293.ae2peat.xmod.Addons;

public class ItemListsManager {
    private static IItemListsAdapter adapter;

    static {
        if (Addons.JEI.isLoaded()) {
            adapter = JEIPlugin.getAdapter();
        } else if (Addons.REI.isLoaded()) {

        } else if (Addons.EMI.isLoaded()) {

        }
    }

    public static IItemListsAdapter getAdapter() {
        return adapter;
    }
}

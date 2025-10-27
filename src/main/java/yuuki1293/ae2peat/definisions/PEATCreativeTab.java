package yuuki1293.ae2peat.definisions;

import net.pedroksl.ae2addonlib.registry.CreativeTabRegistry;
import yuuki1293.ae2peat.AE2PEAT;

public class PEATCreativeTab extends CreativeTabRegistry {
    public static final PEATCreativeTab INSTANCE = new PEATCreativeTab();

    public PEATCreativeTab() {
        super(AE2PEAT.MOD_ID, PEATText.ModName.text(), PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL::stack);
    }
}

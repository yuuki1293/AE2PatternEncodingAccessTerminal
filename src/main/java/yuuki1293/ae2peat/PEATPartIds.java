package yuuki1293.ae2peat;

import appeng.api.ids.AEConstants;
import net.minecraft.resources.ResourceLocation;

public class PEATPartIds {
    public static final ResourceLocation PATTERN_ENCODING_ACCESS_TERMINAL = id("pattern_encoding_access_terminal");

    private static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(AEConstants.MOD_ID, id);
    }
}

package yuuki1293.ae2peat.init;

import appeng.core.AppEng;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.IForgeRegistry;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

public class InitMenuTypes {
    public static void init(IForgeRegistry<MenuType<?>> registry) {
        registry.register(AppEng.makeId("pattern_encoding_access_terminal"), PatternEncodingAccessTermMenu.TYPE);
    }
}

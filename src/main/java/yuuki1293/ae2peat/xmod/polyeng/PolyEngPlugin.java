package yuuki1293.ae2peat.xmod.polyeng;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import net.neoforged.fml.loading.FMLEnvironment;
import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.xmod.polyeng.widget.PatternEncodingAccessTerminalWidget;

public class PolyEngPlugin {
    public static void init() {
        if (FMLEnvironment.dist.isClient()) {
            PolymorphWidgets.getInstance().registerWidget(screen -> {
                if (screen instanceof PatternEncodingAccessTermScreen<?> peat) {
                    return new PatternEncodingAccessTerminalWidget<>(peat);
                }

                return null;
            });
        }
    }
}

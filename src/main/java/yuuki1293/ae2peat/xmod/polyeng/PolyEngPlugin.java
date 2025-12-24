package yuuki1293.ae2peat.xmod.polyeng;

import net.neoforged.fml.loading.FMLEnvironment;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;

import yuuki1293.ae2peat.client.gui.PatternEncodingAccessTermScreen;
import yuuki1293.ae2peat.xmod.polyeng.widget.PatternEncodingAccessTerminalWidget;

public class PolyEngPlugin {

    public static void init() {
        if (FMLEnvironment.dist.isClient()) {
            PolymorphWidgets.getInstance()
                .registerWidget(screen -> {
                    if (screen instanceof PatternEncodingAccessTermScreen<?>peat) {
                        return new PatternEncodingAccessTerminalWidget<>(peat);
                    }

                    return null;
                });
        }
    }
}

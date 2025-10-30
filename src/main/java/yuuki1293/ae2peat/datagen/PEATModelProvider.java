package yuuki1293.ae2peat.datagen;

import appeng.core.AppEng;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.pedroksl.ae2addonlib.datagen.AE2AddonModelProvider;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.definisions.PEATItems;

public class PEATModelProvider extends AE2AddonModelProvider {
    public PEATModelProvider(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, AE2PEAT.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        terminalPart(PEATItems.PATTERN_ENCODING_ACCESS_TERMINAL);
        basicItem(PEATItems.WIRELESS_PATTERN_ENCODING_ACCESS_TERMINAL);
    }

    private void terminalPart(ItemDefinition<?> part) {
        var id = part.id().getPath();
        var idOff = id + "_off";
        var idOn = id + "_on";

        var front = AE2PEAT.makeId("part/" + id);
        var bright = AE2PEAT.makeId("part/" + id + "_bright");
        var medium = AE2PEAT.makeId("part/" + id + "_medium");
        var dark = AE2PEAT.makeId("part/" + id + "_dark");

        var partOffBase = AppEng.makeId("part/display_off");
        var partOnBase = AppEng.makeId("part/pattern_access_terminal_on");
        var itemBase = AppEng.makeId("item/display_base");

        models().withExistingParent("part/" + idOff, partOffBase)
                .texture("lightsBright", bright)
                .texture("lightsMedium", medium)
                .texture("lightsDark", dark);
        models().withExistingParent("part/" + idOn, partOnBase)
                .texture("lightsBright", bright)
                .texture("lightsMedium", medium)
                .texture("lightsDark", dark);
        itemModels()
                .withExistingParent("item/" + id, itemBase)
                .texture("front", front)
                .texture("front_bright", bright)
                .texture("front_medium", medium)
                .texture("front_dark", dark);
    }
}

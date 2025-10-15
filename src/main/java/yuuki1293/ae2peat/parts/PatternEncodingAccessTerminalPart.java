package yuuki1293.ae2peat.parts;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.util.ConfigManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

public class PatternEncodingAccessTerminalPart extends AbstractDisplayPart implements IConfigurableObject {
    @PartModels
    public static final ResourceLocation MODEL_OFF = ResourceLocation.fromNamespaceAndPath(AE2PEAT.MODID,
        "part/pattern_encoding_access_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = ResourceLocation.fromNamespaceAndPath(AE2PEAT.MODID,
        "part/pattern_encoding_access_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final ConfigManager configManager = new ConfigManager(() -> this.getHost().markForSave());

    public PatternEncodingAccessTerminalPart(IPartItem<?> partItem){
        super(partItem, true);
        this.configManager.registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos){
        if(!super.onPartActivate(player, hand, pos) && !isClientSide()){
            MenuOpener.open(PatternEncodingAccessTermMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public IPartModel getStaticModels(){
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    public void writeToNBT(CompoundTag tag){
        super.writeToNBT(tag);
        configManager.writeToNBT(tag);
    }

    public void readFromNBT(CompoundTag tag){
        super.readFromNBT(tag);
        configManager.readFromNBT(tag);
    }
}

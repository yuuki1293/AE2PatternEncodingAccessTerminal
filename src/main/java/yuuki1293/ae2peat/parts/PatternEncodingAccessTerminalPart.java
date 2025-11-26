package yuuki1293.ae2peat.parts;

import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.api.storage.SupplierStorage;
import appeng.api.util.IConfigManager;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.parts.reporting.AbstractDisplayPart;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yuuki1293.ae2peat.AE2PEAT;
import yuuki1293.ae2peat.api.config.AccessSearchMode;
import yuuki1293.ae2peat.api.config.AutoFilter;
import yuuki1293.ae2peat.api.config.PEATSettings;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.menu.IPEATMenuHost;

public class PatternEncodingAccessTerminalPart extends AbstractDisplayPart
        implements IPatternTerminalLogicHost, IPEATMenuHost {
    private final IConfigManager cm = IConfigManager.builder(
                    () -> this.getHost().markForSave())
            .registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE)
            .registerSetting(PEATSettings.ACCESS_SEARCH_MODE, AccessSearchMode.BOTH)
            .registerSetting(PEATSettings.AUTO_FILTER, AutoFilter.DISABLED)
            .build();

    @PartModels
    public static final ResourceLocation MODEL_OFF = AE2PEAT.makeId("part/pattern_encoding_access_terminal_off");

    @PartModels
    public static final ResourceLocation MODEL_ON = AE2PEAT.makeId("part/pattern_encoding_access_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final PatternEncodingLogic logic = new PatternEncodingLogic(this);

    public PatternEncodingAccessTerminalPart(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.cm.readFromNBT(data, registries);
        this.logic.readFromNBT(data, registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        this.cm.writeToNBT(data, registries);
        this.logic.writeToNBT(data, registries);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!super.onUseWithoutItem(player, pos) && !player.level().isClientSide) {
            MenuOpener.open(getMenuType(player), player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.open(getMenuType(player), player, subMenu.getLocator(), true);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getPartItem());
    }

    public MenuType<?> getMenuType(Player player) {
        return PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get();
    }

    @Override
    public MEStorage getInventory() {
        return new SupplierStorage(() -> {
            var grid = getMainNode().getGrid();
            if (grid != null) {
                return grid.getStorageService().getInventory();
            }
            return null;
        });
    }

    @Override
    public ILinkStatus getLinkStatus() {
        return ILinkStatus.ofManagedNode(getMainNode());
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        for (var is : this.logic.getBlankPatternInv()) {
            drops.add(is);
        }
        for (var is : this.logic.getEncodedPatternInv()) {
            drops.add(is);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.logic.getBlankPatternInv().clear();
        this.logic.getEncodedPatternInv().clear();
    }

    @Override
    public PatternEncodingLogic getLogic() {
        return logic;
    }

    @Override
    public void markForSave() {
        getHost().markForSave();
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}

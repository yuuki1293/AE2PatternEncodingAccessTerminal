package yuuki1293.ae2peat.menu;

import appeng.api.config.*;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.IPatternAccessTermMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.util.*;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.client.gui.Icon;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.bidirectional.ConfigValuePacket;
import appeng.core.network.clientbound.ClearPatternAccessTerminalPacket;
import appeng.core.network.clientbound.MEInventoryUpdatePacket;
import appeng.core.network.clientbound.PatternAccessTerminalPacket;
import appeng.core.network.clientbound.SetLinkStatusPacket;
import appeng.core.network.serverbound.MEInteractionPacket;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.helpers.InventoryAction;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.me.helpers.ActionHostEnergySource;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.guisync.LinkStatusAwareMenu;
import appeng.menu.interfaces.KeyTypeSelectionMenu;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.IMEInteractionHandler;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.PatternTermSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yuuki1293.ae2peat.definisions.PEATMenus;
import yuuki1293.ae2peat.parts.PatternEncodingAccessTerminalPart;

public class PatternEncodingAccessTermMenu extends AEBaseMenu
        implements IConfigManagerListener, IConfigurableObject, IMEInteractionHandler, LinkStatusAwareMenu {

    // region me storage menu
    private final IConfigManager clientCM;
    private final ITerminalHost termHost;
    /**
     * The number of active crafting jobs in the network. -1 means unknown and will hide the label on the screen.
     */
    @GuiSync(100)
    public int activeCraftingJobs = -1;

    private static final short SEARCH_KEY_TYPES_ID = 101;

    @GuiSync(SEARCH_KEY_TYPES_ID)
    public KeyTypeSelectionMenu.SyncedKeyTypes searchKeyTypes = new KeyTypeSelectionMenu.SyncedKeyTypes();

    // Client-side: last status received from server
    // Server-side: last status sent to client
    private ILinkStatus linkStatus = ILinkStatus.ofDisconnected(null);

    @Nullable
    private Runnable gui;

    private IConfigManager serverCM;

    protected final MEStorage storage;

    protected final IEnergySource energySource;

    private final IncrementalUpdateHelper updateHelper = new IncrementalUpdateHelper();

    /**
     * The repository of entries currently known on the client-side. This is maintained by the screen associated with
     * this menu and will only be non-null on the client-side.
     */
    @Nullable
    private IClientRepo clientRepo;

    /**
     * The last set of craftables sent to the client.
     */
    private Set<AEKey> previousCraftables = Collections.emptySet();

    private KeyCounter previousAvailableStacks = new KeyCounter();

    // endregion
    // region access term
    private IPatternAccessTermMenuHost accessHost;

    @GuiSync(1)
    public ShowPatternProviders showPatternProviders = ShowPatternProviders.VISIBLE;

    public ShowPatternProviders getShownProviders() {
        return showPatternProviders;
    }

    // We use this serial number to uniquely identify all inventories we send to the client
    // It is used in packets sent by the client to interact with these inventories
    private static long inventorySerial = Long.MIN_VALUE;
    private final Map<PatternContainer, PatternEncodingAccessTermMenu.ContainerTracker> diList =
            new IdentityHashMap<>();
    private final Long2ObjectOpenHashMap<PatternEncodingAccessTermMenu.ContainerTracker> byId =
            new Long2ObjectOpenHashMap<>();
    /**
     * Tracks hosts that were visible before, even if they no longer match the filter. For
     * {@link ShowPatternProviders#NOT_FULL}.
     */
    private final Set<PatternContainer> pinnedHosts = Collections.newSetFromMap(new IdentityHashMap<>());

    // endregion
    // region encoding term
    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    private static final int CRAFTING_GRID_SLOTS = CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT;

    private static final String ACTION_SET_MODE = "setMode";
    private static final String ACTION_ENCODE = "encode";
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_SET_SUBSTITUTION = "setSubstitution";
    private static final String ACTION_SET_FLUID_SUBSTITUTION = "setFluidSubstitution";
    private static final String ACTION_SET_STONECUTTING_RECIPE_ID = "setStonecuttingRecipeId";
    private static final String ACTION_CYCLE_PROCESSING_OUTPUT = "cycleProcessingOutput";

    private final PatternEncodingLogic encodingLogic;
    private final FakeSlot[] craftingGridSlots = new FakeSlot[9];
    private final FakeSlot[] processingInputSlots = new FakeSlot[AEProcessingPattern.MAX_INPUT_SLOTS];
    private final FakeSlot[] processingOutputSlots = new FakeSlot[AEProcessingPattern.MAX_OUTPUT_SLOTS];
    private final FakeSlot stonecuttingInputSlot;
    private final FakeSlot smithingTableTemplateSlot;
    private final FakeSlot smithingTableBaseSlot;
    private final FakeSlot smithingTableAdditionSlot;
    private final PatternTermSlot craftOutputSlot;
    private final RestrictedInputSlot blankPatternSlot;
    private final RestrictedInputSlot encodedPatternSlot;
    // 9x9 inventory wrapper to feed into the crafting mode slots

    private final ConfigInventory encodedInputsInv;
    private final ConfigInventory encodedOutputsInv;

    private RecipeHolder<CraftingRecipe> currentRecipe;
    // The current mode is essentially the last-known client-side version of mode
    private EncodingMode currentMode;

    @GuiSync(97)
    public EncodingMode mode = EncodingMode.CRAFTING;

    @GuiSync(96)
    public boolean substitute = false;

    @GuiSync(95)
    public boolean substituteFluids = true;

    @GuiSync(94)
    @Nullable
    public ResourceLocation stonecuttingRecipeId;

    private final List<RecipeHolder<StonecutterRecipe>> stonecuttingRecipes = new ArrayList<>();

    /**
     * Whether fluids can be substituted or not depends on the recipe. This set contains the slots of the crafting
     * matrix that support such substitution.
     */
    public IntSet slotsSupportingFluidSubstitution = new IntArraySet();
    // endregion

    public PatternEncodingAccessTermMenu(int id, Inventory ip, PatternEncodingAccessTerminalPart anchor) {
        this(PEATMenus.PATTERN_ENCODING_ACCESS_TERMINAL.get(), id, ip, anchor, true);
    }

    public <T extends IConfigurableObject & IPatternTerminalMenuHost & IPatternAccessTermMenuHost>
            PatternEncodingAccessTermMenu(MenuType<?> menuType, int id, Inventory ip, T host, boolean bindInventory) {
        super(menuType, id, ip, host);

        this.termHost = host;
        this.accessHost = host;
        if (host instanceof IEnergySource hostEnergySource) {
            this.energySource = hostEnergySource;
        } else if (host instanceof IActionHost actionHost) {
            this.energySource = new ActionHostEnergySource(actionHost);
        } else {
            this.energySource = IEnergySource.empty();
        }
        this.storage = Objects.requireNonNull(host.getInventory(), "host inventory is null");

        this.clientCM = IConfigManager.builder(this::onSettingChanged)
                .registerSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE)
                .build();

        if (isServerSide()) {
            this.serverCM = host.getConfigManager();
        }

        if (bindInventory) {
            this.createPlayerInventorySlots(ip);
        }

        this.encodingLogic = host.getLogic();
        this.encodedInputsInv = encodingLogic.getEncodedInputInv();
        this.encodedOutputsInv = encodingLogic.getEncodedOutputInv();

        // Wrappers for use with slots
        var encodedInputs = encodedInputsInv.createMenuWrapper();
        var encodedOutputs = encodedOutputsInv.createMenuWrapper();

        // Create the 3x3 crafting input grid for crafting mode
        for (int i = 0; i < CRAFTING_GRID_SLOTS; i++) {
            var slot = new FakeSlot(encodedInputs, i);
            slot.setHideAmount(true);
            this.addSlot(this.craftingGridSlots[i] = slot, SlotSemantics.CRAFTING_GRID);
        }
        // Create the output slot used for crafting mode patterns
        this.addSlot(this.craftOutputSlot = new PatternTermSlot(), SlotSemantics.CRAFTING_RESULT);

        // Create as many slots as needed for processing inputs and outputs
        for (int i = 0; i < processingInputSlots.length; i++) {
            this.addSlot(
                    this.processingInputSlots[i] = new FakeSlot(encodedInputs, i), SlotSemantics.PROCESSING_INPUTS);
        }
        for (int i = 0; i < this.processingOutputSlots.length; i++) {
            this.addSlot(
                    this.processingOutputSlots[i] = new FakeSlot(encodedOutputs, i), SlotSemantics.PROCESSING_OUTPUTS);
        }
        this.processingOutputSlots[0].setIcon(Icon.BACKGROUND_PRIMARY_OUTPUT);

        // Input for stonecutting pattern encoding
        this.addSlot(this.stonecuttingInputSlot = new FakeSlot(encodedInputs, 0), SlotSemantics.STONECUTTING_INPUT);
        this.stonecuttingInputSlot.setHideAmount(true);

        // Input for smithing table pattern encoding
        this.addSlot(
                this.smithingTableTemplateSlot = new FakeSlot(encodedInputs, 0), SlotSemantics.SMITHING_TABLE_TEMPLATE);
        this.smithingTableTemplateSlot.setHideAmount(true);
        this.addSlot(this.smithingTableBaseSlot = new FakeSlot(encodedInputs, 1), SlotSemantics.SMITHING_TABLE_BASE);
        this.smithingTableBaseSlot.setHideAmount(true);
        this.addSlot(
                this.smithingTableAdditionSlot = new FakeSlot(encodedInputs, 2), SlotSemantics.SMITHING_TABLE_ADDITION);
        this.smithingTableAdditionSlot.setHideAmount(true);

        this.addSlot(
                this.blankPatternSlot = new RestrictedInputSlot(
                        RestrictedInputSlot.PlacableItemType.BLANK_PATTERN, encodingLogic.getBlankPatternInv(), 0),
                SlotSemantics.BLANK_PATTERN);
        this.addSlot(
                this.encodedPatternSlot = new RestrictedInputSlot(
                        RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN, encodingLogic.getEncodedPatternInv(), 0),
                SlotSemantics.ENCODED_PATTERN);

        this.encodedPatternSlot.setStackLimit(1);

        registerClientAction(ACTION_ENCODE, this::encode);
        registerClientAction(
                ACTION_SET_STONECUTTING_RECIPE_ID, ResourceLocation.class, encodingLogic::setStonecuttingRecipeId);
        registerClientAction(ACTION_CLEAR, this::clear);
        registerClientAction(ACTION_SET_MODE, EncodingMode.class, encodingLogic::setMode);
        registerClientAction(ACTION_SET_SUBSTITUTION, Boolean.class, encodingLogic::setSubstitution);
        registerClientAction(ACTION_SET_FLUID_SUBSTITUTION, Boolean.class, encodingLogic::setFluidSubstitution);
        registerClientAction(ACTION_CYCLE_PROCESSING_OUTPUT, this::cycleProcessingOutput);

        updateStonecuttingRecipes();
    }

    @Nullable
    public IGridNode getGridNode() {
        if (termHost instanceof IActionHost actionHost) {
            return actionHost.getActionableNode();
        }
        return null;
    }

    public boolean isKeyVisible(AEKey key) {
        // If the host is a basic item cell with a limited key space, account for this
        if (itemMenuHost != null && itemMenuHost.getItem() instanceof IBasicCellItem basicCellItem) {
            return basicCellItem.getKeyType().contains(key);
        }

        return true;
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            this.updateLinkStatus();

            this.updateActiveCraftingJobs();

            for (var set : this.serverCM.getSettings()) {
                var sideLocal = this.serverCM.getSetting(set);
                var sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    set.copy(serverCM, clientCM);
                    sendPacketToClient(new ConfigValuePacket(set, serverCM));
                }
            }

            if (termHost instanceof KeyTypeSelectionHost keyTypeSelectionHost) {
                this.searchKeyTypes = new KeyTypeSelectionMenu.SyncedKeyTypes(
                        keyTypeSelectionHost.getKeyTypeSelection().enabled());
            }

            var craftables = getCraftablesFromGrid();
            var availableStacks = storage.getAvailableStacks();

            // This is currently not supported/backed by any network service
            var requestables = new KeyCounter();

            try {
                // Craftables
                // Newly craftable
                Sets.difference(previousCraftables, craftables).forEach(updateHelper::addChange);
                // No longer craftable
                Sets.difference(craftables, previousCraftables).forEach(updateHelper::addChange);

                // Available changes
                previousAvailableStacks.removeAll(availableStacks);
                previousAvailableStacks.removeZeros();
                previousAvailableStacks.keySet().forEach(updateHelper::addChange);

                if (updateHelper.hasChanges()) {
                    var builder = MEInventoryUpdatePacket.builder(
                            containerId,
                            updateHelper.isFullUpdate(),
                            getPlayer().registryAccess());
                    builder.setFilter(this::isKeyVisible);
                    builder.addChanges(updateHelper, availableStacks, craftables, requestables);
                    builder.buildAndSend(this::sendPacketToClient);
                    updateHelper.commitChanges();
                }

            } catch (Exception e) {
                AELog.warn(e, "Failed to send incremental inventory update to client");
            }

            previousCraftables = ImmutableSet.copyOf(craftables);
            previousAvailableStacks = availableStacks;

            if (this.mode != encodingLogic.getMode()) {
                this.setMode(encodingLogic.getMode());
            }

            this.substitute = encodingLogic.isSubstitution();
            this.substituteFluids = encodingLogic.isFluidSubstitution();
            this.stonecuttingRecipeId = encodingLogic.getStonecuttingRecipeId();

            showPatternProviders =
                    this.termHost.getConfigManager().getSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS);

            super.broadcastChanges();

            updateLinkStatus();

            if (showPatternProviders != ShowPatternProviders.NOT_FULL) {
                this.pinnedHosts.clear();
            }

            IGrid grid = getGrid();

            var state = new PatternEncodingAccessTermMenu.VisitorState();
            if (grid != null) {
                for (var machineClass : grid.getMachineClasses()) {
                    if (PatternContainer.class.isAssignableFrom(machineClass)) {
                        visitPatternProviderHosts(grid, (Class<? extends PatternContainer>) machineClass, state);
                    }
                }

                // Ensure we don't keep references to removed hosts
                pinnedHosts.removeIf(host -> host.getGrid() != grid);
            } else {
                pinnedHosts.clear();
            }

            if (state.total != this.diList.size() || state.forceFullUpdate) {
                sendFullUpdate(grid);
            } else {
                sendIncrementalUpdate();
            }
        }
    }

    @Override
    public void onServerDataSync(ShortSet updatedFields) {
        super.onServerDataSync(updatedFields);

        if (updatedFields.contains(SEARCH_KEY_TYPES_ID)) {
            // Trigger re-sort
            if (getGui() != null) {
                getGui().run();
            }
        }

        // Update slot visibility
        for (var slot : craftingGridSlots) {
            slot.setActive(mode == EncodingMode.CRAFTING);
        }
        craftOutputSlot.setActive(mode == EncodingMode.CRAFTING);
        for (var slot : processingInputSlots) {
            slot.setActive(mode == EncodingMode.PROCESSING);
        }
        for (var slot : processingOutputSlots) {
            slot.setActive(mode == EncodingMode.PROCESSING);
        }

        if (this.currentMode != this.mode) {
            this.encodingLogic.setMode(this.mode);
            this.getAndUpdateOutput();
            this.updateStonecuttingRecipes();
        }
    }

    protected boolean showsCraftables() {
        return true;
    }

    private Set<AEKey> getCraftablesFromGrid() {
        IGridNode hostNode = getGridNode();
        // Wireless terminals do not directly expose the target grid (even though they have one)
        if (hostNode == null && termHost instanceof IActionHost actionHost) {
            hostNode = actionHost.getActionableNode();
        }
        if (!showsCraftables()) {
            return Collections.emptySet();
        }

        if (hostNode != null && hostNode.isActive()) {
            return hostNode.getGrid().getCraftingService().getCraftables(this::isKeyVisible);
        }
        return Collections.emptySet();
    }

    private void updateActiveCraftingJobs() {
        IGridNode hostNode = getGridNode();
        IGrid grid = null;
        if (hostNode != null) {
            grid = hostNode.getGrid();
        }

        if (grid == null) {
            // No grid to query crafting jobs from
            this.activeCraftingJobs = -1;
            return;
        }

        int activeJobs = 0;
        for (var cpus : grid.getCraftingService().getCpus()) {
            if (cpus.isBusy()) {
                activeJobs++;
            }
        }
        this.activeCraftingJobs = activeJobs;
    }

    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        if (this.getGui() != null) {
            this.getGui().run();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (isServerSide()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    @Override
    public final void handleInteraction(long serial, InventoryAction action) {
        if (isClientSide()) {
            ServerboundPacket message = new MEInteractionPacket(containerId, serial, action);
            PacketDistributor.sendToServer(message);
        }
    }

    public ILinkStatus getLinkStatus() {
        return linkStatus;
    }

    @Nullable
    private Runnable getGui() {
        return this.gui;
    }

    /**
     * Sets the current screen. Will be notified when settings change and it needs to update its sorting.
     */
    public void setGui(@Nullable Runnable gui) {
        this.gui = gui;
    }

    @Nullable
    public IClientRepo getClientRepo() {
        return clientRepo;
    }

    public void setClientRepo(@Nullable IClientRepo clientRepo) {
        this.clientRepo = clientRepo;
    }

    /**
     * Checks if the terminal has a given reservedAmounts of the requested item. Used to determine for REI/JEI if a
     * recipe is potentially craftable based on the available items.
     * <p/>
     * This method is <strong>slow</strong>, but it is client-only and thus doesn't scale with the player count.
     */
    public boolean hasIngredient(Ingredient ingredient, Object2IntOpenHashMap<Object> reservedAmounts) {
        var clientRepo = getClientRepo();

        if (clientRepo != null && getLinkStatus().connected()) {
            for (var stack : clientRepo.getByIngredient(ingredient)) {
                var reservedAmount = reservedAmounts.getOrDefault(stack, 0);
                if (stack.getStoredAmount() - reservedAmount >= 1) {
                    reservedAmounts.merge(stack, 1, Integer::sum);
                    return true;
                }
            }
        }

        return false;
    }

    public ITerminalHost getHost() {
        return termHost;
    }

    // When using a custom implementation of ILinkStatus, override this and implement your own packet
    protected void updateLinkStatus() {
        var linkStatus = termHost.getLinkStatus();
        if (!Objects.equals(this.linkStatus, linkStatus)) {
            this.linkStatus = linkStatus;
            sendPacketToClient(new SetLinkStatusPacket(linkStatus));
        }
    }

    @Override
    public void setLinkStatus(ILinkStatus linkStatus) {
        this.linkStatus = linkStatus;
    }

    @Override
    public void setItem(int slotID, int stateId, @NotNull ItemStack stack) {
        super.setItem(slotID, stateId, stack);
        this.getAndUpdateOutput();
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        super.initializeContents(stateId, items, carried);
        this.getAndUpdateOutput();
    }

    private ItemStack getAndUpdateOutput() {
        var level = this.getPlayerInventory().player.level();

        var items = NonNullList.withSize(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT, ItemStack.EMPTY);
        boolean invalidIngredients = false;
        for (int x = 0; x < items.size(); x++) {
            var stack = getEncodedCraftingIngredient(x);
            if (stack != null) {
                items.set(x, stack);
            } else {
                invalidIngredients = true;
            }
        }

        var input = CraftingInput.of(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT, items);

        if (this.currentRecipe == null || !this.currentRecipe.value().matches(input, level)) {
            if (invalidIngredients) {
                this.currentRecipe = null;
            } else {
                this.currentRecipe = level.getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, input, level)
                        .orElse(null);
            }
            this.currentMode = this.mode;
            checkFluidSubstitutionSupport();
        }

        final ItemStack is;

        if (this.currentRecipe == null) {
            is = ItemStack.EMPTY;
        } else {
            is = this.currentRecipe.value().assemble(input, level.registryAccess());
        }

        this.craftOutputSlot.setResultItem(is);
        return is;
    }

    private void checkFluidSubstitutionSupport() {
        this.slotsSupportingFluidSubstitution.clear();

        if (this.currentRecipe == null) {
            return; // No recipe -> no substitution
        }

        var encodedPattern = encodePattern();
        if (encodedPattern != null) {
            var decodedPattern = PatternDetailsHelper.decodePattern(
                    encodedPattern, this.getPlayerInventory().player.level());
            if (decodedPattern instanceof AECraftingPattern craftingPattern) {
                for (int i = 0; i < craftingPattern.getSparseInputs().size(); i++) {
                    if (craftingPattern.getValidFluid(i) != null) {
                        slotsSupportingFluidSubstitution.add(i);
                    }
                }
            }
        }
    }

    public void encode() {
        if (isClientSide()) {
            sendClientAction(ACTION_ENCODE);
            return;
        }

        ItemStack encodedPattern = encodePattern();
        if (encodedPattern != null) {
            var encodeOutput = this.encodedPatternSlot.getItem();

            // first check the output slots, should either be null, or a pattern (encoded or otherwise)
            if (!encodeOutput.isEmpty()
                    && !PatternDetailsHelper.isEncodedPattern(encodeOutput)
                    && !AEItems.BLANK_PATTERN.is(encodeOutput)) {
                return;
            } // if nothing is there we should snag a new pattern.
            else if (encodeOutput.isEmpty()) {
                var blankPattern = this.blankPatternSlot.getItem();
                if (!isPattern(blankPattern)) {
                    return; // no blanks.
                }

                // remove one, and clear the input slot.
                blankPattern.shrink(1);
                if (blankPattern.getCount() <= 0) {
                    this.blankPatternSlot.set(ItemStack.EMPTY);
                }
            }

            this.encodedPatternSlot.set(encodedPattern);
        } else {
            clearPattern();
        }
    }

    /**
     * Clears the pattern in the encoded pattern slot.
     */
    private void clearPattern() {
        var encodedPattern = this.encodedPatternSlot.getItem();
        if (PatternDetailsHelper.isEncodedPattern(encodedPattern)) {
            this.encodedPatternSlot.set(AEItems.BLANK_PATTERN.stack(encodedPattern.getCount()));
        }
    }

    @Nullable
    private ItemStack encodePattern() {
        return switch (this.mode) {
            case CRAFTING -> encodeCraftingPattern();
            case PROCESSING -> encodeProcessingPattern();
            case SMITHING_TABLE -> encodeSmithingTablePattern();
            case STONECUTTING -> encodeStonecuttingPattern();
        };
    }

    @Nullable
    private ItemStack encodeCraftingPattern() {
        var ingredients = new ItemStack[CRAFTING_GRID_SLOTS];
        boolean valid = false;
        for (int x = 0; x < ingredients.length; x++) {
            ingredients[x] = getEncodedCraftingIngredient(x);
            if (ingredients[x] == null) {
                return null; // Invalid item
            } else if (!ingredients[x].isEmpty()) {
                // At least one input must be set, but it doesn't matter which one
                valid = true;
            }
        }
        if (!valid) {
            return null;
        }

        var result = this.getAndUpdateOutput();
        if (result.isEmpty() || currentRecipe == null) {
            return null;
        }

        return PatternDetailsHelper.encodeCraftingPattern(
                this.currentRecipe, ingredients, result, isSubstitute(), isSubstituteFluids());
    }

    @Nullable
    private ItemStack encodeProcessingPattern() {
        var inputs = new GenericStack[encodedInputsInv.size()];
        boolean valid = false;
        for (int slot = 0; slot < encodedInputsInv.size(); slot++) {
            inputs[slot] = encodedInputsInv.getStack(slot);
            if (inputs[slot] != null) {
                // At least one input must be set, but it doesn't matter which one
                valid = true;
            }
        }
        if (!valid) {
            return null;
        }

        var outputs = new GenericStack[encodedOutputsInv.size()];
        for (int slot = 0; slot < encodedOutputsInv.size(); slot++) {
            outputs[slot] = encodedOutputsInv.getStack(slot);
        }
        if (outputs[0] == null) {
            // The first output slot is required
            return null;
        }

        return PatternDetailsHelper.encodeProcessingPattern(Arrays.asList(inputs), Arrays.asList(outputs));
    }

    @Nullable
    private ItemStack encodeSmithingTablePattern() {
        if (!(encodedInputsInv.getKey(0) instanceof AEItemKey template)
                || !(encodedInputsInv.getKey(1) instanceof AEItemKey base)
                || !(encodedInputsInv.getKey(2) instanceof AEItemKey addition)) {
            return null;
        }

        var input = new SmithingRecipeInput(template.toStack(), base.toStack(), addition.toStack());

        var level = getPlayer().level();
        var recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMITHING, input, level)
                .orElse(null);
        if (recipe == null) {
            return null;
        }

        var output = AEItemKey.of(recipe.value().assemble(input, level.registryAccess()));

        return PatternDetailsHelper.encodeSmithingTablePattern(
                recipe, template, base, addition, output, encodingLogic.isSubstitution());
    }

    @Nullable
    private ItemStack encodeStonecuttingPattern() {
        // Find the selected recipe
        if (stonecuttingRecipeId == null) {
            return null;
        }

        if (!(encodedInputsInv.getKey(0) instanceof AEItemKey input)) {
            return null;
        }

        var recipeInput = new SingleRecipeInput(input.toStack());

        var level = getPlayer().level();
        var recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.STONECUTTING, recipeInput, level, stonecuttingRecipeId)
                .orElse(null);
        if (recipe == null) {
            return null;
        }

        var output = AEItemKey.of(recipe.value().getResultItem(level.registryAccess()));

        return PatternDetailsHelper.encodeStonecuttingPattern(recipe, input, output, encodingLogic.isSubstitution());
    }

    /**
     * Get potential crafting ingredient encoded in given slot, return null if something is encoded in the slot, but
     * it's not an item.
     */
    @Nullable
    private ItemStack getEncodedCraftingIngredient(int slot) {
        var what = encodedInputsInv.getKey(slot);
        if (what == null) {
            return ItemStack.EMPTY;
        } else if (what instanceof AEItemKey itemKey) {
            return itemKey.toStack(1);
        } else {
            return null; // There's something in this slot that's not an item
        }
    }

    private boolean isPattern(ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }

        return AEItems.BLANK_PATTERN.is(output);
    }

    @Override
    public void onSlotChange(Slot s) {
        if (s == this.encodedPatternSlot && isServerSide()) {
            this.broadcastChanges();
        }

        if (s == this.stonecuttingInputSlot) {
            updateStonecuttingRecipes();
        }
    }

    private void updateStonecuttingRecipes() {
        stonecuttingRecipes.clear();
        if (encodedInputsInv.getKey(0) instanceof AEItemKey itemKey) {
            var level = getPlayer().level();
            var recipeManager = level.getRecipeManager();
            var recipeInput = new SingleRecipeInput(itemKey.toStack());
            stonecuttingRecipes.addAll(recipeManager.getRecipesFor(RecipeType.STONECUTTING, recipeInput, level));
        }

        // Deselect a recipe that is now unavailable
        if (stonecuttingRecipeId != null
                && stonecuttingRecipes.stream().noneMatch(r -> r.id().equals(stonecuttingRecipeId))) {
            stonecuttingRecipeId = null;
        }
    }

    public void clear() {
        if (isClientSide()) {
            sendClientAction(ACTION_CLEAR);
            return;
        }

        encodedInputsInv.clear();
        encodedOutputsInv.clear();

        this.broadcastChanges();
        this.getAndUpdateOutput();
    }

    public EncodingMode getMode() {
        return this.mode;
    }

    public void setMode(EncodingMode mode) {
        if (this.mode != mode && mode == EncodingMode.STONECUTTING) {
            updateStonecuttingRecipes();
        }

        if (isClientSide()) {
            sendClientAction(ACTION_SET_MODE, mode);
        } else {
            this.mode = mode;
        }
    }

    public boolean isSubstitute() {
        return this.substitute;
    }

    public void setSubstitute(boolean substitute) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_SUBSTITUTION, substitute);
        } else {
            this.substitute = substitute;
        }
    }

    public boolean isSubstituteFluids() {
        return this.substituteFluids;
    }

    public void setSubstituteFluids(boolean substituteFluids) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_FLUID_SUBSTITUTION, substituteFluids);
        } else {
            this.substituteFluids = substituteFluids;
        }
    }

    public @Nullable ResourceLocation getStonecuttingRecipeId() {
        return stonecuttingRecipeId;
    }

    public void setStonecuttingRecipeId(ResourceLocation id) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_STONECUTTING_RECIPE_ID, id);
        } else {
            this.encodingLogic.setStonecuttingRecipeId(id);
        }
    }

    @Override
    protected int transferStackToMenu(ItemStack input) {
        int initialCount = input.getCount();

        // try refilling the blank pattern slot
        if (blankPatternSlot.mayPlace(input)) {
            input = blankPatternSlot.safeInsert(input);
            if (input.isEmpty()) {
                return initialCount;
            }
        }

        // try refilling the encoded pattern slot
        if (encodedPatternSlot.mayPlace(input)) {
            input = encodedPatternSlot.safeInsert(input);
            if (input.isEmpty()) {
                return initialCount;
            }
        }

        int transferred = initialCount - input.getCount();
        return transferred + super.transferStackToMenu(input);
    }

    @Contract("null -> false")
    public boolean canModifyAmountForSlot(@Nullable Slot slot) {
        return isProcessingPatternSlot(slot) && slot.hasItem();
    }

    @Contract("null -> false")
    public boolean isProcessingPatternSlot(@Nullable Slot slot) {
        if (slot == null || mode != EncodingMode.PROCESSING) {
            return false;
        }

        for (var processingOutputSlot : processingOutputSlots) {
            if (processingOutputSlot == slot) {
                return true;
            }
        }

        for (var craftingSlot : processingInputSlots) {
            if (craftingSlot == slot) {
                return true;
            }
        }
        return false;
    }

    public FakeSlot[] getCraftingGridSlots() {
        return craftingGridSlots;
    }

    public FakeSlot[] getProcessingInputSlots() {
        return processingInputSlots;
    }

    public FakeSlot[] getProcessingOutputSlots() {
        return processingOutputSlots;
    }

    public FakeSlot getSmithingTableTemplateSlot() {
        return smithingTableTemplateSlot;
    }

    public FakeSlot getSmithingTableBaseSlot() {
        return smithingTableBaseSlot;
    }

    public FakeSlot getSmithingTableAdditionSlot() {
        return smithingTableAdditionSlot;
    }

    /**
     * Cycles the defined processing outputs around in case recipe transfer didn't put what the player considers the
     * primary output into the right slot.
     */
    public void cycleProcessingOutput() {
        if (isClientSide()) {
            sendClientAction(ACTION_CYCLE_PROCESSING_OUTPUT);
        } else {
            if (mode != EncodingMode.PROCESSING) {
                return;
            }

            var newOutputs = new ItemStack[getProcessingOutputSlots().length];
            for (int i = 0; i < processingOutputSlots.length; i++) {
                newOutputs[i] = ItemStack.EMPTY;
                if (!processingOutputSlots[i].getItem().isEmpty()) {
                    // Search for the next, skipping empty slots
                    for (int j = 1; j < processingOutputSlots.length; j++) {
                        var nextItem = processingOutputSlots[(i + j) % processingOutputSlots.length].getItem();
                        if (!nextItem.isEmpty()) {
                            newOutputs[i] = nextItem;
                            break;
                        }
                    }
                }
            }

            for (int i = 0; i < newOutputs.length; i++) {
                processingOutputSlots[i].set(newOutputs[i]);
            }
        }
    }

    // Can cycle if there is more than 1 processing output encoded
    public boolean canCycleProcessingOutputs() {
        return mode == EncodingMode.PROCESSING
                && Arrays.stream(processingOutputSlots)
                                .filter(s -> !s.getItem().isEmpty())
                                .count()
                        > 1;
    }

    public List<RecipeHolder<StonecutterRecipe>> getStonecuttingRecipes() {
        return stonecuttingRecipes;
    }

    @Nullable
    private IGrid getGrid() {
        var agn = accessHost.getGridNode();
        if (agn != null && agn.isActive()) {
            return agn.getGrid();
        }
        return null;
    }

    private static class VisitorState {
        // Total number of pattern provider hosts found
        int total;
        // Set to true if any visited machines were missing from diList, or had a different name
        boolean forceFullUpdate;
    }

    private boolean isFull(PatternContainer logic) {
        for (int i = 0; i < logic.getTerminalPatternInventory().size(); i++) {
            if (logic.getTerminalPatternInventory().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isVisible(PatternContainer container) {
        boolean isVisible = container.isVisibleInTerminal();

        return switch (getShownProviders()) {
            case ShowPatternProviders.VISIBLE -> isVisible;
            case ShowPatternProviders.NOT_FULL -> isVisible && (pinnedHosts.contains(container) || !isFull(container));
            case ShowPatternProviders.ALL -> true;
        };
    }

    private <T extends PatternContainer> void visitPatternProviderHosts(
            IGrid grid, Class<T> machineClass, PatternEncodingAccessTermMenu.VisitorState state) {
        for (var container : grid.getActiveMachines(machineClass)) {
            if (!isVisible(container)) {
                continue;
            }

            if (getShownProviders() == ShowPatternProviders.NOT_FULL) {
                pinnedHosts.add(container);
            }

            var t = this.diList.get(container);
            if (t == null || !t.group.equals(container.getTerminalGroup())) {
                state.forceFullUpdate = true;
            }

            state.total++;
        }
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        final PatternEncodingAccessTermMenu.ContainerTracker inv = this.byId.get(id);
        if (inv == null) {
            // Can occur if the client sent an interaction packet right before an inventory got removed
            return;
        }
        if (slot < 0 || slot >= inv.server.size()) {
            // Client refers to an invalid slot. This should NOT happen
            AELog.warn("Client refers to invalid slot %d of inventory %s", slot, inv.container);
            return;
        }

        final ItemStack is = inv.server.getStackInSlot(slot);

        var patternSlot = new FilteredInternalInventory(
                inv.server.getSlotInv(slot), new PatternEncodingAccessTermMenu.PatternSlotFilter());

        var carried = getCarried();
        switch (action) {
            case PICKUP_OR_SET_DOWN -> {
                if (!carried.isEmpty()) {
                    ItemStack inSlot = patternSlot.getStackInSlot(0);
                    if (inSlot.isEmpty()) {
                        setCarried(patternSlot.addItems(carried));
                    } else {
                        inSlot = inSlot.copy();
                        final ItemStack inHand = carried.copy();

                        patternSlot.setItemDirect(0, ItemStack.EMPTY);
                        setCarried(ItemStack.EMPTY);

                        setCarried(patternSlot.addItems(inHand.copy()));

                        if (getCarried().isEmpty()) {
                            setCarried(inSlot);
                        } else {
                            setCarried(inHand);
                            patternSlot.setItemDirect(0, inSlot);
                        }
                    }
                } else {
                    setCarried(patternSlot.getStackInSlot(0));
                    patternSlot.setItemDirect(0, ItemStack.EMPTY);
                }
            }
            case SPLIT_OR_PLACE_SINGLE -> {
                if (!carried.isEmpty()) {
                    ItemStack extra = carried.split(1);
                    if (!extra.isEmpty()) {
                        extra = patternSlot.addItems(extra);
                    }
                    if (!extra.isEmpty()) {
                        carried.grow(extra.getCount());
                    }
                } else if (!is.isEmpty()) {
                    setCarried(patternSlot.extractItem(0, (is.getCount() + 1) / 2, false));
                }
            }
            case SHIFT_CLICK -> {
                var stack = patternSlot.getStackInSlot(0).copy();
                if (!player.getInventory().add(stack)) {
                    patternSlot.setItemDirect(0, stack);
                } else {
                    patternSlot.setItemDirect(0, ItemStack.EMPTY);
                }
            }
            case MOVE_REGION -> {
                for (int x = 0; x < inv.server.size(); x++) {
                    var stack = inv.server.getStackInSlot(x);
                    if (!player.getInventory().add(stack)) {
                        patternSlot.setItemDirect(0, stack);
                    } else {
                        patternSlot.setItemDirect(0, ItemStack.EMPTY);
                    }
                }
            }
            case CREATIVE_DUPLICATE -> {
                if (player.getAbilities().instabuild && carried.isEmpty()) {
                    setCarried(is.isEmpty() ? ItemStack.EMPTY : is.copy());
                }
            }
        }
    }

    public void quickMovePattern(ServerPlayer player, int clickedSlot, List<Long> allowedPatternContainers) {
        if (clickedSlot < 0 || clickedSlot >= this.slots.size()) {
            return;
        }
        Slot sourceSlot = getSlot(clickedSlot);
        if (!isPlayerSideSlot(sourceSlot)) {
            return;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        if (sourceStack.getCount() != 1) {
            return;
        }
        var pattern = PatternDetailsHelper.decodePattern(sourceStack, player.level());
        if (pattern == null) {
            return;
        }
        boolean molecularAssemblerPattern = pattern instanceof IMolecularAssemblerSupportedPattern;

        // Collect possible targets
        List<PatternEncodingAccessTermMenu.ContainerTracker> targets = new ArrayList<>();
        for (var id : allowedPatternContainers) {
            var inv = this.byId.get(id.longValue());
            // Check pattern container exists and is visible
            if (inv != null && isVisible(inv.container)) {
                var icon = inv.group.icon();
                // Keep molecular assembler iff pattern is supported by molecular assembler
                boolean molecularAssembler = icon != null && icon.is(AEBlocks.MOLECULAR_ASSEMBLER);
                if (molecularAssemblerPattern == molecularAssembler) {
                    targets.add(inv);
                }
            }
        }

        // For now, limit to pattern containers in the same group
        if (targets.stream().map(t -> t.group).distinct().count() != 1) {
            return;
        }

        // Try to insert in each container until we succeed
        for (var target : targets) {
            var targetContainer =
                    new FilteredInternalInventory(target.server, new PatternEncodingAccessTermMenu.PatternSlotFilter());
            if (targetContainer.addItems(sourceStack).isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
                return;
            }
        }
    }

    private void sendFullUpdate(@Nullable IGrid grid) {
        this.byId.clear();
        this.diList.clear();

        sendPacketToClient(new ClearPatternAccessTerminalPacket());

        if (grid == null) {
            return;
        }

        for (var machineClass : grid.getMachineClasses()) {
            var containerClass = tryCastMachineToContainer(machineClass);
            if (containerClass == null) {
                continue;
            }

            for (var container : grid.getActiveMachines(containerClass)) {
                if (isVisible(container)) {
                    this.diList.put(
                            container,
                            new PatternEncodingAccessTermMenu.ContainerTracker(
                                    container, container.getTerminalPatternInventory(), container.getTerminalGroup()));
                }
            }
        }

        for (var inv : this.diList.values()) {
            this.byId.put(inv.serverId, inv);
            sendPacketToClient(inv.createFullPacket());
        }
    }

    private void sendIncrementalUpdate() {
        for (var inv : this.diList.values()) {
            var packet = inv.createUpdatePacket();
            if (packet != null) {
                sendPacketToClient(packet);
            }
        }
    }

    public void setSearch(String filter) {}

    private static class ContainerTracker {

        private final PatternContainer container;
        private final long sortBy;
        private final long serverId = inventorySerial++;
        private final PatternContainerGroup group;
        // This is used to track the inventory contents we sent to the client for change detection
        private final InternalInventory client;
        // This is a reference to the real inventory used by this machine
        private final InternalInventory server;

        public ContainerTracker(PatternContainer container, InternalInventory patterns, PatternContainerGroup group) {
            this.container = container;
            this.server = patterns;
            this.client = new AppEngInternalInventory(this.server.size());
            this.group = group;
            this.sortBy = container.getTerminalSortOrder();
        }

        public PatternAccessTerminalPacket createFullPacket() {
            var slots = new Int2ObjectArrayMap<ItemStack>(server.size());
            for (int i = 0; i < server.size(); i++) {
                var stack = server.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    slots.put(i, stack);
                }
            }

            return PatternAccessTerminalPacket.fullUpdate(serverId, server.size(), sortBy, group, slots);
        }

        @Nullable
        public PatternAccessTerminalPacket createUpdatePacket() {
            var changedSlots = detectChangedSlots();
            if (changedSlots == null) {
                return null;
            }

            var slots = new Int2ObjectArrayMap<ItemStack>(changedSlots.size());
            for (int i = 0; i < changedSlots.size(); i++) {
                var slot = changedSlots.getInt(i);
                var stack = server.getStackInSlot(slot);
                // "update" client side.
                client.setItemDirect(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                slots.put(slot, stack);
            }

            return PatternAccessTerminalPacket.incrementalUpdate(serverId, slots);
        }

        @Nullable
        private IntList detectChangedSlots() {
            IntList changedSlots = null;
            for (int x = 0; x < server.size(); x++) {
                if (isDifferent(server.getStackInSlot(x), client.getStackInSlot(x))) {
                    if (changedSlots == null) {
                        changedSlots = new IntArrayList();
                    }
                    changedSlots.add(x);
                }
            }
            return changedSlots;
        }

        private static boolean isDifferent(ItemStack a, ItemStack b) {
            if (a.isEmpty() && b.isEmpty()) {
                return false;
            }

            if (a.isEmpty() || b.isEmpty()) {
                return true;
            }

            return !ItemStack.matches(a, b);
        }
    }

    private static class PatternSlotFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && PatternDetailsHelper.isEncodedPattern(stack);
        }
    }

    private static Class<? extends PatternContainer> tryCastMachineToContainer(Class<?> machineClass) {
        if (PatternContainer.class.isAssignableFrom(machineClass)) {
            return machineClass.asSubclass(PatternContainer.class);
        }
        return null;
    }
}

package yuuki1293.ae2peat.gui;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.behaviors.EmptyingAction;
import appeng.api.config.*;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.*;
import appeng.api.storage.ILinkStatus;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.*;
import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.client.gui.me.patternaccess.PatternSlot;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;
import appeng.client.gui.widgets.*;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.core.network.serverbound.QuickMovePatternPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.ItemListMod;
import appeng.menu.SlotSemantics;
import appeng.parts.encoding.EncodingMode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import guideme.color.ConstantColor;
import guideme.document.LytRect;
import guideme.render.SimpleRenderContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

public class PatternEncodingAccessTermScreen<C extends PatternEncodingAccessTermMenu> extends AEBaseScreen<C>
        implements ISortSource {

    private static final Logger LOG = LoggerFactory.getLogger(PatternEncodingAccessTermScreen.class);

    private static final String TEXT_ID_ENTRIES_SHOWN = "entriesShown";

    private static final int MIN_ROWS = 2;

    private final TerminalStyle termStyle;
    protected final Repo repo;

    private final Map<EncodingMode, EncodingModePanel> modePanels = new EnumMap<>(EncodingMode.class);
    private final Map<EncodingMode, TabButton> modeTabButtons = new EnumMap<>(EncodingMode.class);

    private static final int GUI_WIDTH = 195;
    private static final int GUI_TOP_AND_BOTTOM_PADDING = 54;

    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;

    private static final int GUI_HEADER_HEIGHT = 17;
    private static final int GUI_FOOTER_HEIGHT = 99;
    private static final int COLUMNS = 9;

    /**
     * Additional margin in pixel for a text row inside the scrolling box.
     */
    private static final int PATTERN_PROVIDER_NAME_MARGIN_X = 2;

    /**
     * The maximum length for the string of a text row in pixel.
     */
    private static final int TEXT_MAX_WIDTH = 155;

    /**
     * Height of a table-row in pixels.
     */
    private static final int ROW_HEIGHT = 18;

    /**
     * Size of a slot in both x and y dimensions in pixel, most likely always the same as ROW_HEIGHT.
     */
    private static final int SLOT_SIZE = ROW_HEIGHT;

    // Bounding boxes of key areas in the UI texture.
    // The upper part of the UI, anything above the scrollable area (incl. its top border)
    private static final Rect2i HEADER_BBOX = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);
    // Background for a text row in the scroll-box.
    // Spans across the whole texture including the right and left borders including the scrollbar.
    // Covers separate textures for the top, middle and bottoms rows for more customization.
    private static final Rect2i ROW_TEXT_TOP_BBOX = new Rect2i(0, 17, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_TEXT_MIDDLE_BBOX = new Rect2i(0, 53, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_TEXT_BOTTOM_BBOX = new Rect2i(0, 89, GUI_WIDTH, ROW_HEIGHT);
    // Background for a inventory row in the scroll-box.
    // Spans across the whole texture including the right and left borders including the scrollbar.
    // Covers separate textures for the top, middle and bottoms rows for more customization.
    private static final Rect2i ROW_INVENTORY_TOP_BBOX = new Rect2i(0, 35, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_INVENTORY_MIDDLE_BBOX = new Rect2i(0, 71, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i ROW_INVENTORY_BOTTOM_BBOX = new Rect2i(0, 107, GUI_WIDTH, ROW_HEIGHT);
    // This is the lower part of the UI, anything below the scrollable area (incl. its bottom border)
    private static final Rect2i FOOTER_BBOX = new Rect2i(0, 125, GUI_WIDTH, GUI_FOOTER_HEIGHT);

    private static final Comparator<PatternContainerGroup> GROUP_COMPARATOR =
            Comparator.comparing(group -> group.name().getString().toLowerCase(Locale.ROOT));

    private final HashMap<Long, PatternContainerRecord> byId = new HashMap<>();
    // Used to show multiple pattern providers with the same name under a single header
    private final HashMultimap<PatternContainerGroup, PatternContainerRecord> byGroup = HashMultimap.create();
    private final ArrayList<PatternContainerGroup> groups = new ArrayList<>();
    private final ArrayList<PatternEncodingAccessTermScreen.Row> rows = new ArrayList<>();

    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();
    private final Scrollbar scrollbar;
    private final AETextField searchField;
    private final Map<ItemStack, String> patternSearchText = new WeakHashMap<>();

    private int visibleRows = 0;

    private final ServerSettingToggleButton<ShowPatternProviders> showPatternProviders;

    public PatternEncodingAccessTermScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.termStyle = style.getTerminalStyle();
        if (this.style == null) {
            throw new IllegalStateException(
                    "Cannot construct screen " + getClass() + " without a terminalStyles setting");
        }

        this.scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.repo = new Repo(scrollbar, this);
        menu.setClientRepo(this.repo);

        // Clear external search on open if configured, but not upon returning from a sub-screen
        if (!menu.isReturnedFromSubScreen() && config.isUseExternalSearch() && config.isClearExternalSearchOnOpen()) {
            ItemListMod.setSearchText("");
        }

        for (var mode : EncodingMode.values()) {
            var panel =
                    switch (mode) {
                        case CRAFTING -> new CraftingEncodingPanel(this, widgets);
                        case PROCESSING -> new ProcessingEncodingPanel(this, widgets);
                        case SMITHING_TABLE -> new SmithingTableEncodingPanel(this, widgets);
                        case STONECUTTING -> new StonecuttingEncodingPanel(this, widgets);
                    };
            var tabButton = new TabButton(
                    panel.getIcon(), panel.getTabTooltip(), btn -> getMenu().setMode(mode));
            tabButton.setStyle(TabButton.Style.HORIZONTAL);

            var modeIndex = modeTabButtons.size();
            widgets.add("modePanel" + modeIndex, panel);
            widgets.add("modeTabButton" + modeIndex, tabButton);
            modeTabButtons.put(mode, tabButton);
            modePanels.put(mode, panel);
        }

        var encodeBtn = new ActionButton(ActionItems.ENCODE, act -> menu.encode());
        widgets.add("encodePattern", encodeBtn);

        this.imageWidth = GUI_WIDTH;

        // Add a terminalstyle button
        appeng.api.config.TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
        this.addToLeftToolbar(
                new SettingToggleButton<>(Settings.TERMINAL_STYLE, terminalStyle, this::toggleTerminalStyle));

        showPatternProviders =
                new ServerSettingToggleButton<>(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS, ShowPatternProviders.VISIBLE);

        this.addToLeftToolbar(showPatternProviders);

        this.searchField = widgets.addTextField("search");
        this.searchField.setResponder(str -> this.refreshList());
        this.searchField.setPlaceholder(GuiText.SearchPlaceholder.text());
    }

    @Override
    public void init() {
        // Re-create the ME slots since the number of rows could have changed
        List<Slot> slots = this.menu.slots;
        slots.removeIf(slot -> slot instanceof RepoSlot);

        this.visibleRows = Math.max(
                2,
                config.getTerminalStyle()
                        .getRows((this.height - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT - GUI_TOP_AND_BOTTOM_PADDING)
                                / ROW_HEIGHT));
        // Render inventory in correct place.
        this.imageHeight = GUI_HEADER_HEIGHT + GUI_FOOTER_HEIGHT + this.visibleRows * ROW_HEIGHT;

        super.init();

        // Autofocus search field
        this.setInitialFocus(this.searchField);

        // numLines may have changed, recalculate scroll bar.
        this.resetScrollbar();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        for (var mode : EncodingMode.values()) {
            var selected = menu.getMode() == mode;
            modeTabButtons.get(mode).setSelected(selected);
            modePanels.get(mode).setVisible(selected);
        }

        this.showPatternProviders.set(this.menu.getShownProviders());
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        this.menu.slots.removeIf(slot -> slot instanceof PatternSlot);

        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        var level = Minecraft.getInstance().level;

        final int scrollLevel = scrollbar.getCurrentScroll();
        int i = 0;
        for (; i < this.visibleRows; ++i) {
            if (scrollLevel + i < this.rows.size()) {
                var row = this.rows.get(scrollLevel + i);
                if (row instanceof PatternEncodingAccessTermScreen.SlotsRow slotsRow) {
                    // Note: We have to shift everything after the header up by 1 to avoid black line duplication.
                    var container = slotsRow.container;
                    for (int col = 0; col < slotsRow.slots; col++) {
                        var slot = new PatternSlot(
                                container, slotsRow.offset + col, col * SLOT_SIZE + GUI_PADDING_X, (i + 1) * SLOT_SIZE);
                        this.menu.slots.add(slot);

                        // Indicate invalid patterns
                        var pattern = container.getInventory().getStackInSlot(slotsRow.offset + col);
                        if (!pattern.isEmpty() && PatternDetailsHelper.decodePattern(pattern, level) == null) {
                            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x7fff0000);
                        }
                    }
                } else if (row instanceof PatternEncodingAccessTermScreen.GroupHeaderRow headerRow) {
                    var group = headerRow.group;
                    if (group.icon() != null) {
                        var renderContext = new SimpleRenderContext(LytRect.empty(), guiGraphics);
                        renderContext.renderItem(
                                group.icon().getReadOnlyStack(),
                                GUI_PADDING_X + PATTERN_PROVIDER_NAME_MARGIN_X,
                                GUI_PADDING_Y + GUI_HEADER_HEIGHT + i * ROW_HEIGHT,
                                8,
                                8);
                    }

                    final int rows = this.byGroup.get(group).size();

                    FormattedText displayName;
                    if (rows > 1) {
                        displayName =
                                Component.empty().append(group.name()).append(Component.literal(" (" + rows + ')'));
                    } else {
                        displayName = group.name();
                    }

                    var text = Language.getInstance()
                            .getVisualOrder(this.font.substrByWidth(displayName, TEXT_MAX_WIDTH - 10));

                    guiGraphics.drawString(
                            font,
                            text,
                            GUI_PADDING_X + PATTERN_PROVIDER_NAME_MARGIN_X + 10,
                            GUI_PADDING_Y + GUI_HEADER_HEIGHT + i * ROW_HEIGHT,
                            textColor,
                            false);
                }
            }
        }

        // Draw an overlay indicating the grid is disconnected
        renderLinkStatus(guiGraphics, getMenu().getLinkStatus());
    }

    private void renderLinkStatus(GuiGraphics guiGraphics, ILinkStatus linkStatus) {
        // Draw an overlay indicating the grid is disconnected
        if (!linkStatus.connected()) {
            var renderContext = new SimpleRenderContext(LytRect.empty(), guiGraphics);

            var rect = new LytRect(GUI_PADDING_X - 1, GUI_HEADER_HEIGHT, COLUMNS * 18, visibleRows * ROW_HEIGHT);

            renderContext.fillRect(rect, new ConstantColor(0x3f000000));

            // Draw the disconnect status on top of the grid
            var statusDescription = linkStatus.statusDescription();
            if (statusDescription != null) {
                renderContext.renderTextCenteredIn(statusDescription.getString(), ERROR_TEXT_STYLE, rect);
            }
        }
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        // handler for middle mouse button crafting in survival mode
        if (this.minecraft.options.keyPickItem.matchesMouse(btn)) {
            var slot = this.findSlot(xCoord, yCoord);
            if (menu.canModifyAmountForSlot(slot)) {
                var currentStack = GenericStack.fromItemStack(slot.getItem());
                if (currentStack != null) {
                    var screen = new SetProcessingPatternAmountScreen<>(this, currentStack, newStack -> {
                        ServerboundPacket message = new InventoryActionPacket(
                                InventoryAction.SET_FILTER, slot.index, GenericStack.wrapInItemStack(newStack));
                        PacketDistributor.sendToServer(message);
                    });
                    switchToScreen(screen);
                    return true;
                }
            }
        }
        if (btn == 1 && this.searchField.isMouseOver(xCoord, yCoord)) {
            this.searchField.setValue("");
            // Don't return immediately to also grab focus.
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof PatternSlot) {
            InventoryAction action = null;

            switch (clickType) {
                case PICKUP: // pickup / set-down.
                    action = mouseButton == 1
                            ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                            : InventoryAction.PICKUP_OR_SET_DOWN;
                    break;
                case QUICK_MOVE:
                    action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;

                case CLONE: // creative dupe:
                    if (getPlayer().getAbilities().instabuild) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }

                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                PatternSlot machineSlot = (PatternSlot) slot;
                final InventoryActionPacket p = new InventoryActionPacket(
                        action, machineSlot.slot, machineSlot.getMachineInv().getServerId());
                PacketDistributor.sendToServer(p);
            }

            return;
        }

        if (clickType == ClickType.QUICK_MOVE && menu.isPlayerSideSlot(slot)) {
            Set<Long> visiblePatternContainers = new LinkedHashSet<>();
            for (var row : this.rows) {
                if (row instanceof PatternEncodingAccessTermScreen.SlotsRow slotsRow) {
                    visiblePatternContainers.add(slotsRow.container.getServerId());
                }
            }

            int clickedSlot = slot.getContainerSlot();
            var packet =
                    new QuickMovePatternPacket(menu.containerId, clickedSlot, List.copyOf(visiblePatternContainers));
            PacketDistributor.sendToServer(packet);
            return;
        }

        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && menu.canModifyAmountForSlot(this.hoveredSlot)) {
            var itemTooltip = new ArrayList<>(getTooltipFromContainerItem(this.hoveredSlot.getItem()));
            var unwrapped = GenericStack.fromItemStack(this.hoveredSlot.getItem());
            if (unwrapped != null) {
                itemTooltip.add(Tooltips.getAmountTooltip(ButtonToolTips.Amount, unwrapped));
            }
            itemTooltip.add(Tooltips.getSetAmountTooltip());
            drawTooltip(guiGraphics, x, y, itemTooltip);
        } else if (hoveredSlot == null) {
            var hoveredLineIndex = getHoveredLineIndex(x, y);
            if (hoveredLineIndex != -1) {
                var row = rows.get(hoveredLineIndex);
                if (row instanceof PatternEncodingAccessTermScreen.GroupHeaderRow headerRow
                        && !headerRow.group.tooltip().isEmpty()) {
                    guiGraphics.renderTooltip(font, headerRow.group.tooltip(), Optional.empty(), x, y);
                    return;
                }
            }
        } else {
            super.renderTooltip(guiGraphics, x, y);
        }
    }

    private int getHoveredLineIndex(int x, int y) {
        x = x - leftPos - GUI_PADDING_X;
        y = y - topPos - SLOT_SIZE; // Header is exactly one slot in size
        if (x < 0 || y < 0) {
            return -1;
        }
        if (x >= SLOT_SIZE * COLUMNS || y >= visibleRows * ROW_HEIGHT) {
            return -1;
        }

        var rowIndex = scrollbar.getCurrentScroll() + y / ROW_HEIGHT;
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return -1;
        }
        return rowIndex;
    }

    @Override
    protected EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
        // Since the crafting matrix and output slot are not backed by a config inventory, the default behavior
        // does not work out of the box.
        if (menu.isProcessingPatternSlot(slot)) {
            // See if we should offer the left-/right-click differentiation for setting a different filter
            var emptyingAction = ContainerItemStrategies.getEmptyingAction(carried);
            if (emptyingAction != null) {
                return emptyingAction;
            }
        }

        return super.getEmptyingAction(slot, carried);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        // Draw the top of the dialog
        blit(guiGraphics, offsetX, offsetY, HEADER_BBOX);

        final int scrollLevel = scrollbar.getCurrentScroll();

        int currentY = offsetY + GUI_HEADER_HEIGHT;

        // Draw the footer now so slots will draw on top of it
        blit(guiGraphics, offsetX, currentY + this.visibleRows * ROW_HEIGHT, FOOTER_BBOX);

        for (int i = 0; i < this.visibleRows; ++i) {
            // Draw the dialog background for this row
            // Skip 1 pixel for the first row in order to not over-draw on the top scrollbox border,
            // and do the same but for the bottom border on the last row
            boolean firstLine = i == 0;
            boolean lastLine = i == this.visibleRows - 1;

            // Draw the background for the slots in an inventory row
            Rect2i bbox = selectRowBackgroundBox(false, firstLine, lastLine);
            blit(guiGraphics, offsetX, currentY, bbox);
            if (scrollLevel + i < this.rows.size()) {
                var row = this.rows.get(scrollLevel + i);
                if (row instanceof PatternEncodingAccessTermScreen.SlotsRow slotsRow) {
                    bbox = selectRowBackgroundBox(true, firstLine, lastLine);
                    bbox.setWidth(GUI_PADDING_X + SLOT_SIZE * slotsRow.slots - 1);
                    blit(guiGraphics, offsetX, currentY, bbox);
                }
            }

            currentY += ROW_HEIGHT;
        }
    }

    private Rect2i selectRowBackgroundBox(boolean isInvLine, boolean firstLine, boolean lastLine) {
        if (isInvLine) {
            if (firstLine) {
                return ROW_INVENTORY_TOP_BBOX;
            } else if (lastLine) {
                return ROW_INVENTORY_BOTTOM_BBOX;
            } else {
                return ROW_INVENTORY_MIDDLE_BBOX;
            }
        } else if (firstLine) {
            return ROW_TEXT_TOP_BBOX;
        } else if (lastLine) {
            return ROW_TEXT_BOTTOM_BBOX;
        } else {
            return ROW_TEXT_MIDDLE_BBOX;
        }
    }

    @Override
    public boolean charTyped(char character, int key) {
        if (character == ' ' && this.searchField.getValue().isEmpty()) {
            return true;
        }
        return super.charTyped(character, key);
    }

    public void clear() {
        this.byId.clear();
        // invalid caches on refresh
        this.cachedSearches.clear();
        this.refreshList();
    }

    public void postFullUpdate(
            long inventoryId,
            long sortBy,
            PatternContainerGroup group,
            int inventorySize,
            Int2ObjectMap<ItemStack> slots) {
        var record = new PatternContainerRecord(inventoryId, inventorySize, sortBy, group);
        this.byId.put(inventoryId, record);

        var inventory = record.getInventory();
        for (var entry : slots.int2ObjectEntrySet()) {
            inventory.setItemDirect(entry.getIntKey(), entry.getValue());
        }

        // invalid caches on refresh
        this.cachedSearches.clear();
        this.refreshList();
    }

    public void postIncrementalUpdate(long inventoryId, Int2ObjectMap<ItemStack> slots) {
        var record = byId.get(inventoryId);
        if (record == null) {
            LOG.warn("Ignoring incremental update for unknown inventory id {}", inventoryId);
            return;
        }

        var inventory = record.getInventory();
        for (var entry : slots.int2ObjectEntrySet()) {
            inventory.setItemDirect(entry.getIntKey(), entry.getValue());
        }
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot s) {
        super.renderSlot(guiGraphics, s);

        if (shouldShowCraftableIndicatorForSlot(s)) {
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0, 0, 100); // Items are rendered with offset of 100, offset text too.
            StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, s.x - 11, s.y - 11, "+", false);
            poseStack.popPose();
        }
    }

    @Override
    protected @NotNull List<Component> getTooltipFromContainerItem(ItemStack stack) {
        var lines = super.getTooltipFromContainerItem(stack);

        // Append an indication to the tooltip that the item is craftable
        if (hoveredSlot != null && shouldShowCraftableIndicatorForSlot(hoveredSlot)) {
            lines = new ArrayList<>(lines); // Ensures we're not modifying a cached copy
            lines.add(ButtonToolTips.Craftable.text().withStyle(ChatFormatting.DARK_GRAY));
        }

        return lines;
    }

    private boolean shouldShowCraftableIndicatorForSlot(Slot s) {
        // Mark inputs for patterns for which the grid already has a pattern
        var semantic = menu.getSlotSemantic(s);
        if (semantic == SlotSemantics.CRAFTING_GRID
                || semantic == SlotSemantics.PROCESSING_INPUTS
                || semantic == SlotSemantics.SMITHING_TABLE_ADDITION
                || semantic == SlotSemantics.SMITHING_TABLE_BASE
                || semantic == SlotSemantics.SMITHING_TABLE_TEMPLATE
                || semantic == SlotSemantics.STONECUTTING_INPUT) {
            var slotContent = GenericStack.fromItemStack(s.getItem());
            if (slotContent == null) {
                return false;
            }

            return repo.isCraftable(slotContent.what());
        }
        return false;
    }

    /**
     * Rebuilds the list of pattern providers.
     * <p>
     * Respects a search term if present (ignores case) and adding only matching patterns.
     */
    private void refreshList() {
        this.byGroup.clear();

        final String searchFilterLowerCase = this.searchField.getValue().toLowerCase();

        final Set<Object> cachedSearch = this.getCacheForSearchTerm(searchFilterLowerCase);
        final boolean rebuild = cachedSearch.isEmpty();

        for (PatternContainerRecord entry : this.byId.values()) {
            // ignore inventory if not doing a full rebuild or cache already marks it as miss.
            if (!rebuild && !cachedSearch.contains(entry)) {
                continue;
            }

            // Shortcut to skip any filter if search term is ""/empty
            boolean found = searchFilterLowerCase.isEmpty();

            // Search if the current inventory holds a pattern containing the search term.
            if (!found) {
                for (ItemStack itemStack : entry.getInventory()) {
                    found = this.itemStackMatchesSearchTerm(itemStack, searchFilterLowerCase);
                    if (found) {
                        break;
                    }
                }
            }

            // if found, filter skipped or machine name matching the search term, add it
            if (found || entry.getSearchName().contains(searchFilterLowerCase)) {
                this.byGroup.put(entry.getGroup(), entry);
                cachedSearch.add(entry);
            } else {
                cachedSearch.remove(entry);
            }
        }

        this.groups.clear();
        this.groups.addAll(this.byGroup.keySet());

        this.groups.sort(GROUP_COMPARATOR);

        this.rows.clear();
        this.rows.ensureCapacity(this.getMaxRows());

        for (var group : this.groups) {
            this.rows.add(new PatternEncodingAccessTermScreen.GroupHeaderRow(group));

            var containers = new ArrayList<>(this.byGroup.get(group));
            Collections.sort(containers);
            for (var container : containers) {
                // Wrap the container inventory slots
                var inventory = container.getInventory();
                for (var offset = 0; offset < inventory.size(); offset += COLUMNS) {
                    var slots = Math.min(inventory.size() - offset, COLUMNS);
                    var containerRow = new PatternEncodingAccessTermScreen.SlotsRow(container, offset, slots);
                    this.rows.add(containerRow);
                }
            }
        }

        // lines may have changed - recalculate scroll bar.
        this.resetScrollbar();
    }

    /**
     * Should be called whenever this.lines.size() or this.numLines changes.
     */
    private void resetScrollbar() {
        // Needs to take the border into account, so offset for 1 px on the top and bottom.
        scrollbar.setHeight(this.visibleRows * ROW_HEIGHT - 2);
        scrollbar.setRange(0, this.rows.size() - this.visibleRows, 2);
    }

    private boolean itemStackMatchesSearchTerm(ItemStack itemStack, String searchTerm) {
        if (itemStack.isEmpty()) {
            return false;
        }

        // Potential later use to filter by input
        return patternSearchText
                .computeIfAbsent(itemStack, this::getPatternSearchText)
                .contains(searchTerm);
    }

    private String getPatternSearchText(ItemStack stack) {
        var level = menu.getPlayer().level();
        var text = new StringBuilder();
        var pattern = PatternDetailsHelper.decodePattern(stack, level);

        if (pattern != null) {
            for (var output : pattern.getOutputs()) {
                output.what().getDisplayName().visit(content -> {
                    text.append(content.toLowerCase());
                    return Optional.empty();
                });
                text.append('\n');
            }
        }

        return text.toString();
    }

    /**
     * Tries to retrieve a cache for a with search term as keyword.
     * <p>
     * If this cache should be empty, it will populate it with an earlier cache if available or at least the cache for
     * the empty string.
     *
     * @param searchTerm the corresponding search
     * @return a Set matching a superset of the search term
     */
    private Set<Object> getCacheForSearchTerm(String searchTerm) {
        if (!this.cachedSearches.containsKey(searchTerm)) {
            this.cachedSearches.put(searchTerm, new HashSet<>());
        }

        final Set<Object> cache = this.cachedSearches.get(searchTerm);

        if (cache.isEmpty() && searchTerm.length() > 1) {
            cache.addAll(this.getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
        }

        return cache;
    }

    private void reinitialize() {
        this.children().removeAll(this.renderables);
        this.renderables.clear();
        this.init();
    }

    private void toggleTerminalStyle(SettingToggleButton<appeng.api.config.TerminalStyle> btn, boolean backwards) {
        appeng.api.config.TerminalStyle next = btn.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        btn.set(next);
        this.reinitialize();
    }

    /**
     * The max amount of unique names and each inv row. Not affected by the filtering.
     *
     * @return max amount of unique names and each inv row
     */
    private int getMaxRows() {
        return this.groups.size() + this.byId.size();
    }

    /**
     * A version of blit that lets us pass a source rectangle
     *
     * @see GuiGraphics#blit(ResourceLocation, int, int, int, int, int, int)
     */
    private void blit(GuiGraphics guiGraphics, int offsetX, int offsetY, Rect2i srcRect) {
        var texture = AppEng.makeId("textures/guis/patternaccessterminal.png");
        guiGraphics.blit(
                texture, offsetX, offsetY, srcRect.getX(), srcRect.getY(), srcRect.getWidth(), srcRect.getHeight());
    }

    protected int getVisibleRows() {
        return visibleRows;
    }

    @Override
    public void containerTick() {
        this.repo.setEnabled(this.menu.getLinkStatus().connected());

        super.containerTick();
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.AMOUNT;
    }

    @Override
    public SortDir getSortDir() {
        return SortDir.ASCENDING;
    }

    @Override
    public ViewItems getSortDisplay() {
        return ViewItems.ALL;
    }

    @Override
    public Set<AEKeyType> getSortKeyTypes() {
        return Sets.newHashSet(AEKeyTypes.getAll());
    }

    sealed interface Row {}

    /**
     * A row containing a header for a group.
     */
    record GroupHeaderRow(PatternContainerGroup group) implements PatternEncodingAccessTermScreen.Row {}

    /**
     * A row containing slots for a subset of a pattern container inventory.
     */
    record SlotsRow(PatternContainerRecord container, int offset, int slots)
            implements PatternEncodingAccessTermScreen.Row {}
}

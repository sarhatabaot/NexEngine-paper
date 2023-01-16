package su.nexmedia.engine.api.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecorationAndState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.utils.ComponentUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;

import java.util.*;

public abstract class AbstractMenu<P extends NexPlugin<P>> extends AbstractListener<P> implements ICleanable, Menu<P> {

    private static final Map<Player, AbstractMenu<?>> PLAYER_MENUS = new WeakHashMap<>();

    protected final UUID id;
    protected final Set<Player> viewers;
    protected final Map<String, MenuItem> items;
    protected final Map<Player, List<MenuItem>> userItems;
    protected final Map<Player, int[]> userPage;

    protected Component title;
    protected int size;
    protected JYML cfg;

    private MenuListener<P> listener;

    public static @Nullable AbstractMenu<?> getMenu(@NotNull Player player) {
        return PLAYER_MENUS.get(player);
    }

    public AbstractMenu(@NotNull P plugin, @NotNull JYML cfg, @NotNull String path) {
        this(plugin, cfg.getString(path + "Title", ""), cfg.getInt(path + "Size"));
        this.cfg = cfg;
    }

    public AbstractMenu(@NotNull P plugin, @NotNull String title, int size) {
        this(plugin, ComponentUtil.asComponent(title), size);
    }

    public AbstractMenu(@NotNull P plugin, @NotNull Component title, int size) {
        super(plugin);
        this.id = UUID.randomUUID();
        this.title = title;
        this.setSize(size);

        this.items = new LinkedHashMap<>();
        this.userItems = new WeakHashMap<>();
        this.userPage = new WeakHashMap<>();
        this.viewers = new HashSet<>();

        this.listener = new MenuListener<>(this);
        this.listener.registerListeners();
        this.registerListeners();
    }

    @Override
    public void clear() {
        this.viewers.forEach(Player::closeInventory);
        this.viewers.clear();
        this.items.clear();
        this.userItems.clear();
        this.userPage.clear();
        this.unregisterListeners();
        this.listener.unregisterListeners();
        this.listener = null;
        this.cfg = null;
    }

    @Override
    public void onItemClickDefault(@NotNull Player player, @NotNull MenuItemType itemType) {
        int pageMax = this.getPageMax(player);
        switch (itemType) {
            case CLOSE -> player.closeInventory();
            case PAGE_NEXT -> this.open(player, Math.min(pageMax, this.getPage(player) + 1));
            case PAGE_PREVIOUS -> this.open(player, Math.max(1, this.getPage(player) - 1));
            default -> {}
        }
    }

    @Override
    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        return true;
    }

    @Override
    public boolean onReady(@NotNull Player player, @NotNull Inventory inventory) {
        return true;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryDragEvent event) {
        return true;
    }

    @Override
    public boolean open(@NotNull Player player, int page) {
        if (player.isSleeping()) return false;

        Inventory inventory;
        if (this.isViewer(player)) {
            this.getUserItemsMap().remove(player);
            inventory = player.getOpenInventory().getTopInventory();
            inventory.clear();
        } else {
            inventory = this.createInventory();
        }

        this.setPage(player, page, page);
        if (!this.onPrepare(player, inventory)) return false;
        this.setItems(player, inventory);
        if (!this.onReady(player, inventory)) return false;
        if (this.getViewers().add(player)) {
            player.openInventory(inventory);
        }
        PLAYER_MENUS.put(player, this);
        return true;
    }

    @Override
    public void addItem(@NotNull ItemStack item, int... slots) {
        this.addItem(new MenuItemImpl(item, slots));
    }

    @Override
    public void addItem(@NotNull Player player, @NotNull ItemStack item, int... slots) {
        this.addItem(player, new MenuItemImpl(item, slots));
    }

    @Override
    public void setItems(@NotNull Player player, @NotNull Inventory inventory) {
        // Auto paginator
        int page = this.getPage(player);
        int pages = this.getPageMax(player);

        List<MenuItem> items = new ArrayList<>(this.getItemsMap().values());
        items.sort(Comparator.comparingInt(MenuItem::getPriority));
        items.addAll(this.getUserItems(player));

        for (MenuItem menuItem : items) {
            if (menuItem.getType() == MenuItemType.PAGE_NEXT) {
                if (page >= pages) {
                    continue;
                }
            }
            if (menuItem.getType() == MenuItemType.PAGE_PREVIOUS) {
                if (page <= 1) {
                    continue;
                }
            }

            ItemStack item = menuItem.getItem();
            this.onItemPrepare(player, menuItem, item);

            for (int slot : menuItem.getSlots()) {
                if (slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item);
            }
        }
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        item.editMeta(meta -> {
            // Support PAPI placeholders
            ItemUtil.setPlaceholderAPI(meta, player);

            // Minecraft shows custom displayName & lore with ITALIC by default.
            // We remove the ITALIC style, leaving it to the end-users to decide.
            TextDecorationAndState italicOff = TextDecoration.ITALIC.withState(false);
            if (meta.hasDisplayName()) {
                Component modified = meta.displayName().applyFallbackStyle(italicOff);
                meta.displayName(modified);
            }
            if (meta.hasLore()) {
                List<Component> modified = meta.lore().stream()
                    .map(line -> line.equals(Component.empty()) ? line : line.applyFallbackStyle(italicOff))
                    .toList();
                meta.lore(modified);
            }
        });
    }

    @Override
    public void onClick(@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e) {
        if (item == null || item.getType().isAir()) return;

        MenuItem menuItem = this.getItem(player, slot);
        if (menuItem == null) return;

        MenuClick click = menuItem.getClickHandler();
        if (click != null) click.click(player, menuItem.getType(), e);

        // Execute custom user actions when click button.
        ClickType clickType = ClickType.from(e);
        menuItem.getClickCommands(clickType).forEach(command -> PlayerUtil.dispatchCommand(player, command));
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        this.getUserPageMap().remove(player);
        this.getUserItemsMap().remove(player);
        this.getViewers().remove(player);

        PLAYER_MENUS.remove(player);

        if (this.getViewers().isEmpty() && this.destroyWhenNoViewers()) {
            this.clear();
        }
    }

    @Override
    public @NotNull UUID getId() {
        return id;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(@NotNull Component title) {
        this.title = title;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public @NotNull Map<String, MenuItem> getItemsMap() {
        return items;
    }

    @Override
    public @NotNull Map<Player, List<MenuItem>> getUserItemsMap() {
        return userItems;
    }

    @Override
    public @NotNull Map<Player, int[]> getUserPageMap() {
        return userPage;
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return viewers;
    }

}

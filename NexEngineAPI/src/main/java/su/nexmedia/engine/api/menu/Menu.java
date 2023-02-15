package su.nexmedia.engine.api.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.ArrayUtil;

import java.util.*;

public interface Menu<P> {

    enum SlotType {
        EMPTY_PLAYER,
        EMPTY_MENU,
        PLAYER,
        MENU
    }

    void onItemClickDefault(@NotNull Player player, @NotNull MenuItemType itemType);

    boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory);

    boolean onReady(@NotNull Player player, @NotNull Inventory inventory);

    boolean cancelClick(@NotNull InventoryClickEvent event, @NotNull SlotType slotType);

    boolean cancelClick(@NotNull InventoryDragEvent event);

    boolean open(@NotNull Player player, int page);

    default void update() {
        this.getViewers().forEach(player -> this.open(player, this.getPage(player)));
    }

    void setItems(@NotNull Player player, @NotNull Inventory inventory);

    void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item);

    void onClick(@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e);

    void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e);

    default boolean isViewer(@NotNull Player player) {
        return this.getViewers().contains(player);
    }

    @NotNull Component getTitle(@NotNull Player player);

    @NotNull InventoryType getInventoryType();

    void setInventoryType(@NotNull InventoryType inventoryType);

    default @NotNull Inventory createInventory() {
        return Bukkit.getServer().createInventory(null, this.getSize(), this.getTitle());
    }

    default @NotNull Inventory createInventory(@NotNull Player player) {
        Component title = this.getTitle(player);
        if (this.getInventoryType() == InventoryType.CHEST) {
            return Bukkit.getServer().createInventory(null, this.getSize(), title);
        } else {
            return Bukkit.getServer().createInventory(null, this.getInventoryType(), title);
        }
    }

    default @NotNull List<MenuItem> getUserItems(@NotNull Player player) {
        return this.getUserItemsMap().computeIfAbsent(player, p -> new ArrayList<>());
    }

    default @Nullable MenuItem getItem(@NotNull String id) {
        return this.getItemsMap().get(id.toLowerCase());
    }

    default @Nullable MenuItem getItem(int slot) {
        return this.getItemsMap().values().stream()
            .filter(item -> ArrayUtil.contains(item.getSlots(), slot))
            .max(Comparator.comparingInt(MenuItem::getPriority))
            .orElse(null);
    }

    default @Nullable MenuItem getItem(@NotNull Player player, int slot) {
        return this.getUserItems(player).stream()
            .filter(item -> ArrayUtil.contains(item.getSlots(), slot))
            .max(Comparator.comparingInt(MenuItem::getPriority))
            .orElse(this.getItem(slot));
    }

    void addItem(@NotNull ItemStack item, int... slots);

    void addItem(@NotNull Player player, @NotNull ItemStack item, int... slots);

    default void addItem(@NotNull MenuItem menuItem) {
        this.getItemsMap().put(menuItem.getId(), menuItem);
    }

    default void addItem(@NotNull Player player, @NotNull MenuItem menuItem) {
        this.getUserItems(player).add(menuItem);
    }

    default int getPage(@NotNull Player player) {
        return this.getUserPageMap().getOrDefault(player, new int[]{-1, -1})[0];
    }

    default int getPageMax(@NotNull Player player) {
        return this.getUserPageMap().getOrDefault(player, new int[]{-1, -1})[1];
    }

    default void setPage(@NotNull Player player, int pageCurrent, int pageMax) {
        pageCurrent = Math.max(1, pageCurrent);
        pageMax = Math.max(1, pageMax);
        this.getUserPageMap().put(player, new int[]{Math.min(pageCurrent, pageMax), pageMax});
    }

    @NotNull UUID getId();

    @NotNull Component getTitle();

    void setTitle(@NotNull Component title);

    int getSize();

    void setSize(int size);

    @NotNull Map<String, MenuItem> getItemsMap();

    @NotNull Map<Player, List<MenuItem>> getUserItemsMap();

    @NotNull Map<Player, int[]> getUserPageMap();

    @NotNull Set<Player> getViewers();

    default boolean destroyWhenNoViewers() {
        return false;
    }

}

package su.nexmedia.engine.api.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.type.ClickType;

import java.util.*;

public class MenuItemImpl implements MenuItem {

    protected final String  id;
    protected final Enum<?> type;

    protected int                          priority;
    protected ItemStack                    item;
    protected int[]                        slots;
    protected MenuClick                    clickHandler;
    protected Map<ClickType, List<String>> clickCommands;

    public MenuItemImpl(@NotNull ItemStack item) {
        this(item, new int[0]);
    }

    public MenuItemImpl(@NotNull ItemStack item, int... slots) {
        this(item, null, slots);
    }

    public MenuItemImpl(@NotNull ItemStack item, @Nullable Enum<?> type, int... slots) {
        this(UUID.randomUUID().toString(), item, type, slots);
    }

    public MenuItemImpl(@NotNull String id, @NotNull ItemStack item, int... slots) {
        this(id, item, null, slots);
    }

    public MenuItemImpl(@NotNull String id, @NotNull ItemStack item, @Nullable Enum<?> type, int... slots) {
        this(id, type, slots, 0, item, new HashMap<>());
    }

    public MenuItemImpl(@NotNull MenuItem menuItem) {
        this(menuItem.getId(), menuItem.getType(), menuItem.getSlots(), menuItem.getPriority(), menuItem.getItem(), menuItem.getClickCommands());
    }

    public MenuItemImpl(
        @NotNull String id, @Nullable Enum<?> type, int[] slots, int priority,
        @NotNull ItemStack item,
        @NotNull Map<ClickType, List<String>> clickCommands) {
        this.id = id.toLowerCase();
        this.type = type;
        this.setPriority(priority);
        this.setSlots(slots);
        this.setItem(item);
        this.clickCommands = clickCommands;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @Nullable Enum<?> getType() {
        return type;
    }

    @Override
    public int[] getSlots() {
        return slots;
    }

    @Override
    public void setSlots(int... slots) {
        this.slots = slots;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public @NotNull ItemStack getItem() {
        return new ItemStack(this.item);
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @Override
    public @Nullable MenuClick getClickHandler() {
        return clickHandler;
    }

    @Override
    public void setClickHandler(@Nullable MenuClick clickHandler) {
        this.clickHandler = clickHandler;
    }

    @Override
    public @NotNull Map<ClickType, List<String>> getClickCommands() {
        return clickCommands;
    }

}

package su.nexmedia.engine.hooks.item;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.item.PluginItem;
import su.nexmedia.engine.api.manager.ILogger;
import su.nexmedia.engine.hooks.Hooks;

public class ItemsAdderItem extends PluginItem<CustomStack> {

    public ItemsAdderItem(final ILogger logger) {
        super(logger);
    }

    @Override public boolean available() {
        return Hooks.hasItemsAdder();
    }

    @Override
    public @Nullable CustomStack getPluginItem() {
        return CustomStack.getInstance(getItemId());
    }

    @Override
    public @Nullable ItemStack createItemStack() {
        if (getPluginItem() == null) return null;
        ItemStack itemStack = getPluginItem().getItemStack();
        itemStack.setAmount(1);
        return itemStack;
    }

    @Override
    public @Nullable ItemStack createItemStack(@NotNull Player player) {
        return createItemStack();
    }

    @Override
    public boolean matches(@NotNull ItemStack item) {
        CustomStack other = CustomStack.byItemStack(item);
        if (other == null) return false;
        return getItemId().equalsIgnoreCase(other.getNamespacedID());
    }

    @Override
    public boolean belongs(@NotNull ItemStack item) {
        return CustomStack.byItemStack(item) != null;
    }

    @Override
    public @Nullable String toItemId(@NotNull ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        if (stack == null) return null;
        return stack.getNamespacedID();
    }

}

package su.nexmedia.engine.api.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PluginItem<T> {

    private String plugin;
    private String itemId;

    protected PluginItem() {
    }

    /**
     * It's called every time this plugin item instance is instantiated.
     * <p>
     * When it's called, the {@link #plugin} and {@link #itemId} are already set.
     */
    protected void onConstruct() {
    }

    /**
     * Gets the Plugin ID of this Plugin Item (always lowercase).
     *
     * @return the Plugin ID
     */
    public @NotNull String getPlugin() {
        return plugin;
    }

    /**
     * Gets the Item ID of this Plugin Item (always lowercase).
     * <p>
     * The format of Item ID is implementation-defined.
     *
     * @return the Item ID
     */
    public @NotNull String getItemId() {
        return itemId;
    }

    /**
     * Sets the Plugin ID of this Plugin Item.
     *
     * @param plugin the Plugin ID
     */
    public void setPlugin(@NotNull String plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets the Item ID of this Plugin Item.
     *
     * @param itemId the Item ID
     */
    public void setItemId(@NotNull String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets an instance of the plugin item from the external plugin codebase. The implementation is expected to use
     * {@link #getPlugin()} and {@link #getItemId()} to get the specific plugin item instance from the database of the
     * external plugin.
     *
     * @return the plugin item instance
     */
    abstract public @Nullable T getPluginItem();

    /**
     * @return the item stack generated from this plugin item
     */
    abstract public @Nullable ItemStack createItemStack();

    /**
     * @return the item stack generated from this plugin item, varying depending on the given player
     */
    abstract public @Nullable ItemStack createItemStack(@NotNull Player player);

    /**
     * Check whether the specific itemStack matches this plugin item.
     *
     * @param item the itemStack to compare with
     *
     * @return true if the given itemStack is this plugin item, otherwise false
     */
    abstract public boolean matches(@NotNull ItemStack item);

    /**
     * Check whether the specific itemStack is from this plugin.
     *
     * @param item the itemStack to compare with
     *
     * @return true if the given itemStack is from this plugin, otherwise false
     */
    abstract public boolean belongs(@NotNull ItemStack item);

    /**
     * Generate the config reference (format: {pluginName}:{itemId}) from the specific itemStack.
     *
     * @param item the itemStack to be converted into the config reference
     *
     * @return the config reference from this itemStack, or <code>null</code> if this is not a custom plugin item
     */
    abstract public @Nullable String ofItemId(@NotNull ItemStack item);

}

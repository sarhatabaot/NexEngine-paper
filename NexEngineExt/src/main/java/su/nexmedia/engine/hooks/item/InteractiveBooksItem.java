package su.nexmedia.engine.hooks.item;

import net.leonardo_dgs.interactivebooks.IBook;
import net.leonardo_dgs.interactivebooks.InteractiveBooks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.item.PluginItem;
import su.nexmedia.engine.api.manager.ILogger;
import su.nexmedia.engine.hooks.Hooks;

public class InteractiveBooksItem extends PluginItem<IBook> {

    public InteractiveBooksItem(final ILogger logger) {
        super(logger);
    }

    @Override public boolean available() {
        return Hooks.hasInteractiveBooks();
    }

    @Override
    public @Nullable IBook getPluginItem() {
        IBook book = InteractiveBooks.getBook(getItemId());
        if (book == null) {
            logger().error("[%s] Cannot found item with ID: %s".formatted(getPlugin(), getItemId()));
            return null;
        }
        return book;
    }

    @Override
    public @Nullable ItemStack createItemStack() {
        IBook pluginItem = getPluginItem();
        if (pluginItem == null) return null;
        ItemStack item = pluginItem.getItem();
        item.setAmount(1);
        return item;
    }

    @Override
    public @Nullable ItemStack createItemStack(@NotNull Player player) {
        IBook pluginItem = getPluginItem();
        if (pluginItem == null) return null;
        ItemStack item = pluginItem.getItem(player);
        item.setAmount(1);
        return item;
    }

    @Override
    public boolean matches(@NotNull ItemStack item) {
        IBook book = InteractiveBooks.getBook(item);
        if (book == null) return false;
        return book.getId().equalsIgnoreCase(getItemId());
    }

    @Override
    public boolean belongs(@NotNull ItemStack item) {
        return item.getType() == Material.WRITTEN_BOOK && InteractiveBooks.getBook(item) != null;
    }

    @Override
    public @Nullable String toItemId(@NotNull ItemStack item) {
        IBook book = InteractiveBooks.getBook(item);
        if (book == null) return null;
        return book.getId();
    }

}

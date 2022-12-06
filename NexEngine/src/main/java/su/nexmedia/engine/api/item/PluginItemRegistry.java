package su.nexmedia.engine.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class PluginItemRegistry {

    private static final Map<String, Supplier<PluginItem<?>>> constructors = new HashMap<>();

    public static void registerForConfig(@NotNull String pluginId, @NotNull Supplier<PluginItem<?>> constructor) {
        constructors.put(pluginId.toLowerCase(), constructor);
    }

    public static void unregisterForConfig(@NotNull String pluginId) {
        constructors.remove(pluginId.toLowerCase());
    }

    public static @Nullable PluginItem<?> fromItemStackNullable(@Nullable ItemStack item) {
        if (item == null) return null;
        for (Map.Entry<String, Supplier<PluginItem<?>>> entry : constructors.entrySet()) {
            PluginItem<?> pluginItem = entry.getValue().get();
            if (pluginItem.belongs(item)) {
                String plugin = Objects.requireNonNull(entry.getKey());
                String itemId = Objects.requireNonNull(pluginItem.ofItemId(item));
                pluginItem.setPlugin(plugin);
                pluginItem.setItemId(itemId);
                pluginItem.onConstruct();
                return pluginItem;
            }
        }
        return null;
    }

    public static @NotNull PluginItem<?> fromItemStack(@NotNull ItemStack item) {
        PluginItem<?> pluginItem = fromItemStackNullable(item);
        if (pluginItem == null) throw new NullPointerException();
        return pluginItem;
    }

    public static @Nullable PluginItem<?> fromConfigNullable(@Nullable String plugin, @Nullable String itemId) {
        if (plugin == null || itemId == null) return null;
        plugin = plugin.toLowerCase();
        itemId = itemId.toLowerCase();
        if (constructors.containsKey(plugin)) {
            PluginItem<?> item = constructors.get(plugin).get();
            item.setPlugin(plugin);
            item.setItemId(itemId);
            item.onConstruct();
            return item;
        }
        NexEngine.get().error("Unsupported plugin item '" + itemId + "' from external plugin '" + plugin + "'."
                              + " Remove this config line if you don't have the external plugin installed");
        return null;
    }

    public static @NotNull PluginItem<?> fromConfig(@NotNull String plugin, @NotNull String itemId) {
        return Objects.requireNonNull(fromConfigNullable(plugin, itemId));
    }

    public static @Nullable PluginItem<?> fromConfigNullable(@Nullable String reference) {
        if (!isPluginItemId(reference)) {
            NexEngine.get().error("The format of plugin item ID '" + reference + "' is not correct. Correct format: {plugin}:{itemId}");
            return null;
        }
        String[] split = toPluginItemId(reference);
        return fromConfig(split[0], split[1]);
    }

    public static @NotNull PluginItem<?> fromConfig(@NotNull String reference) {
        return Objects.requireNonNull(fromConfigNullable(reference));
    }

    public static @Nullable String toReference(@Nullable ItemStack item) {
        PluginItem<?> pluginItem = fromItemStackNullable(item);
        if (pluginItem == null) return null;
        return pluginItem.getPlugin() + ":" + pluginItem.getItemId();
    }

    public static boolean isPluginItemId(@Nullable String reference) {
        return reference != null && toPluginItemId(reference).length == 2;
    }

    @Contract(pure = true)
    private static @NotNull String[] toPluginItemId(@NotNull String reference) {
        return reference.split(":", 2);
    }

}

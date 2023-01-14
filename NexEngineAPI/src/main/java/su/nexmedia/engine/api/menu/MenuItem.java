package su.nexmedia.engine.api.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.type.ClickType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface MenuItem {

    @NotNull String getId();

    @Nullable Enum<?> getType();

    int[] getSlots();

    void setSlots(int... slots);

    int getPriority();

    void setPriority(int priority);

    @NotNull ItemStack getItem();

    void setItem(@NotNull ItemStack item);

    @Nullable MenuClick getClickHandler();

    void setClickHandler(@Nullable MenuClick clickHandler);

    @NotNull Map<ClickType, List<String>> getClickCommands();

    default @NotNull List<String> getClickCommands(@NotNull ClickType clickType) {
        return this.getClickCommands().getOrDefault(clickType, Collections.emptyList());
    }

}

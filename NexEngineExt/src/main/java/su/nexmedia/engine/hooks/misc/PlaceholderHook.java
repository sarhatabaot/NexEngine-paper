package su.nexmedia.engine.hooks.misc;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

import static su.nexmedia.engine.utils.ComponentUtil.asComponent;

public class PlaceholderHook {

    /**
     * Sets the PAPI placeholders in given component.
     *
     * @param player the player
     * @param text   the component to set placeholders
     *
     * @return a modified copy of the component
     */
    @Contract(pure = true)
    public static @NotNull Component setPlaceholders(@NotNull OfflinePlayer player, @NotNull Component text) {
        return text.replaceText(config -> config
            .match(PlaceholderHook.getPlacehodlerPattern())
            .replacement((matchResult, builder) -> {
                String matched = matchResult.group();
                String replaced = PlaceholderHook.setPlaceholders(player, matched);
                return asComponent(replaced);
            })
        );
    }

    public static @NotNull String setPlaceholders(@Nullable OfflinePlayer player, @NotNull String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static @NotNull Pattern getPlacehodlerPattern() {
        return PlaceholderAPI.getPlaceholderPattern();
    }

}

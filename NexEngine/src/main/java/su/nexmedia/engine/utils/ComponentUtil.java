package su.nexmedia.engine.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.hooks.Hooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ComponentUtil {

    @Contract(pure = true)
    public static @NotNull Component asComponent(@NotNull String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    @Contract(pure = true)
    public static @NotNull List<Component> asComponent(@NotNull List<String> miniMessage) {
        return miniMessage.stream().map(ComponentUtil::asComponent).collect(Collectors.toList());
    }

    @Contract(pure = true)
    public static @NotNull String asMiniMessage(@NotNull Component component) {
        return MiniMessage.miniMessage().serialize(component.compact());
    }

    @Contract(pure = true)
    public static @NotNull List<String> asMiniMessage(@NotNull List<Component> component) {
        return component.stream().map(ComponentUtil::asMiniMessage).collect(Collectors.toList());
    }

    @Contract(pure = true)
    public static @NotNull String asPlainText(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Contract(pure = true)
    public static @NotNull List<Component> stripEmpty(@NotNull List<Component> original) {
        List<Component> stripped = new ArrayList<>();
        for (int index = 0; index < original.size(); index++) {
            Component originLine = original.get(index);
            String plainLine = ComponentUtil.asPlainText(originLine);
            if (plainLine.isEmpty()) {
                String last = stripped.isEmpty() ? null : ComponentUtil.asPlainText(stripped.get(stripped.size() - 1));
                if (last == null || last.isEmpty() || index == (original.size() - 1)) continue;
            }
            stripped.add(originLine);
        }
        return stripped;
    }

    /**
     * Strips all MiniMessage tags from given text.
     *
     * @param miniMessage a text in MiniMessage format
     *
     * @return a plain text
     */
    @Contract(pure = true)
    public static @NotNull String stripTags(@NotNull String miniMessage) {
        return MiniMessage.miniMessage().stripTags(miniMessage);
    }

    /**
     * Sets the PAPI placeholders in given component.
     *
     * @param player    the player
     * @param component the component to set placeholders
     *
     * @return a modified copy of the component
     */
    @Contract(pure = true)
    public static Component setPlaceholderAPI(@NotNull Player player, @NotNull Component component) {
        if (!Hooks.hasPlaceholderAPI()) return component;
        Pattern pattern = PlaceholderAPI.getPlaceholderPattern();
        return component.replaceText(config -> config
            .match(pattern)
            .replacement((matchResult, builder) -> {
                String matched = matchResult.group();
                String replaced = PlaceholderAPI.setPlaceholders(player, matched);
                return ComponentUtil.asComponent(replaced);
            })
        );
    }

    @Contract(pure = true)
    public static @NotNull Component replace(@NotNull Component component, String literal, String replacement) {
        return replace(component, literal, asComponent(replacement));
    }

    @Contract(pure = true)
    public static @NotNull Component replace(@NotNull Component component, String literal, Component replacement) {
        return component.replaceText(config -> config
            .matchLiteral(literal)
            .replacement(replacement)
        );
    }

    /**
     * Applies the string replacer to given component.
     *
     * @param replacer  a string replacer
     * @param component a component which the string replacer applies to
     *
     * @return a modified copy of the component
     */
    @Contract(pure = true)
    public static @NotNull Component replace(@NotNull Component component, @NotNull UnaryOperator<String> replacer) {
        return component.replaceText(config -> config
            .match(IPlaceholder.PERCENT_PATTERN)
            .replacement((matchResult, builder) -> {
                String matched = matchResult.group();
                String replaced = replacer.apply(matched);
                return ComponentUtil.asComponent(replaced);
            })
        );
    }

    /**
     * Applies the string replacer to given component list.
     *
     * @param replacer  a string replacer
     * @param component a component list which the string replacer applies to
     *
     * @return a modified copy of the list
     */
    @Contract(pure = true)
    public static @NotNull List<Component> replace(@NotNull List<Component> component, @NotNull UnaryOperator<String> replacer) {
        List<Component> replaced = new ArrayList<>();
        for (Component line : component) {
            replaced.add(replace(line, replacer));
        }
        return replaced;
    }

    /**
     * Modifies the list of components such that the new list has the given placeholder replaced by the given replacer.
     *
     * @param original    the original list of components to which the replacement is applied
     * @param placeholder the placeholder contained in the list of components
     * @param keep        true to keep other contents around the placeholder
     * @param replacer    the new component replacing the placeholder
     *
     * @return a modified copy of the list
     */
    @Contract(pure = true)
    public static @NotNull List<Component> replace(@NotNull List<Component> original, @NotNull String placeholder, boolean keep, Component... replacer) {
        return ComponentUtil.replace(original, placeholder, keep, Arrays.asList(replacer));
    }

    /**
     * Modifies the list of components such that the new list has the given placeholder replaced by the given replacer.
     *
     * @param original    the original list of components to which the replacement is applied
     * @param placeholder the placeholder contained in the list of components
     * @param keep        true to keep other contents around the placeholder
     * @param replacer    the new component replacing the placeholder
     *
     * @return a modified copy of the list
     */
    @Contract(pure = true)
    public static @NotNull List<Component> replace(@NotNull List<Component> original, @NotNull String placeholder, boolean keep, List<Component> replacer) {
        List<Component> replaced = new ArrayList<>();
        for (Component line : original) {
            if (ComponentUtil.asPlainText(line).contains(placeholder)) {
                if (!keep) {
                    replaced.addAll(replacer);
                }
                else {
                    for (Component lineReplaced : replacer) {
                        replaced.add(line.replaceText(config -> {
                            config.matchLiteral(placeholder);
                            config.replacement(lineReplaced);
                        }));
                    }
                }
                continue;
            }
            replaced.add(line);
        }

        return replaced;
    }

}

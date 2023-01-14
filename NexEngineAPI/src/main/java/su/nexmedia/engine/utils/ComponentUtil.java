package su.nexmedia.engine.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class ComponentUtil {

    /**
     * Converts the MiniMessage string into a component.
     *
     * @param miniMessage a MiniMessage string
     *
     * @return a component
     */
    @Contract(pure = true)
    public static @NotNull Component asComponent(@NotNull String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    /**
     * Converts the list of MiniMessage strings into a list of components.
     *
     * @param miniMessage a list of a MiniMessage strings
     *
     * @return a list of components
     */
    @Contract(pure = true)
    public static @NotNull List<Component> asComponent(@NotNull List<String> miniMessage) {
        return miniMessage.stream().map(ComponentUtil::asComponent).toList();
    }

    /**
     * Converts the component into a MiniMessage string.
     *
     * @param component a component
     *
     * @return a string in MiniMessage representation
     */
    @Contract(pure = true)
    public static @NotNull String asMiniMessage(@NotNull Component component) {
        return MiniMessage.miniMessage().serialize(component.compact());
    }

    /**
     * Converts the list of components into a list of MiniMessage strings.
     *
     * @param component a list of components
     *
     * @return a list of strings in MiniMessage representation
     */
    @Contract(pure = true)
    public static @NotNull List<String> asMiniMessage(@NotNull List<Component> component) {
        return component.stream().map(ComponentUtil::asMiniMessage).toList();
    }

    /**
     * Converts the component into a plain text, without any text decorations.
     *
     * @param component a component
     *
     * @return a plain text
     */
    @Contract(pure = true)
    public static @NotNull String asPlainText(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Removes consecutive "empty components" in a list, leaving only a single empty component for any group of
     * consecutive "empty components" found. "Empty components" are those where the plain content string is
     * <code>""</code>.
     *
     * @param componentList a list of components which may contain empty lines
     *
     * @return a modified copy of the list
     */
    @Contract(pure = true)
    public static @NotNull List<Component> stripEmpty(@NotNull List<Component> componentList) {
        List<Component> stripped = new ArrayList<>();
        for (int index = 0; index < componentList.size(); index++) {
            Component originLine = componentList.get(index);
            String plainLine = asPlainText(originLine);
            if (plainLine.isEmpty()) {
                String last = stripped.isEmpty() ? null : asPlainText(stripped.get(stripped.size() - 1));
                if (last == null || last.isEmpty() || index == (componentList.size() - 1)) continue;
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
                return asComponent(replaced);
            })
        );
    }

    /**
     * Applies the string replacer to each component of the list.
     *
     * @param replacer      a string replacer
     * @param componentList a list of components which the string replacer applies to
     *
     * @return a modified copy of the list
     */
    @Contract(pure = true)
    public static @NotNull List<Component> replace(@NotNull List<Component> componentList, @NotNull UnaryOperator<String> replacer) {
        List<Component> replaced = new ArrayList<>();
        for (Component line : componentList) {
            replaced.add(replace(line, replacer));
        }
        return replaced;
    }

    /**
     * @see #replace(List, String, boolean, List)
     */
    @Contract(pure = true)
    public static @NotNull List<Component> replace(@NotNull List<Component> original, @NotNull String placeholder, boolean keep, Component... replacer) {
        return replace(original, placeholder, keep, Arrays.asList(replacer));
    }

    /**
     * Modifies the list of components such that the new list has the given placeholder replaced by the given replacer.
     *
     * @param oldList     the list of components to which the replacement is applied
     * @param placeholder the placeholder contained in the list of components
     * @param keep        true to keep other contents around the placeholder
     * @param replacer    the new list of components replacing the placeholder
     *
     * @return a modified copy of the list
     */
    @Contract(pure = true)
    public static @NotNull List<Component> replace(@NotNull List<Component> oldList, @NotNull String placeholder, boolean keep, List<Component> replacer) {
        List<Component> replaced = new ArrayList<>();
        for (Component oldLine : oldList) {
            if (asPlainText(oldLine).contains(placeholder)) {
                if (!keep) {
                    replaced.addAll(replacer);
                } else {
                    for (Component lineReplaced : replacer) {
                        replaced.add(oldLine.replaceText(config -> {
                            config.matchLiteral(placeholder);
                            config.replacement(lineReplaced);
                        }));
                    }
                }
                continue;
            }
            replaced.add(oldLine);
        }

        return replaced;
    }

}

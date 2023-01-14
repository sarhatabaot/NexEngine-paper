package su.nexmedia.engine.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IPlaceholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class ComponentUtil {

    /**
     * Converts the MiniMessage string into a component.
     * <p>
     * It will return a {@link Component#empty()} if the given string is null.
     *
     * @param miniMessage a MiniMessage string
     *
     * @return a component
     */
    @Contract(pure = true)
    public static @NotNull Component asComponent(@Nullable String miniMessage) {
        if (miniMessage == null) return Component.empty();
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    /**
     * Converts the list of MiniMessage strings into a list of components.
     * <p>
     * It will return a {@link List#of()} if the given string is null.
     *
     * @param miniMessage a list of a MiniMessage strings
     *
     * @return a list of components
     */
    @Contract(pure = true)
    public static @NotNull List<Component> asComponent(@Nullable List<String> miniMessage) {
        if (miniMessage == null) return List.of();
        return miniMessage.stream().map(ComponentUtil::asComponent).toList();
    }

    /**
     * Converts the component into a MiniMessage string.
     * <p>
     * It will return an empty string if the given component is null.
     *
     * @param component a component
     *
     * @return a string in MiniMessage representation
     */
    @Contract(pure = true)
    public static @NotNull String asMiniMessage(@Nullable Component component) {
        if (component == null) return "";
        return MiniMessage.miniMessage().serialize(component.compact());
    }

    /**
     * Converts the list of components into a list of MiniMessage strings.
     * <p>
     * It will return a {@link List#of()} if the given list is null.
     *
     * @param component a list of components
     *
     * @return a list of strings in MiniMessage representation
     */
    @Contract(pure = true)
    public static @NotNull List<String> asMiniMessage(@Nullable List<Component> component) {
        if (component == null) return List.of();
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
    @SafeVarargs
    @Contract(pure = true)
    public static @NotNull Component replace(@NotNull Component component, @NotNull UnaryOperator<String>... replacer) {
        return component.replaceText(config -> config
            .match(IPlaceholder.PERCENT_PATTERN)
            .replacement((matchResult, builder) -> {
                String replaced = matchResult.group();
                for (final UnaryOperator<String> re : replacer) {
                    replaced = re.apply(replaced);
                }
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
    @SafeVarargs
    @Contract(pure = true)
    public static @NotNull List<Component> replace(@NotNull List<Component> componentList, @NotNull UnaryOperator<String>... replacer) {
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
     * Modifies the list of components such that the given placeholder is replaced by the given replacer.
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

    /**
     * Inserts the src list into the dst list, at the position where the given placeholder presents.
     * <p>
     * Note that only the first placeholder encountered will be replaced.
     *
     * @param placeholder the placeholder in the lore
     * @param dst         the lore which contains the placeholder to be modified
     * @param src         the lore to be copied and inserted into the dst lore
     *
     * @return a modified copy of the dst list
     */
    @Contract("_, null, _ -> null; _, !null, _ -> !null ")
    public static List<Component> replacePlaceholderList(@NotNull String placeholder, @Nullable List<Component> dst, @NotNull List<Component> src) {
        if (dst == null) return null;

        // Component is complex. We use plain text to find the pos of the placeholder
        List<String> dst$plain = dst.stream().map(ComponentUtil::asPlainText).toList();

        // Let's find the index of placeholder in dst
        int placeholderIdx = -1;
        for (int i = 0; i < dst$plain.size(); i++) {
            if (ComponentUtil.asPlainText(dst.get(i)).contains(placeholder)) {
                placeholderIdx = i;
                break;
            }
        }
        if (placeholderIdx == -1) return dst;

        // Insert the src into the dst
        List<Component> result = new ArrayList<>(dst);
        // Need to remove the raw placeholder from dst
        result.remove(placeholderIdx);
        result.addAll(placeholderIdx, src);
        // for (final Component line : Lists.reverse(src)) {
        //     result.add(placeholderIdx, line);
        // }
        return result;
    }

}

package su.nexmedia.engine.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.random.Rnd;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    public static final char AMPERSAND_CHAR = LegacyComponentSerializer.AMPERSAND_CHAR;

    @Deprecated public static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    public static @NotNull String oneSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    public static @NotNull String noSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", "");
    }

    /**
     * Translates ampersand ({@code &}) color codes into section ({@code §}) color codes.
     * <p>
     * The translation supports three different RGB formats: 1) Legacy Mojang color and formatting codes (such as §a or
     * §l), 2) Adventure-specific RGB format (such as §#a25981) and  3) BungeeCord RGB color code format (such as
     * §x§a§2§5§9§8§1).
     *
     * @param str a legacy text where its color codes are in <b>ampersand</b> {@code &} format
     *
     * @return a legacy text where its color codes are in <b>section</b> {@code §} format
     */
    public static @NotNull String color(@NotNull String str) {
        return LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacy(AMPERSAND_CHAR).deserialize(str));
    }

    public static @NotNull Component asComponent(@NotNull String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    public static @NotNull List<Component> asComponent(@NotNull List<String> miniMessage) {
        return miniMessage.stream().map(StringUtil::asComponent).collect(Collectors.toList());
    }

    public static @NotNull String asMiniMessage(@NotNull Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static @NotNull List<String> asMiniMessage(@NotNull List<Component> component) {
        return component.stream().map(StringUtil::asMiniMessage).collect(Collectors.toList());
    }

    /**
     * Strips all MiniMessage tags from given text.
     *
     * @param miniMessage a text in MiniMessage format
     *
     * @return a plain text
     */
    public static @NotNull String stripTags(@NotNull String miniMessage) {
        return MiniMessage.miniMessage().stripTags(miniMessage);
    }

    /**
     * Converts given Component into plain text.
     *
     * @param component a text of Component
     *
     * @return a plain text
     */
    public static @NotNull String asPlainText(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Converts given legacy text into plain text.
     *
     * @param legacy a text containing legacy color codes
     *
     * @return a plain text
     */
    public static @NotNull String asPlainText(@NotNull String legacy) {
        return PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(legacy));
    }

    /**
     * Sets the PAPI placeholders in given component.
     *
     * @param player the player
     * @param component the component to set placeholders
     *
     * @return the component with all placeholders parsed
     */
    public static Component setPlaceholderAPI(@NotNull Player player, @NotNull Component component) {
        if (!Hooks.hasPlaceholderAPI()) return component;

        Pattern pattern = PlaceholderAPI.getPlaceholderPattern();
        return component.replaceText(config -> {
            config.match(pattern);
            config.replacement((matchResult, builder) -> {
                String matched = matchResult.group();
                String replaced = PlaceholderAPI.setPlaceholders(player, matched);
                return StringUtil.asComponent(replaced);
            });
        });
    }

    /**
     * Applies the string replacer to given component.
     *
     * @param replacer a string replacer
     * @param component a component which the string replacer applies to
     *
     * @return the component modified by the string replacer
     */
    @Contract(pure = true)
    public static @NotNull Component applyStringReplacer(@NotNull UnaryOperator<String> replacer, @NotNull Component component) {
        return component.replaceText(config -> {
            config.match(IPlaceholder.PERCENT_PATTERN);
            config.replacement((matchResult, builder) -> {
                String matched = matchResult.group();
                String replaced = replacer.apply(matched);
                return StringUtil.asComponent(replaced);
            });
        });
    }

    /**
     * Applies the string replacer to given component list.
     *
     * @param replacer a string replacer
     * @param component a component list which the string replacer applies to
     *
     * @return the component list modified by the string replacer
     */
    public static @NotNull List<Component> applyStringReplacer(@NotNull UnaryOperator<String> replacer, @NotNull List<Component> component) {
        List<Component> replaced = new ArrayList<>();
        for (Component line : component) {
            replaced.add(applyStringReplacer(replacer, line));
        }
        return replaced;
    }

    /**
     * Removes multiple color codes that are 'color of color'. Example: {@code &a&b&cText} -> {@code &cText}.
     *
     * @param str a string to fix
     * @return a string with a proper color codes formatting
     */
    @Deprecated
    public static @NotNull String colorFix(@NotNull String str) {
        return NexEngine.get().getNMS().fixColors(str);
    }

    public static @NotNull Color parseColor(@NotNull String colorRaw) {
        String[] rgb = colorRaw.split(",");
        int red = StringUtil.getInteger(rgb[0], 0);
        if (red < 0) red = Rnd.get(255);

        int green = rgb.length >= 2 ? StringUtil.getInteger(rgb[1], 0) : 0;
        if (green < 0) green = Rnd.get(255);

        int blue = rgb.length >= 3 ? StringUtil.getInteger(rgb[2], 0) : 0;
        if (blue < 0) blue = Rnd.get(255);

        return Color.fromRGB(red, green, blue);
    }

    /**
     * @deprecated in favor of {@link #color(String)}
     */
    @Deprecated
    public static @NotNull String colorHex(@NotNull String str) {
        return color(str);
    }

    public static @NotNull String colorHexRaw(@NotNull String str) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(LegacyComponentSerializer.legacySection().deserialize(str));
    }

    public static @NotNull String colorRaw(@NotNull String str) {
        return str.replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR);
    }

    /**
     * @deprecated in favor of {@link #asPlainText(String)}
     */
    @Deprecated
    public static @NotNull String colorOff(@NotNull String str) {
        return StringUtil.asPlainText(str);
    }

    @Deprecated
    public static @NotNull List<String> color(@NotNull List<String> list) {
        list.replaceAll(StringUtil::color);
        return list;
    }

    @Deprecated
    public static @NotNull Set<String> color(@NotNull Set<String> list) {
        return new HashSet<>(StringUtil.color(new ArrayList<>(list)));
    }

    public static @NotNull List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, boolean keep, String... replacer) {
        return StringUtil.replace(orig, placeholder, keep, Arrays.asList(replacer));
    }

    public static @NotNull List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, boolean keep, List<String> replacer) {
        List<String> replaced = new ArrayList<>();
        for (String line : orig) {
            if (line.contains(placeholder)) {
                if (!keep) {
                    replaced.addAll(replacer);
                }
                else {
                    replacer.forEach(lineRep -> replaced.add(line.replace(placeholder, lineRep)));
                }
                continue;
            }
            replaced.add(line);
        }

        return replaced;
    }

    public static double getDouble(@NotNull String input, double def) {
        return getDouble(input, def, false);
    }

    public static double getDouble(@NotNull String input, double def, boolean allowNega) {
        try {
            double amount = Double.parseDouble(input);
            if (amount < 0.0 && !allowNega) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public static int getInteger(@NotNull String input, int def) {
        return getInteger(input, def, false);
    }

    public static int getInteger(@NotNull String input, int def, boolean nega) {
        return (int) getDouble(input, def, nega);
    }

    public static int[] getIntArray(@NotNull String str) {
        String[] raw = str.replaceAll("\\s", "").split(",");
        int[] slots = new int[raw.length];
        for (int i = 0; i < raw.length; i++) {
            try {
                slots[i] = Integer.parseInt(raw[i].trim());
            } catch (NumberFormatException ignored) {}
        }
        return slots;
    }

    public static @NotNull String capitalizeFully(@NotNull String str) {
        if (str.length() != 0) {
            str = str.toLowerCase();
            return capitalize(str);
        }
        return str;
    }

    public static @NotNull String capitalize(@NotNull String str) {
        if (str.length() != 0) {
            int strLen = str.length();
            StringBuilder buffer = new StringBuilder(strLen);
            boolean capitalizeNext = true;

            for (int i = 0; i < strLen; ++i) {
                char ch = str.charAt(i);
                if (Character.isWhitespace(ch)) {
                    buffer.append(ch);
                    capitalizeNext = true;
                }
                else if (capitalizeNext) {
                    buffer.append(Character.toTitleCase(ch));
                    capitalizeNext = false;
                }
                else {
                    buffer.append(ch);
                }
            }
            return buffer.toString();
        }
        return str;
    }

    public static @NotNull String capitalizeFirstLetter(@NotNull String original) {
        if (original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    /**
     * @param original a list to remove empty lines from
     * @return a list with no multiple empty lines in a row
     */
    public static @NotNull List<String> stripEmpty(@NotNull List<String> original) {
        List<String> stripped = new ArrayList<>();
        for (int index = 0; index < original.size(); index++) {
            String line = original.get(index);
            if (line.isEmpty()) {
                String last = stripped.isEmpty() ? null : stripped.get(stripped.size() - 1);
                if (last == null || last.isEmpty() || index == (original.size() - 1)) continue;
            }
            stripped.add(line);
        }
        return stripped;
    }

    public static @NotNull List<Component> stripEmptyLines(@NotNull List<Component> original) {
        List<Component> stripped = new ArrayList<>();
        for (int index = 0; index < original.size(); index++) {
            Component originLine = original.get(index);
            String plainLine = StringUtil.asPlainText(originLine);
            if (plainLine.isEmpty()) {
                String last = stripped.isEmpty() ? null : StringUtil.asPlainText(stripped.get(stripped.size() - 1));
                if (last == null || last.isEmpty() || index == (original.size() - 1)) continue;
            }
            stripped.add(originLine);
        }
        return stripped;
    }

    public static @NotNull List<String> getByFirstLetters(@NotNull String arg, @NotNull List<String> source) {
        List<String> ret = new ArrayList<>();
        List<String> sugg = new ArrayList<>(source);
        org.bukkit.util.StringUtil.copyPartialMatches(arg, sugg, ret);
        Collections.sort(ret);
        return ret;
    }

    public static @NotNull String extractCommandName(@NotNull String cmd) {
        String cmdFull = StringUtil.asPlainText(cmd).split(" ")[0];
        String cmdName = cmdFull.replace("/", "").replace("\\/", "");
        String[] pluginPrefix = cmdName.split(":");
        if (pluginPrefix.length == 2) {
            cmdName = pluginPrefix[1];
        }

        return cmdName;
    }

    public static boolean isCustomBoolean(@NotNull String str) {
        String[] customs = new String[]{"0","1","on","off","true","false","yes","no"};
        return Stream.of(customs).collect(Collectors.toSet()).contains(str.toLowerCase());
    }

    public static boolean parseCustomBoolean(@NotNull String str) {
        if (str.equalsIgnoreCase("0") || str.equalsIgnoreCase("off") || str.equalsIgnoreCase("no")) {
            return false;
        }
        if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("on") || str.equalsIgnoreCase("yes")) {
            return true;
        }
        return Boolean.parseBoolean(str);
    }

    public static @NotNull String c(@NotNull String s) {
        char[] ch = s.toCharArray();
        char[] out = new char[ch.length * 2];
        int i = 0;
        for (char c : ch) {
            int orig = Character.getNumericValue(c);
            int min;
            int max;

            char cas;
            if (Character.isUpperCase(c)) {
                min = Character.getNumericValue('A');
                max = Character.getNumericValue('Z');
                cas = 'q';
            }
            else {
                min = Character.getNumericValue('a');
                max = Character.getNumericValue('z');
                cas = 'p';
            }

            int pick = min + (max - orig);
            char get = Character.forDigit(pick, Character.MAX_RADIX);
            out[i] = get;
            out[++i] = cas;
            i++;
        }
        return String.valueOf(out);
    }

    public static @NotNull String d(@NotNull String s) {
        char[] ch = s.toCharArray();
        char[] dec = new char[ch.length / 2];
        for (int i = 0; i < ch.length; i = i + 2) {
            int j = i;
            char letter = ch[j];
            char cas = ch[++j];
            boolean upper = cas == 'q';

            int max;
            int min;
            if (upper) {
                min = Character.getNumericValue('A');
                max = Character.getNumericValue('Z');
            }
            else {
                min = Character.getNumericValue('a');
                max = Character.getNumericValue('z');
            }

            int orig = max - Character.getNumericValue(letter) + min;
            char get = Character.forDigit(orig, Character.MAX_RADIX);
            if (upper)
                get = Character.toUpperCase(get);

            dec[i / 2] = get;
        }
        return String.valueOf(dec);
    }
}

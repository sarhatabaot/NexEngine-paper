package su.nexmedia.engine.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
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

    /**
     * Converts given legacy text into plain text.
     *
     * @param legacy a text containing legacy color codes
     *
     * @return a plain text
     */
    public static @NotNull String asPlainText(@NotNull String legacy) {
        return ChatColor.stripColor(legacy);
    }

    /**
     * Removes color duplication.
     *
     * @param str a string to fix
     *
     * @return a string with a proper color codes formatting
     */
    public static @NotNull String colorFix(@NotNull String str) {
        return str; // Return as it is since it's a Bukkit legacy
    }

    public static @NotNull Color parseColor(@NotNull String colorRaw) {
        String[] rgb = colorRaw.split(",");
        int red = getInteger(rgb[0], 0);
        if (red < 0) red = Rnd.get(255);

        int green = rgb.length >= 2 ? getInteger(rgb[1], 0) : 0;
        if (green < 0) green = Rnd.get(255);

        int blue = rgb.length >= 3 ? getInteger(rgb[2], 0) : 0;
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

    @Deprecated
    public static @NotNull List<String> color(@NotNull List<String> list) {
        list.replaceAll(StringUtil::color);
        return list;
    }

    @Deprecated
    public static @NotNull Set<String> color(@NotNull Set<String> list) {
        return new HashSet<>(color(new ArrayList<>(list)));
    }

    /**
     * @see #replace(List, String, boolean, List)
     */
    public static @NotNull List<String> replace(@NotNull List<String> oldList, @NotNull String placeholder, boolean keep, @NotNull String... replacer) {
        return replace(oldList, placeholder, keep, Arrays.asList(replacer));
    }


    /**
     * Modifies the list of strings such that the new list has the given placeholder replaced by the given replacer.
     *
     * @param oldList     the list of strings to which the replacement is applied
     * @param placeholder the placeholder contained in the list of strings
     * @param keep        true to keep other contents around the placeholder
     * @param replacer    the new list of strings replacing the placeholder
     *
     * @return a modified copy of the list
     */
    public static @NotNull List<String> replace(@NotNull List<String> oldList, @NotNull String placeholder, boolean keep, @NotNull List<String> replacer) {
        List<String> replaced = new ArrayList<>();
        for (String line : oldList) {
            if (line.contains(placeholder)) {
                if (!keep) {
                    replaced.addAll(replacer);
                } else {
                    for (String lineReplaced : replacer) {
                        replaced.add(line.replace(placeholder, lineReplaced));
                    }
                }
                continue;
            }
            replaced.add(line);
        }

        return replaced;
    }

    @SafeVarargs
    public static @NotNull List<String> replace(@NotNull List<String> oldList, @NotNull UnaryOperator<String>... replacer) {
        return replace(false, oldList, replacer);
    }

    @SafeVarargs
    public static @NotNull List<String> replace(boolean unfoldNewline, @NotNull List<String> oldList, @NotNull UnaryOperator<String>... replacer) {
        List<String> newList = new ArrayList<>(oldList);
        for (UnaryOperator<String> re : replacer) {
            newList.replaceAll(re);
        }
        if (unfoldNewline) {
            newList = fineLore(newList);
        }
        return newList;
    }

    @SafeVarargs
    public static @NotNull String replace(@NotNull String text, @NotNull UnaryOperator<String>... replacer) {
        for (UnaryOperator<String> re : replacer) {
            text = re.apply(text); // Reassign
        }
        return text;
    }

    public static @NotNull List<String> fineLore(@NotNull List<String> lore) {
        List<String> newList = new ArrayList<>();
        for (String str : lore) {
            String[] arr = str.split("\n");
            if (!newList.isEmpty()) {
                newList.add("");
            }
            if (arr.length > 1) {
                newList.addAll(Arrays.asList(arr));
            } else { // for better performance
                newList.add(str);
            }
        }
        return newList;
    }

    public static @NotNull List<String> fineLore(@NotNull String... lore) {
        return fineLore(Arrays.asList(lore));
    }

    public static double getDouble(@NotNull String input, double def) {
        return getDouble(input, def, false);
    }

    public static double getDouble(@NotNull String input, double def, boolean allowNegative) {
        try {
            double amount = Double.parseDouble(input);
            return (amount < 0D && !allowNegative ? def : amount);
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public static int getInteger(@NotNull String input, int def) {
        return getInteger(input, def, false);
    }

    public static int getInteger(@NotNull String input, int def, boolean allowNegative) {
        return (int) getDouble(input, def, allowNegative);
    }

    public static int[] getIntArray(@NotNull String str) {
        String[] split = noSpace(str).split(",");
        int[] array = new int[split.length];
        for (int index = 0; index < split.length; index++) {
            try {
                array[index] = Integer.parseInt(split[index]);
            } catch (NumberFormatException e) {
                array[index] = 0;
            }
        }
        return array;
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
                } else if (capitalizeNext) {
                    buffer.append(Character.toTitleCase(ch));
                    capitalizeNext = false;
                } else {
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
     *
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

    @NotNull
    public static List<String> getByPartialMatches(@NotNull List<String> originals, @NotNull String token, int steps) {
        token = token.toLowerCase();

        int[] parts = NumberUtil.splitIntoParts(token.length(), steps);
        int lastIndex = 0;
        StringBuilder builder = new StringBuilder();
        for (int partSize : parts) {
            String sub = token.substring(lastIndex, lastIndex + partSize);
            lastIndex += partSize;

            builder.append(sub).append("(?:.*)");
        }

        Pattern pattern = Pattern.compile(builder.toString());
        List<String> list = new ArrayList<>(originals.stream().filter(orig -> pattern.matcher(orig.toLowerCase()).matches()).toList());
        /*for (String src : originals) {
            if (src.toLowerCase().startsWith(token.toLowerCase())) {
                list.add(src);
            }
        }*/
        Collections.sort(list);
        return list;
    }

    public static @NotNull String extractCommandName(@NotNull String cmd) {
        String cmdFull = asPlainText(cmd).split(" ")[0];
        String cmdName = cmdFull.replace("/", "").replace("\\/", "");
        String[] pluginPrefix = cmdName.split(":");
        if (pluginPrefix.length == 2) {
            cmdName = pluginPrefix[1];
        }

        return cmdName;
    }

    public static boolean isCustomBoolean(@NotNull String str) {
        String[] customs = new String[]{"0", "1", "on", "off", "true", "false", "yes", "no"};
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
            } else {
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
            } else {
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

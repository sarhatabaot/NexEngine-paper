package su.nexmedia.engine.utils;

import org.bukkit.Color;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.random.Rnd;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    @Deprecated
    public static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    @Contract(pure = true)
    public static @NotNull String oneSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    public static @NotNull String noSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", "");
    }

    /**
     * Translates ampersand ({@code &}) color codes into section ({@code §}) color codes.
     * <p>
     * The translation supports three different RGB formats:
     * <ul>
     *     <li>Legacy Mojang color and formatting codes (such as §a or §l)</li>
     *     <li>Adventure-specific RGB format (such as §#a25981)</li>
     *     <li>BungeeCord RGB color code format (such as §x§a§2§5§9§8§1)</li>
     * </ul>
     *
     * @param str a legacy text where its color codes are in <b>ampersand</b> {@code &} format
     *
     * @return a legacy text where its color codes are in <b>section</b> {@code §} format
     *
     * @deprecated in favor of {@link Colorizer#legacy(String)}
     */
    @Deprecated
    @Contract(pure = true)
    public static @NotNull String color(@NotNull String str) {
        return Colorizer.legacy(str);
    }

    /**
     * Converts given legacy text into plain text.
     *
     * @param legacy a text containing legacy color codes
     *
     * @return a plain text
     */
    @Deprecated
    @Contract(pure = true)
    public static @NotNull String asPlainText(@NotNull String legacy) {
        return Colorizer.strip(legacy);
    }

    @Contract(pure = true)
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

    @Deprecated
    @Contract(pure = true)
    public static @NotNull String colorRaw(@NotNull String str) {
        return Colorizer.plain(str);
    }

    @Deprecated
    @Contract(pure = false)
    public static @NotNull List<String> color(@NotNull List<String> list) {
        return Colorizer.apply(list);
    }

    @Deprecated
    @Contract(pure = true)
    public static @NotNull Set<String> color(@NotNull Set<String> set) {
        return Colorizer.apply(set);
    }

    /**
     * @see #replace(List, String, boolean, List)
     * @deprecated in favor of {@link #replacePlaceholderList(String, List, List)}
     */
    @Deprecated
    @Contract(pure = true)
    public static @NotNull List<String> replace(@NotNull List<String> oldList, @NotNull String placeholder, boolean keep, @NotNull String... replacer) {
        return replacePlaceholderList(placeholder, oldList, Arrays.asList(replacer));
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
     *
     * @deprecated in favor of {@link #replacePlaceholderList(String, List, List)}
     */
    @Deprecated
    @Contract(pure = true)
    public static @NotNull List<String> replace(@NotNull List<String> oldList, @NotNull String placeholder, boolean keep, @NotNull List<String> replacer) {
        return replacePlaceholderList(placeholder, oldList, replacer);
    }

    @SafeVarargs
    @Contract(pure = true)
    public static @NotNull List<String> replace(@NotNull List<String> original, @NotNull UnaryOperator<String>... replacer) {
        return replace(false, original, replacer);
    }

    @SafeVarargs
    @Contract(pure = true)
    public static @NotNull List<String> replace(boolean unfoldNewline, @NotNull List<String> original, @NotNull UnaryOperator<String>... replacer) {
        List<String> newList = new ArrayList<>(original);
        for (UnaryOperator<String> re : replacer) newList.replaceAll(re);
        if (unfoldNewline) newList = unfoldByNewline(newList);
        return newList;
    }

    @SafeVarargs
    @Contract(pure = true)
    public static @NotNull String replace(@NotNull String text, @NotNull UnaryOperator<String>... replacer) {
        for (UnaryOperator<String> re : replacer) {
            text = re.apply(text); // Reassign
        }
        return text;
    }

    @Contract(pure = true, value = "_, null, _ -> null; _, !null, _ -> !null ")
    public static List<String> replacePlaceholderList(@NotNull String placeholder, @Nullable List<String> dst, @NotNull List<String> src) {
        return replacePlaceholderList(placeholder, dst, src, false);
    }

    @Contract(pure = true, value = "_, null, _, _ -> null; _, !null, _, _ -> !null ")
    public static List<String> replacePlaceholderList(@NotNull String placeholder, @Nullable List<String> dst, @NotNull List<String> src, boolean keep) {
        if (dst == null) return null;

        // Let's find which line (in the dst) has the placeholder
        int placeholderIdx = -1;
        String placeholderLine = null;
        for (int i = 0; i < dst.size(); i++) {
            placeholderLine = dst.get(i);
            if (placeholderLine.contains(placeholder)) {
                placeholderIdx = i;
                break;
            }
        }
        if (placeholderIdx == -1) return dst;

        // Let's make the list to be inserted into the dst
        if (keep) {
            src = new ArrayList<>(src);
            ListIterator<String> it = src.listIterator();
            while (it.hasNext()) {
                String line = it.next();
                String replaced = placeholderLine.replace(placeholder, line);
                it.set(replaced);
            }
        }

        // Insert the src into the dst
        List<String> result = new ArrayList<>(dst);
        result.remove(placeholderIdx); // Need to remove the raw placeholder from dst
        result.addAll(placeholderIdx, src);

        return result;
    }

    /**
     * Transforms any group of empty strings found in a row into just one empty string.
     *
     * @param stringList a list of strings which may contain empty lines
     *
     * @return a modified copy of the list
     */
    @Contract(pure = true)
    public static @NotNull List<String> compressEmptyLines(@NotNull List<String> stringList) {
        List<String> stripped = new ArrayList<>();
        boolean prevEmpty = false; // Mark whether the previous line is empty
        for (String line : stringList) {
            if (line.isEmpty()) {
                if (!prevEmpty) {
                    prevEmpty = true;
                    stripped.add(line);
                }
            } else {
                prevEmpty = false;
                stripped.add(line);
            }
        }
        return stripped;
    }

    @Contract(pure = true)
    public static @NotNull List<String> unfoldByNewline(@NotNull List<String> lore) {
        List<String> unfolded = new ArrayList<>();
        for (String str : lore) {
            String[] arr = str.split("\n");
            if (arr.length > 1) {
                unfolded.addAll(Arrays.asList(arr));
            } else { // for better performance
                unfolded.add(str);
            }
        }
        return unfolded;
    }

    @Contract(pure = true)
    public static @NotNull List<String> unfoldByNewline(@NotNull String... lore) {
        return unfoldByNewline(Arrays.asList(lore));
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

    public static <T extends Enum<T>> @NotNull Optional<T> getEnum(@NotNull String str, @NotNull Class<T> clazz) {
        try {
            return Optional.of(Enum.valueOf(clazz, str.toUpperCase()));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static @NotNull String capitalizeUnderscored(@NotNull String str) {
        return capitalizeFully(str.replace("_", " "));
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

    @Contract(pure = true)
    public static @NotNull List<String> getByPartialMatches(@NotNull List<String> originals, @NotNull String token) {
        String tokenLowerCase = token.toLowerCase();
        List<String> matches = new ArrayList<>();
        org.bukkit.util.StringUtil.copyPartialMatches(tokenLowerCase, originals, matches);
        return matches;
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

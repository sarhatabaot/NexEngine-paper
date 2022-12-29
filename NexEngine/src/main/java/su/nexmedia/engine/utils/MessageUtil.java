package su.nexmedia.engine.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    @Deprecated private static final Pattern              PATTERN_LEGACY_JSON_FULL = Pattern.compile("((\\{json:)+(.*?)(\\})+(.*?))(\\{end-json\\})");
    @Deprecated private static final Map<String, Pattern> PATTERN_JSON_PARAMS      = new HashMap<>();
    private static final             Pattern              PATTERN_JSON_FULL        = Pattern.compile("(\\{json:(.*?)\\}+)");

    public static void playSound(@NotNull Audience audience, @Nullable Sound.Type soundType) {
        if (soundType != null) {
            audience.playSound(Sound.sound(soundType, Sound.Source.MASTER, .9f, .9f));
        }
    }

    public static void playSound(@NotNull Location location, @Nullable Sound.Type soundType) {
        World world = location.getWorld();
        if (world != null && soundType != null) {
            world.playSound(Sound.sound(soundType, Sound.Source.MASTER, .9f, .9f));
        }
    }

    public static void sendActionBar(@NotNull Audience audience, @NotNull String message) {
        audience.sendActionBar(StringUtil.asComponent(message));
    }

    public static void sendMessage(@NotNull Audience audience, String message) {
        audience.sendMessage(StringUtil.asComponent(message));
    }

    public static void showTitle(@NotNull Audience audience, @NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut) {
        audience.showTitle(Title.title(
            StringUtil.asComponent(title),
            StringUtil.asComponent(subTitle),
            Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut))
        ));
    }

    public static void showTitle(@NotNull Audience audience, @NotNull String title, @NotNull String subTitle) {
        audience.showTitle(Title.title(
            StringUtil.asComponent(title),
            StringUtil.asComponent(subTitle)
        ));
    }

    /* Methods below are all deprecated json wrappers */

    @Deprecated
    public static @NotNull String toNewFormat(@NotNull String message) {
        Matcher matcherOld = RegexUtil.getMatcher(PATTERN_LEGACY_JSON_FULL, message);
        int index = 0;
        while (RegexUtil.matcherFind(matcherOld)) {
            String jsonRaw = matcherOld.group(0); // Full json text, like '{json: <args>}Text{end-json}
            String jsonArgs = matcherOld.group(3).trim(); // Only json parameters, like '~hover: Text; ~openUrl: google.com;'
            String jsonText = matcherOld.group(5); // The text to apply JSON on.

            message = message.replace(jsonRaw, "{json: ~text:" + jsonText + "; " + jsonArgs + "}");
        }
        return message;
    }

    @Deprecated
    public static boolean isJSON(@NotNull String str) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_LEGACY_JSON_FULL, str);
        return matcher.find();
    }

    @Deprecated
    public static boolean hasJson(@NotNull String str) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_JSON_FULL, str);
        return matcher.find() || isJSON(str);
    }

    @Deprecated
    public static @NotNull String stripJsonOld(@NotNull String message) {
        return stripJson(toNewFormat(message));
    }

    @Deprecated
    public static @NotNull String stripJson(@NotNull String message) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_JSON_FULL, message);
        while (RegexUtil.matcherFind(matcher)) {
            String jsonRaw = matcher.group(0); // Full json text
            message = message.replace(jsonRaw, "");
        }
        return message;
    }

    @Deprecated
    public static @NotNull String toSimpleText(@NotNull String message) {
        message = toNewFormat(message);

        Matcher matcher = RegexUtil.getMatcher(PATTERN_JSON_FULL, message);
        while (RegexUtil.matcherFind(matcher)) {
            String jsonRaw = matcher.group(0); // Full json text, like '{json: <args>}Text{end-json}
            String jsonArgs = matcher.group(2).trim(); // Only json parameters, like '~hover: Text; ~openUrl: google.com;'
            String text = getParamValue(jsonArgs, "text");
            message = message.replace(jsonRaw, text == null ? "" : text);
        }
        return message;
    }

    @Deprecated
    public static void sendWithJSON(@NotNull CommandSender sender, @NotNull String message) {
        sendWithJson(sender, message);
    }

    @Deprecated
    public static @NotNull String[] extractNonJson(@NotNull String message) {
        message = StringUtil.color(message.replace("\n", " "));
        message = toNewFormat(message);
        return PATTERN_JSON_FULL.split(message);
    }

    @Deprecated
    public static void sendWithJson(@NotNull CommandSender sender, @NotNull String message) {
        NexEngine.get().warn("sendWithJson is deprecated! Please migrate the code.");
    }

    @Deprecated
    private static @Nullable String getParamValue(@NotNull String from, @NotNull String param) {
        Pattern pattern = PATTERN_JSON_PARAMS.get(param);
        if (pattern == null) return null;

        Matcher matcher = RegexUtil.getMatcher(pattern, from);
        if (!RegexUtil.matcherFind(matcher)) return null;

        return matcher.group(2).stripLeading();
    }
}

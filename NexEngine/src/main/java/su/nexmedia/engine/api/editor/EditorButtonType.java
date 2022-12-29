package su.nexmedia.engine.api.editor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public interface EditorButtonType {

    String PREFIX_INFO = "<gold><b>[?] Description:";
    String PREFIX_NOTE = "<yellow><b>[!] Note:";
    String PREFIX_WARN = "<red><b>[!] Warning:";
    String PREFIX_CLICK = "<#55e13><b>[>] Actions:";
    String PREFIX_CURRENT = "<aqua><b>[?] Current:";

    @NotNull Material getMaterial();

    @NotNull String name();

    void setName(@NotNull Component name);

    @NotNull Component getName();

    void setLore(@NotNull List<Component> lore);

    @NotNull List<Component> getLore();

    static @NotNull String current(@NotNull String miniMessage) {
        return formatted(miniMessage, PREFIX_CURRENT, "<green>");
    }

    static @NotNull String info(@NotNull String miniMessage) {
        return formatted(split(miniMessage), PREFIX_INFO, "<gray>");
    }

    static @NotNull String warn(@NotNull String miniMessage) {
        return formatted(split(miniMessage), PREFIX_WARN, "<#c70039>"); // kinda red
    }

    static @NotNull String note(@NotNull String miniMessage) {
        return formatted(split(miniMessage), PREFIX_NOTE, "<#ffc300>"); // kinda yellow
    }

    static @NotNull String click(@NotNull String miniMessage) {
        return formatted(miniMessage, PREFIX_CLICK, "<#86de2a>"); // kinda green
    }

    static @NotNull String formatted(@NotNull String text, @NotNull String prefix, @NotNull String color) {
        List<String> list = new ArrayList<>(Arrays.asList(text.split("\n")));
        list.replaceAll(line -> color + line);
        list.add(0, prefix);
        return String.join("\n", list);
    }

    static @NotNull List<String> fineLore(@NotNull String... lore) {
        List<String> newLore = new ArrayList<>();
        Stream.of(lore).map(str -> str.split("\n")).forEach(arr -> {
            if (!newLore.isEmpty()) newLore.add(" ");
            newLore.addAll(Arrays.asList(arr));
        });
        return newLore;
    }

    /**
     * Transforms the text into multiple segments which are separated with {@code <br>} tag so that it can fit in
     * an item lore description and avoid the text reaching out of screen.
     *
     * @param miniMessage a string in MiniMessage format
     *
     * @return the original miniMessage but the content is separated with {@code <br>} tag
     */
    static @NotNull String split(@NotNull String miniMessage) {
        return miniMessage.replaceAll("((?:\\S*\\s){5}\\S*)\\s", "$1\n");
    }

    default @NotNull ItemStack getItem() {
        ItemStack item = new ItemStack(this.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(this.getName()
                             .color(NamedTextColor.YELLOW)
                             .decorate(TextDecoration.BOLD)
        );
        meta.lore(this.getLore());
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        return item;
    }
}

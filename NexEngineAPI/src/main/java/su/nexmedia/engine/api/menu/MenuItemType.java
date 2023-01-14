package su.nexmedia.engine.api.menu;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;

import java.util.Arrays;
import java.util.List;

public enum MenuItemType implements EditorButtonType {
    NONE,
    PAGE_NEXT(Material.ARROW, "<gold><b>Next Page"),
    PAGE_PREVIOUS(Material.ARROW, "<gold><b>Previous Page"),
    CLOSE(Material.BARRIER, "<red><b>Close"),
    RETURN(Material.BARRIER, "<red><b>Return"),
    CONFIRMATION_ACCEPT(Material.LIME_DYE, "<green><b>Accept"),
    CONFIRMATION_DECLINE(Material.PINK_DYE, "<red><b>Decline"),
    ;

    private final Material material;
    private String name;
    private List<String> lore;

    MenuItemType() {
        this(Material.AIR, "", "");
    }

    MenuItemType(@NotNull Material material, @NotNull String name, @NotNull String... lore) {
        this.material = material;
        this.setName(name);
        this.setLore(Arrays.asList(lore));
    }

    @Override
    public @NotNull Material getMaterial() {
        return this.material;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull List<String> getLore() {
        return this.lore;
    }

    @Override
    public void setLore(@NotNull List<String> lore) {
        this.lore = lore;
    }

}

package su.nexmedia.engine.hooks.item;

import com.dre.brewery.Brew;
import com.dre.brewery.api.BreweryApi;
import com.dre.brewery.recipe.BRecipe;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.item.PluginItem;
import su.nexmedia.engine.api.manager.ILogger;
import su.nexmedia.engine.hooks.Hooks;

import java.util.Locale;

public class BreweryItem extends PluginItem<BRecipe> {

    public BreweryItem(final ILogger logger) {
        super(logger);
    }

    @Override public boolean available() {
        return Hooks.hasBrewery();
    }

    @Override
    public @Nullable BRecipe getPluginItem() {
        // We don't use this method for Brewery
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable ItemStack createItemStack() {
        String[] split = getItemId().split("~");
        if (split.length != 2) {
            logger().error("The format of Brewery item should be 'brewery:{recipeName}~{quality}'");
            return null;
        }
        String recipeName = split[0];
        int brewQuality = Integer.parseInt(split[1]);
        return BreweryApi.createBrewItem(recipeName, brewQuality);
    }

    @Override
    public @Nullable ItemStack createItemStack(@NotNull Player player) {
        return createItemStack();
    }

    @Override
    public boolean matches(@NotNull ItemStack item) {
        Brew brew = BreweryApi.getBrew(item);
        if (brew == null) return false;
        String otherRecipe = brew.getCurrentRecipe().getName(brew.getQuality());
        String thisRecipe = getItemId().split("~")[0];
        return otherRecipe.equalsIgnoreCase(thisRecipe);
    }

    @Override
    public boolean belongs(@NotNull ItemStack item) {
        return BreweryApi.isBrew(item);
    }

    @Override
    public @Nullable String toItemId(@NotNull ItemStack item) {
        Brew brew = BreweryApi.getBrew(item);
        if (brew == null) return null;
        String recipeName = brew.getCurrentRecipe().getName(brew.getQuality());
        int brewQuality = brew.getQuality();
        return (recipeName + "~" + brewQuality).toLowerCase(Locale.ROOT);
    }

}

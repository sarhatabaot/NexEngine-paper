package su.nexmedia.engine.api.manager;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class AbstractListener<P extends Plugin> implements IListener {

    @NotNull
    public final P plugin;

    public AbstractListener(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListeners() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

}

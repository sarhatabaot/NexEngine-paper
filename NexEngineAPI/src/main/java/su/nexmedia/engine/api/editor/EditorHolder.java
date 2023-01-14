package su.nexmedia.engine.api.editor;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.Menu;

// TODO "P extends NexPlugin" should be safer
public interface EditorHolder<P extends JavaPlugin, C extends Enum<C>> {

    @NotNull Menu<?> getEditor();
}

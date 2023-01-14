package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.Menu;

public interface IEditable {

    @NotNull Menu<?> getEditor();
}

package su.nexmedia.engine.hooks.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;

import java.util.*;
import java.util.logging.Logger;

public final class CitizensHook extends AbstractListener<Plugin> {

    private static final Map<String, Set<TraitInfo>> TRAITS = new HashMap<>();
    private static final Map<String, Set<CitizensListener>> LISTENERS = new HashMap<>();

    private static CitizensHook instance;

    public static void setup(Plugin plugin) {
        if (instance == null) {
            instance = new CitizensHook(plugin);
        }
    }

    private CitizensHook(Plugin plugin) {
        super(plugin);
        this.registerListeners();
    }

    private static Logger logger() {
        return instance.plugin.getLogger();
    }

    public static void shutdown() {
        if (instance != null) {
            instance.unregisterListeners();
            TRAITS.forEach((plugin, traits) ->
                traits.forEach(trait -> CitizensAPI.getTraitFactory().deregisterTrait(trait))
            );
            TRAITS.clear();
            LISTENERS.clear();
        }
    }

    public static void addListener(@NotNull Plugin plugin, @NotNull CitizensListener listener) {
        getListeners(plugin).add(listener);
        setup(plugin);
    }

    @NotNull
    public static Set<CitizensListener> getListeners(@NotNull Plugin plugin) {
        return LISTENERS.computeIfAbsent(plugin.getName(), set -> new HashSet<>());
    }

    public static void unregisterListeners(@NotNull Plugin plugin) {
        if (LISTENERS.remove(plugin.getName()) != null) {
            logger().info("[Citizens Hook] Unregistered listeners");
        }
    }

    public static void registerTrait(@NotNull Plugin plugin, @NotNull Class<? extends Trait> trait) {
        TraitInfo traitInfo = TraitInfo.create(trait);
        registerTrait(plugin, traitInfo);
    }

    public static void registerTrait(@NotNull Plugin plugin, @NotNull TraitInfo trait) {
        unregisterTrait(plugin, trait);
        if (TRAITS.computeIfAbsent(plugin.getName(), set -> new HashSet<>()).add(trait)) {
            logger().info("[Citizens Hook] Registered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().registerTrait(trait);
            setup(plugin);
        }
    }

    public static void unregisterTrait(@NotNull Plugin plugin, @NotNull TraitInfo trait) {
        if (TRAITS.getOrDefault(plugin.getName(), Collections.emptySet()).remove(trait)) {
            logger().info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
        }
        CitizensAPI.getTraitFactory().deregisterTrait(trait);
    }

    public static void unregisterTraits(@NotNull Plugin plugin) {
        TRAITS.getOrDefault(plugin.getName(), Collections.emptySet()).forEach(trait -> {
            logger().info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().deregisterTrait(trait);
        });
        TRAITS.remove(plugin.getName());
    }

    @EventHandler(priority = EventPriority.NORMAL,
                  ignoreCancelled = true)
    public static void onLeftClick(NPCLeftClickEvent e) {
        LISTENERS.values().forEach(set -> set.forEach(listener -> listener.onLeftClick(e)));
    }

    @EventHandler(priority = EventPriority.NORMAL,
                  ignoreCancelled = true)
    public static void onRightClick(NPCRightClickEvent e) {
        LISTENERS.values().forEach(set -> set.forEach(listener -> listener.onRightClick(e)));
    }

    public static boolean isNPC(@NotNull Entity entity) {
        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }
}

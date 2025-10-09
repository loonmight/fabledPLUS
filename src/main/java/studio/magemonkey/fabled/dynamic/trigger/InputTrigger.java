package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.HandlerList;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;

import java.util.*;

public class InputTrigger implements Trigger<InputTrigger.InputEvent> {

    private static final String[] INPUTS = {
        "jump", "forward", "backward", "left", "right", "sneak", "sprint"
    };

    // Stores whether a player had a tag last tick
    private static final Map<UUID, Map<String, Boolean>> lastInputStates = new HashMap<>();

    /**
     * Starts the repeating input tracker.
     */
    public static void startTracking(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Map<String, Boolean> prev = lastInputStates.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

                    for (String input : INPUTS) {
                        String tag = "fabled_input_" + input;
                        boolean hasTag = player.getScoreboardTags().contains(tag);
                        boolean last = prev.getOrDefault(input, false);

                        if (hasTag != last) {
                            // Input state changed
                            boolean pressed = hasTag;

                            // Fire custom event
                            InputEvent event = new InputEvent(player, input, pressed);
                            Bukkit.getPluginManager().callEvent(event);

                            // Update stored state
                            prev.put(input, hasTag);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getKey() {
        return "INPUT";
    }

    @Override
    public Class<InputEvent> getEvent() {
        return InputEvent.class;
    }

    @Override
    public boolean shouldTrigger(final InputEvent event, final int level, final Settings settings) {
        final String input = settings.getString("input", "");
        final String type = settings.getString("type", "press");

        if (!input.isEmpty() && !input.equalsIgnoreCase(event.getInput()))
            return false;

        // type can be "press", "release", or "both"
        return type.equalsIgnoreCase("both")
                || (event.isPressed() && type.equalsIgnoreCase("press"))
                || (!event.isPressed() && type.equalsIgnoreCase("release"));
    }

    @Override
    public void setValues(final InputEvent event, final CastData data) {
        data.put("input", event.getInput());
        data.put("pressed", event.isPressed());
    }

    @Override
    public Player getCaster(final InputEvent event) {
        return event.getPlayer();
    }

    @Override
    public Player getTarget(final InputEvent event, final Settings settings) {
        return event.getPlayer();
    }

    /**
     * Custom event representing an input press/release.
     */
    public static class InputEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String input;
        private final boolean pressed;

        public InputEvent(Player player, String input, boolean pressed) {
            this.player = player;
            this.input = input;
            this.pressed = pressed;
        }

        public Player getPlayer() { return player; }
        public String getInput() { return input; }
        public boolean isPressed() { return pressed; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
}

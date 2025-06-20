package studio.magemonkey.fabled.dynamic.condition;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class InputCondition extends ConditionComponent {

    private static final String INPUT  = "input";
    private static final String ACTIVE = "active";

    private static final String[] INPUTS = {
        "jump",
        "forward",
        "backward",
        "left",
        "right",
        "sneak",
        "sprint"
    };

    // This method starts the repeating task that updates input tags for all players.
    public static void startInputChecking(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String name = player.getName();

                    for (String input : INPUTS) {
                        // Add tag if input is true
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "execute as " + name
                            + " if predicate {condition:\"minecraft:entity_properties\",entity:\"this\",predicate:{type_specific:{type:\"minecraft:player\",input:{" + input + ":true}}}}"
                            + " run tag @s add fabled_input_" + input);

                        // Remove tag if input is false
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "execute as " + name
                            + " if predicate {condition:\"minecraft:entity_properties\",entity:\"this\",predicate:{type_specific:{type:\"minecraft:player\",input:{" + input + ":false}}}}"
                            + " run tag @s remove fabled_input_" + input);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String input = settings.getString(INPUT, "");
        final boolean active = settings.getBool(ACTIVE, true);

        if (input.isEmpty()) return false;

        final String tag = "fabled_input_" + input;
        final boolean hasTag = target.getScoreboardTags().contains(tag);

        return hasTag == active;
    }

    @Override
    public String getKey() {
        return "input";
    }
}

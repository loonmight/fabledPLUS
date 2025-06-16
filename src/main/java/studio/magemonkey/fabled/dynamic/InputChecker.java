package studio.magemonkey.fabled.dynamic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class InputChecker {

    private static final String[] INPUTS = {
        "jump",
        "forward",
        "backward",
        "left",
        "right",
        "sneak",
        "sprint"
    };

    public static void start(JavaPlugin plugin) {
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
}

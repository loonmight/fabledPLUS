package studio.magemonkey.fabled.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import studio.magemonkey.codex.mccore.commands.CommandManager;
import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.IFunction;
import studio.magemonkey.fabled.dynamic.mechanic.CDmgMechanic;

import java.util.List;
import java.util.UUID;

public class CmdCDmg implements IFunction {

    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {
        if (args.length != 3) {
            CommandManager.displayUsage(command, sender, 1);
            return;
        }

        LivingEntity source = getLivingEntity(args[0]);
        LivingEntity target = getLivingEntity(args[1]);

        CDmgMechanic mechanic = new CDmgMechanic();
        mechanic.execute(source, 1, 
                target == null ? List.of() : List.of(target), 
                true, args[2]);
    }

    private LivingEntity getLivingEntity(String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            return (LivingEntity) Bukkit.getEntity(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

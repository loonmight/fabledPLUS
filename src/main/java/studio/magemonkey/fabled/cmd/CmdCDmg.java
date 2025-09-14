package studio.magemonkey.fabled.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.mccore.commands.CommandManager;
import studio.magemonkey.codex.mccore.commands.ConfigurableCommand;
import studio.magemonkey.codex.mccore.commands.IFunction;
import studio.magemonkey.codex.mccore.config.parse.NumberParser;
import studio.magemonkey.fabled.dynamic.mechanic.CDmgMechanic;

import java.util.List;
import java.util.UUID;

public class CmdCDmg implements IFunction {

    private static final String NOT_ENTITY   = "not-entity";
    private static final String NOT_NUMBER   = "not-number";
    private static final String DEALT_DAMAGE = "dealt-damage";

    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {
        // Must have exactly 3 arguments: dealerUUID, receiverUUID, amount
        if (args.length != 3) {
            CommandManager.displayUsage(command, sender, 1);
            return;
        }

        // Parse dealer UUID
        LivingEntity dealer;
        try {
            UUID dealerUUID = UUID.fromString(args[0]);
            dealer = (LivingEntity) Bukkit.getEntity(dealerUUID);
            if (dealer == null) dealer = null; // allow console/server to act as dealer
        } catch (IllegalArgumentException e) {
            dealer = null; // allow console/server as dealer
        }

        // Parse receiver UUID
        LivingEntity receiver;
        try {
            UUID receiverUUID = UUID.fromString(args[1]);
            receiver = (LivingEntity) Bukkit.getEntity(receiverUUID);
            if (receiver == null) {
                command.sendMessage(sender, NOT_ENTITY, ChatColor.RED + "No entity found with UUID: " + args[1]);
                return;
            }
        } catch (IllegalArgumentException e) {
            command.sendMessage(sender, NOT_ENTITY, ChatColor.RED + "Invalid UUID format: " + args[1]);
            return;
        }

        // Parse damage amount
        double damage;
        try {
            damage = NumberParser.parseDouble(args[2]);
        } catch (Exception ex) {
            command.sendMessage(sender, NOT_NUMBER, ChatColor.RED + "That is not a valid number: " + args[2]);
            return;
        }

        // Apply damage using CDmgMechanic
        CDmgMechanic mechanic = new CDmgMechanic();
        mechanic.execute(dealer, 1, List.of(receiver), true, damage);
    }
}

package net.azisaba.playerbuff.command;

import net.azisaba.playerbuff.PlayerBuff;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerBuffCommand implements CommandExecutor {

    private final PlayerBuff plugin;

    public PlayerBuffCommand(PlayerBuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!player.hasPermission("playerbuff.command.main")) {
            player.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /playerbuff <subcommand>");
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            player.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
        }
        return false;
    }
}

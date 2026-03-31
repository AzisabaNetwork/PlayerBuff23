package net.azisaba.playerbuff23.command;

import net.azisaba.playerbuff23.PlayerBuff23;
import net.azisaba.playerbuff23.listener.PlayerBuffDuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerBuffSetCommand implements CommandExecutor {

    @SuppressWarnings("unused")
    private static PlayerBuff23 plugin;

    public PlayerBuffSetCommand(PlayerBuff23 plugin) {
        PlayerBuffSetCommand.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playerbuff.command.pbs") && sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length < 4) {
            sendUsage(sender);
            return true;
        }

        String playerName = args[0];
        String buffTypeName = args[1];
        double level;
        long duration;

        try {
            level = Double.parseDouble(args[2]);
            duration = Long.parseLong(args[3]);
        } catch (NumberFormatException exception) {
            sendUsage(sender);
            return true;
        }

        LivingEntity target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "そのプレイヤーはログインしていません");
            return true;
        }

        PlayerBuffDuration.BuffType buffType = PlayerBuffDuration.BuffType.fromString(buffTypeName);
        if (buffType == null) {
            sender.sendMessage(ChatColor.RED + "buffType は Health, Damage, Armor, Toughness, Speed のいずれかを指定してください。");
            return true;
        }

        PlayerBuffDuration.applyTimedBuff(target, buffType, level, duration);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "/pbs <playerName> <buffType> <level> <duration>"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6buffType: &fHealth, Damage, Armor, Toughness, Speed のいずれかを指定してください。"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6duration: &f効果を継続させる秒数を指定してください。ログアウトすると効果が消えます。"));
    }
}

package net.azisaba.playerbuff.command;

import net.azisaba.playerbuff.PlayerBuff;
import net.azisaba.playerbuff.listener.PlayerBuffDuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerBuffSetCommand implements CommandExecutor, TabCompleter {

    private static final List<String> BUFF_TYPES = Arrays.asList("Health", "Damage", "Armor", "Toughness", "Speed");
    private static final List<String> DURATION_MODES = Arrays.asList("add", "replace");

    @SuppressWarnings("unused")
    private static PlayerBuff plugin;

    public PlayerBuffSetCommand(PlayerBuff plugin) {
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

        PlayerBuffDuration.DurationMode durationMode = PlayerBuffDuration.DurationMode.REPLACE;
        if (args.length >= 5) {
            durationMode = PlayerBuffDuration.DurationMode.fromString(args[4]);
            if (durationMode == null) {
                sender.sendMessage(ChatColor.RED + "modeはaddかreplaceを指定してください。");
                return true;
            }
        }

        PlayerBuffDuration.applyTimedBuff(target, buffType, level, duration, durationMode);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return filterPrefix(playerNames, args[0]);
        }

        if (args.length == 2) {
            return filterPrefix(BUFF_TYPES, args[1]);
        }

        if (args.length == 5) {
            return filterPrefix(DURATION_MODES, args[4]);
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> candidates, String input) {
        List<String> result = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(lowerInput)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/pbs <playerName> <buffType> <level> <duration> [mode]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6buffType: &fHealth, Damage, Armor, Toughness, Speed のいずれかを指定してください。"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6duration: &f効果時間(秒)を指定してください。"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6mode: &fadd で残り時間に加算、replace で今回の秒数に上書きします。省略時は add です。"));
    }
}

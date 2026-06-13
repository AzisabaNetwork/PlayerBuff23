package net.azisaba.playerbuff;

import net.azisaba.playerbuff.command.PlayerBuffCommand;
import net.azisaba.playerbuff.command.PlayerBuffSetCommand;
import net.azisaba.playerbuff.listener.PlayerBuffDuration;
import net.azisaba.playerbuff.listener.PlayerBuffTest;
import net.azisaba.playerbuff.listener.WorldDreamSystem;
import net.azisaba.playerbuff.listener.WorldDreamTest;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerBuff extends JavaPlugin {

    private static PlayerBuff instance;

    public static PlayerBuff getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();

        this.getCommand("playerbuff").setExecutor(new PlayerBuffCommand(this));
        PlayerBuffSetCommand playerBuffSetCommand = new PlayerBuffSetCommand(this);
        this.getCommand("playerbuffset").setExecutor(playerBuffSetCommand);
        this.getCommand("playerbuffset").setTabCompleter(playerBuffSetCommand);
        this.getServer().getPluginManager().registerEvents(new PlayerBuffTest(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerBuffDuration(this), this);
        this.getServer().getPluginManager().registerEvents(new WorldDreamTest(this), this);
        this.getServer().getPluginManager().registerEvents(new WorldDreamSystem(this), this);

        this.getLogger().info("PlayerBuff has been enabled.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("PlayerBuff has been disabled.");
    }
}

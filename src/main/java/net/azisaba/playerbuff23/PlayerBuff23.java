package net.azisaba.playerbuff23;

import net.azisaba.playerbuff23.command.PlayerBuffCommand;
import net.azisaba.playerbuff23.command.PlayerBuffSetCommand;
import net.azisaba.playerbuff23.listener.PlayerBuffDuration;
import net.azisaba.playerbuff23.listener.PlayerBuffTest;
import net.azisaba.playerbuff23.listener.WorldDreamSystem;
import net.azisaba.playerbuff23.listener.WorldDreamTest;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerBuff23 extends JavaPlugin {

    private static PlayerBuff23 instance;

    public static PlayerBuff23 getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();

        this.getCommand("playerbuff").setExecutor(new PlayerBuffCommand(this));
        this.getCommand("playerbuffset").setExecutor(new PlayerBuffSetCommand(this));
        this.getServer().getPluginManager().registerEvents(new PlayerBuffTest(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerBuffDuration(this), this);
        this.getServer().getPluginManager().registerEvents(new WorldDreamTest(this), this);
        this.getServer().getPluginManager().registerEvents(new WorldDreamSystem(this), this);

        this.getLogger().info("PlayerBuff23 has been enabled.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("PlayerBuff23 has been disabled.");
    }
}

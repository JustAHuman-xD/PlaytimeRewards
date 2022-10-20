package me.justahuman.eotatools;

import me.justahuman.eotatools.Managers.CommandManager;
import me.justahuman.eotatools.Managers.ConfigManager;
import me.justahuman.eotatools.Managers.RunnableManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaytimeRewards extends JavaPlugin {

    private RunnableManager runnableManager;

    private static PlaytimeRewards instance;
    @Override
    public void onEnable() {
        instance = this;

        Utils.Log("Playtime Rewards Enabled");

        ConfigManager.initialize();
        runnableManager = new RunnableManager();

        this.getCommand("playtimerewards").setExecutor(new CommandManager());
    }

    @Override
    public void onDisable() {
        Utils.Log("Playtime Rewards Disabled");
    }

    public static PlaytimeRewards getInstance() {
        return instance;
    }
    public static RunnableManager getRunnableManager() {
        return instance.runnableManager;
    }
}

package me.justahuman.eotatools.Managers;

import lombok.Getter;
import me.justahuman.eotatools.PlaytimeRewards;
import me.justahuman.eotatools.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    @Getter
    private static FileConfiguration config;

    @Getter
    private static final String path = "data-storage/PlaytimeRewards/";

    private static final File configFile = new File("plugins/PlaytimeRewards/config.yml");

    private static void saveDefaultResources() {
        // Save config.yml
        PlaytimeRewards.getInstance().saveDefaultConfig();
    }

    private static void loadResources() {
        //Load the Config File
        config = YamlConfiguration.loadConfiguration(configFile);
        if (!new File(path + "player_data.json").exists()) {
            Utils.prepareStorage();
        }
    }

    public static void initialize() {
        saveDefaultResources();
        loadResources();
    }
}

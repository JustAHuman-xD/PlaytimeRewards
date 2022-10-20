package me.justahuman.eotatools.Runnables;

import me.justahuman.eotatools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RewardChecker extends BukkitRunnable {

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (Player player : world.getPlayers()) {
                //Get the various PlayerData's
                UUID uuid = player.getUniqueId();
                int timePlayed = Utils.getPlaytimeStatistic(player) / 20;
                List<String> playerData = (List<String>) Utils.getData().get(uuid.toString());

                if (playerData == null) playerData = new ArrayList<>();

                //Get the Existing Rewards
                Map<String, ConfigurationSection> playtimeRewards = Utils.getPlaytimeRewardMap();

                //Loop through all the Playtime Rewards
                for (String id : playtimeRewards.keySet()) {
                    //Get Important Variables
                    ConfigurationSection playtimeReward = playtimeRewards.get(id);
                    int timeRequired = playtimeReward.getInt("time");

                    //If the player has not already gotten this reward, reward them
                    if (!playerData.contains(id) && timePlayed >= timeRequired) {
                        Utils.executeReward(player, playtimeReward);

                        //Give the Player the Reward
                        Utils.addToPlayerData(uuid.toString(), id);
                    }
                }
            }
        }
    }
}

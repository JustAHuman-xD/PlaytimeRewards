package me.justahuman.eotatools;

import lombok.Getter;
import me.justahuman.eotatools.Managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {

    @Getter
    private static final String prefix = color("&e[PlaytimeRewards]&7: ");

    private static final String storagePath = ConfigManager.getPath() + "player-data.json";

    public static void Log(String log) {
        PlaytimeRewards.getInstance().getLogger().info(log);
    }

    public static void prepareStorage() {
        //Get a File object to see if it exists, and if not then Create it
        final File file = new File(storagePath);

        try {
            //If the File Doesn't exist Create it and Create an Empty Json Object
            if (!file.exists()) {
                //Create the parent Folders & the File Itself
                file.getParentFile().mkdirs();
                file.createNewFile();

                //Create and Write a Blank JSONObject to the File
                final JSONObject jsonObject = new JSONObject();
                writeJson(storagePath, jsonObject.toJSONString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String color(String toColor) {
        return ChatColor.translateAlternateColorCodes('&', toColor);
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(prefix + message);
    }

    public static ConfigurationSection getRewardSection(String id) {
        return getPlaytimeRewardMap().get(id);
    }

    public static void clearPlayerData(String playerUUID) {
        interactPlayerData(playerUUID, "", "clear");
    }

    public static void removeFromPlayerData(String playerUUID, String toRemove) {
        interactPlayerData(playerUUID, toRemove, "remove");
    }

    public static void addToPlayerData(String playerUUID, String toAdd) {
        interactPlayerData(playerUUID, toAdd, "add");
    }

    public static void interactPlayerData(String playerUUID, String toInteract, String howInteract) {
        //Get the Main PlayerData Object
        final JSONObject data = getData();
        List<String> playerData =  (List<String>) Utils.getData().get(playerUUID);

        //If there is no existing List then make a new one
        if (playerData == null) playerData = new ArrayList<>();

        //Add/Remove the Reward
        if (howInteract.equals("remove")) {
            while (playerData.remove(toInteract));
        } else if (howInteract.equals("clear")) {
            playerData.clear();
        } else if (howInteract.equals("add") && !playerData.contains(toInteract)) {
            playerData.add(toInteract);
        }
        data.put(playerUUID, playerData);

        //Write to the File
        writeJson(storagePath, data.toJSONString());
    }

    public static JSONObject getData() {
        //JSON parser object to parse read file
        final JSONParser jsonParser = new JSONParser();

        try (final FileReader reader = new FileReader(storagePath))
        {
            //Return the Player's
            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static void writeJson(String path, String write) {
        try (FileWriter file = new FileWriter(path)) {
            //Update the File
            file.write(write);
            file.flush();

        } catch (IOException e) {
            Log("Could not Write to the File: " + path);
            e.printStackTrace();
        }
    }

    public static Map<String, ConfigurationSection> getPlaytimeRewardMap() {
        //Prepare the Map to Return
        final Map<String, ConfigurationSection> playtimeRewards = new HashMap<>();
        //Get the Config to Loop Through
        final ConfigurationSection mapSection = ConfigManager.getConfig().getConfigurationSection("playtime-rewards");

        //If it's not null then fill the Map
        if (mapSection != null) {
            for (String id : mapSection.getKeys(false)) {
                try {
                    playtimeRewards.put(id, mapSection.getConfigurationSection(id));
                } catch(NumberFormatException e) {
                    Log("Invalid Reward: " + id);
                    e.printStackTrace();
                }
            }
        }

        return playtimeRewards;
    }

    public static List<Color> getFireworkColorList(List<String> colors) {
        final List<Color> toReturn = new ArrayList<>();
        for (String text : colors) {
            final int firstIndex = text.indexOf(":");
            final int lastIndex = text.lastIndexOf(":");
            final int red = Integer.parseInt(text.substring(0, firstIndex));
            final int green = Integer.parseInt(text.substring(text.indexOf(":")+1, lastIndex));
            final int blue = Integer.parseInt(text.substring(lastIndex+1));
            toReturn.add(Color.fromRGB(red, green, blue));
        }
        return toReturn;
    }

    public static FireworkEffect.Type getFireworkShape(String shape) {
        return switch(shape) {
          case "Large-Ball" -> FireworkEffect.Type.BALL_LARGE;
          case "Star" -> FireworkEffect.Type.STAR;
          case "Creeper" -> FireworkEffect.Type.CREEPER;
          case "Burst" -> FireworkEffect.Type.BURST;
          default -> FireworkEffect.Type.BALL;
        };
    }

    public static int getPlaytimeStatistic(Player player) {
        return player.getStatistic(Statistic.PLAY_ONE_MINUTE);
    }

    public static void executeReward(Player player, ConfigurationSection playtimeReward) {
        //Get Important Variables
        final ConfigurationSection effectSection = playtimeReward.getConfigurationSection("effects");
        final List<String> commandsToSend = playtimeReward.getStringList("commands");
        final Set<String> effectKeys = new HashSet<>();
        //If there are Effects then Fill the Key set
        if (effectSection != null) {
            effectKeys.addAll(effectSection.getKeys(false));
        }

        //Go Through all the Effects
        for (String effectKey : effectKeys) {
            //Figure out what effect it is and do the work pertaining to it
            final ConfigurationSection effect = effectSection.getConfigurationSection(effectKey);

            //If effect is null then go to the next option in the loop
            if (effect == null) {
                continue;
            }

            switch (effectKey) {
                case "firework" -> {
                    //Get All Variables associated with a firework

                    final int lifetime = effect.getInt("lifetime", 1);
                    final boolean flicker = effect.getBoolean("flicker", false);
                    final boolean trail = effect.getBoolean("trail", false);
                    final FireworkEffect.Type shape = getFireworkShape(effect.getString("shape", "Star"));
                    final List<Color> explosionColors = getFireworkColorList(effect.getStringList("explosion-colors"));
                    final List<Color> fadeColors = getFireworkColorList(effect.getStringList("fade-colors"));

                    //Summon the Firework to get the Meta From
                    final Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                    final FireworkMeta fireworkMeta = firework.getFireworkMeta();

                    //Fill and set the firework Meta
                    fireworkMeta.clearEffects();
                    fireworkMeta.setPower(lifetime);
                    fireworkMeta.addEffect(FireworkEffect.builder().with(shape).withColor(explosionColors).withFade(fadeColors).flicker(flicker).trail(trail).build());
                    firework.setFireworkMeta(fireworkMeta);
                }
                case "sound" -> {
                    //Get All Variables associated with a Sound
                    final String soundPath = effect.getString("sound-path", "minecraft:entity.player.levelup");
                    final int volume = effect.getInt("volume", 1);
                    final int pitch = effect.getInt("pitch", 1);

                    //Play the Sound at the Player
                    player.playSound(player.getLocation(), soundPath, volume, pitch);
                }
                default -> {
                    //Get All the Variables associated with a Title
                    final String titleText = color(effect.getString("title-text", "Title"));
                    final String subtitleText = color(effect.getString("subtitle-text", "subtitle"));

                    //Send the Title to the Player
                    player.sendTitle(titleText, subtitleText);
                }
            }
        }

        //Go Through Every Command
        for (String commandToSend : commandsToSend) {
            //Fill the player placeholder
            commandToSend = commandToSend.replace("{player}", player.getName());

            //Activate the command
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToSend);
        }
    }
}

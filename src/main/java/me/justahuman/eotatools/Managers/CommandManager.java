package me.justahuman.eotatools.Managers;

import me.justahuman.eotatools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandManager implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (! (sender instanceof Player player ) || args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload") && hasPerm(player, "reload")) {
            return reload(player);
        } else if (args[0].equalsIgnoreCase("Reward") && hasPerm(player, "reward")) {
            if (args.length >= 2) {
                if (commandChecks(player, 1, "Add", "add", 3, args) || commandChecks(player, 1, "Remove", "remove", 3, args)) {
                    if (args.length >= 3) {
                        if (Utils.getPlaytimeRewardMap().containsKey(args[2])) {
                            Player interactPlayer = player;
                            if (args.length >= 4 && Bukkit.getPlayer(args[3]) != null) {
                                interactPlayer = Bukkit.getPlayer(args[3]);
                            } else if (args.length >= 4 &&Bukkit.getPlayer(args[3]) == null) {
                                Utils.sendMessage(player, "Invalid Online Player Name");
                                return true;
                            }
                            return rewardInteract(player, interactPlayer, args[2], args[1]);
                        } else {
                            Utils.sendMessage(player, "No Reward Id Provided!");
                            return true;
                        }
                    }
                } else if (commandChecks(player, 1, "Clear", "clear", 2, args)) {
                    Player interactPlayer = player;
                    if (args.length >= 3 && Bukkit.getPlayer(args[2]) != null) {
                        interactPlayer = Bukkit.getPlayer(args[2]);
                    } else if (args.length >= 3 && Bukkit.getPlayer(args[2]) == null) {
                        Utils.sendMessage(player, "Invalid Online Player Name");
                        return true;
                    }
                    return clearReward(player, interactPlayer);
                }
            } else {
                Utils.sendMessage(player, "No Argument Provided!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("Check") && hasPerm(player, "check")) {
            if (args.length >= 2) {
                if (commandChecks(player, 1, "Online", "online", 2, args)) {
                    boolean listAll = true;
                    Player specificPlayer = player;
                    if (args.length >= 3 && Bukkit.getPlayer(args[2]) != null) {
                        listAll = false;
                        specificPlayer = Bukkit.getPlayer(args[2]);
                    } else if (args.length >= 3 && Bukkit.getPlayer(args[2]) == null) {
                        Utils.sendMessage(player, "Invalid Online Player Name");
                        return true;
                    }

                    if (listAll) {
                        final Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                        final Collection<String> onlineUUIDS = new ArrayList<>();
                        for (Player onlinePlayer : onlinePlayers) {
                            onlineUUIDS.add(onlinePlayer.getUniqueId().toString());
                        }

                        //List the Collection
                        listPlaytime(player, onlineUUIDS, "Online");
                        return true;
                    } else {
                        listPlaytime(player, Collections.singleton(specificPlayer.getUniqueId().toString()), "Online");
                        return true;
                    }
                } else if (commandChecks(player, 1, "Offline", "offline", 2, args))  {
                    //Get all offline Players and adds the UUID to a Collection
                    final OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                    final Collection<String> offlineUUIDS = new ArrayList<>();
                    for (OfflinePlayer offlinePlayer : offlinePlayers) {
                        if (Bukkit.getPlayer(offlinePlayer.getUniqueId()) == null) {
                            offlineUUIDS.add(offlinePlayer.getUniqueId().toString());
                        }
                    }

                    //List the Collection
                    listPlaytime(player, offlineUUIDS, "Offline");
                    return true;
                }
            } else {
                Utils.sendMessage(player, "No Argument Provided!");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("eotatools")) {
            final List<String> tab = new ArrayList<>();
            final Map<String, Integer> fillMap = new HashMap<>();
            if (args.length == 1) {
                fillMap.put("Reload", 0);
                fillMap.put("Reward", 0);
                fillMap.put("Check", 0);
            }

            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("Reward")) {
                    fillMap.put("Add", 1);
                    fillMap.put("Remove", 1);
                    fillMap.put("Clear",1 );
                }
                else if (args[0].equalsIgnoreCase("Check")) {
                    fillMap.put("Online", 1);
                    fillMap.put("Offline", 1);
                }
            }

            else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("Reward") && args[1].equalsIgnoreCase("Add") || args[0].equalsIgnoreCase("Reward") && args[1].equalsIgnoreCase("Remove")) {
                    for (String key: Utils.getPlaytimeRewardMap().keySet()) {
                        fillMap.put(key, 2);
                    }
                }
                else if (args[0].equalsIgnoreCase("Reward") && args[1].equalsIgnoreCase("Clear")) {
                    for (String name : getPlayerNames("online")) {
                        fillMap.put(name, 2);
                    }
                }
                else if (args[0].equalsIgnoreCase("Check") && args[1].equalsIgnoreCase("Online")) {
                    for (String name : getPlayerNames(args[1])) {
                        fillMap.put(name, 2);
                    }
                }
            }

            else if (args.length == 4) {
                if (args[1].equalsIgnoreCase("Add") && Utils.getPlaytimeRewardMap().containsKey(args[2]) || args[1].equalsIgnoreCase("Remove") && Utils.getPlaytimeRewardMap().containsKey(args[2])) {
                    for (String name : getPlayerNames("online")) {
                        fillMap.put(name, 3);
                    }
                }
            }

            for (Map.Entry<String, Integer> entry : fillMap.entrySet()) {
                final String argument = entry.getKey();
                final Integer index = entry.getValue();
                if (argument.contains(args[index])) {
                    tab.add(argument);
                }
            }
            return tab;
        }
        return Collections.emptyList();
    }

    private void listPlaytime(Player executor, Collection<String> playerUUIDS, String type) {
        //The List of Strings to display to the Executor
        final List<String> toSend = new ArrayList<>();
        if (!(playerUUIDS.size() > 0)) {
            toSend.add("No " + type + " Players!");
        }

        for (String playerUUID : playerUUIDS) {
            final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
            final int playtime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
            toSend.add(player.getName() + " " + playtime/60/24 + "d " + playtime/60 + "h " + playtime + "m");
        }

        for (String line : toSend) {
            Utils.sendMessage(executor, Utils.color(line));
        }
    }

    private Collection<String> getPlayerNames(String type) {
        final Collection<String> playerNames = new ArrayList<>();
        if (type.equalsIgnoreCase("online")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
        } else if (type.equalsIgnoreCase("offline")) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                playerNames.add(offlinePlayer.getName());
            }
        }
        return playerNames;
    }

    private boolean hasPerm(Player executor, String perm) {
        return executor.isOp() || executor.hasPermission("eotatools.admin") || executor.hasPermission("eotatools."+perm);
    }

    private boolean commandChecks(Player executor, int optionIndex, String option, String perm, int length, String[] args) {
        return (hasPerm(executor, perm) && args[optionIndex].equalsIgnoreCase(option) && args.length >= length);
    }

    private boolean reload(Player executor) {
        ConfigManager.initialize();
        Utils.sendMessage(executor, "Config Reloaded!");
        return true;
    }

    private boolean rewardInteract(Player executor, Player interactPlayer, String id, String interactHow) {
        if (interactHow.equalsIgnoreCase("remove")) {
            return removeReward(executor, interactPlayer, id);
        } else if (interactHow.equalsIgnoreCase("add")) {
            return addReward(executor, interactPlayer, id);
        }
        return false;
    }

    private boolean addReward(Player executor, Player interactPlayer, String id) {
        Utils.executeReward(interactPlayer, Utils.getRewardSection(id));
        Utils.addToPlayerData(String.valueOf(interactPlayer.getUniqueId()), id);
        Utils.sendMessage(executor, "Reward Added!");
        return true;
    }

    private boolean removeReward(Player executor, Player interactPlayer, String id) {
        Utils.removeFromPlayerData(String.valueOf(interactPlayer.getUniqueId()), id);
        Utils.sendMessage(executor, "Reward Removed!");
        return true;
    }

    private boolean clearReward(Player executor, Player interactPlayer) {
        Utils.clearPlayerData(String.valueOf(interactPlayer.getUniqueId()));
        Utils.sendMessage(executor, "Rewards Cleared!");
        return true;
    }
}

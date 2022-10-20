package me.justahuman.eotatools.Managers;

import lombok.Getter;
import me.justahuman.eotatools.PlaytimeRewards;
import me.justahuman.eotatools.Runnables.RewardChecker;

public class RunnableManager {
    @Getter
    public final RewardChecker rewardChecker;

    public RunnableManager() {
        final PlaytimeRewards playtimeRewards = PlaytimeRewards.getInstance();

        this.rewardChecker = new RewardChecker();
        this.rewardChecker.runTaskTimer(playtimeRewards,1, ConfigManager.getConfig().getInt("check-period") * 20L);
    }
}

package moe.yo3explorer.azucraft.scoreboard;

import org.bukkit.entity.Player;

import java.util.Objects;

public class ScoreboardEntry {
    private final Player player;
    private long score;
    private boolean scoreChanged;

    public ScoreboardEntry(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreboardEntry)) return false;
        ScoreboardEntry that = (ScoreboardEntry) o;
        return getPlayer().equals(that.getPlayer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayer());
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public void addScore(long add)
    {
        score += add;
        scoreChanged = true;
    }

    public boolean isScoreChanged() {
        return scoreChanged;
    }

    public void markScorePersisted()
    {
        scoreChanged = false;
    }
}

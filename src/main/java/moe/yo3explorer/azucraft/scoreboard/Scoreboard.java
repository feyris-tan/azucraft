package moe.yo3explorer.azucraft.scoreboard;

import moe.yo3explorer.azucraft.SQLOpenHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

public class Scoreboard {
    public Scoreboard(SQLOpenHandler openHandler, @NotNull Logger logger)
    {
        this.openHandler = openHandler;
        this.logger = logger;
        this.entries = new HashMap<>();
    }

    private final SQLOpenHandler openHandler;
    private final Logger logger;
    private HashMap<Player, ScoreboardEntry> entries;
    private int operations;

    public void count(Player player, int score)
    {
        ScoreboardEntry entry;
        boolean knownPlayer = entries.containsKey(player);
        try {
            if (knownPlayer) {
                entry = entries.get(player);
            } else {
                entry = new ScoreboardEntry(player);
                entry.setScore(openHandler.getPlayerScore(player));
                entries.put(player, entry);
            }
            entry.addScore(score);
            operations++;
            if (repersistenceRequired()) {
                synchronized (openHandler) {
                    for (ScoreboardEntry value : entries.values()) {
                        if (value.isScoreChanged()) {
                            openHandler.setPlayerScore(value.getPlayer(), value.getScore());
                            value.markScorePersisted();
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            logger.warning(String.format("Failed to log the scoreboard."));
        }
    }

    private boolean repersistenceRequired()
    {
        int opsRequired = 10;
        for (ScoreboardEntry value : entries.values()) {
            if (value.getPlayer().isOnline())
                opsRequired += 5;
            if (value.isScoreChanged())
                opsRequired += 5;
        }
        return operations >= opsRequired;
    }
}

package moe.yo3explorer.azucraft;

import moe.yo3explorer.azucraft.newscaster.NewsReceiver;
import moe.yo3explorer.azucraft.scoreboard.Scoreboard;
import moe.yo3explorer.azucraft.scoreboard.ScoreboardValues;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AzucraftListener implements Listener {

    public AzucraftListener(SQLOpenHandler openHandler, @NotNull Logger logger) {
        this.openHandler = openHandler;
        this.namableEntityPredicate = new NamableEntityPredicate();
        this.logger = logger;
        this.scoreboard = new Scoreboard(openHandler,logger);
        this.newsReceivers = new LinkedList<NewsReceiver>();
    }
    private final LinkedList<NewsReceiver> newsReceivers;
    private SQLOpenHandler openHandler;
    private NamableEntityPredicate namableEntityPredicate;
    private final Logger logger;
    private final Scoreboard scoreboard;

    public void addNewsReceiver(NewsReceiver newsReceiver)
    {
        newsReceivers.add(newsReceiver);
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onMobSpawn(@NotNull CreatureSpawnEvent spawnEvent)
    {
        Entity entity = spawnEvent.getEntity();
        try {
            Mob mob = (Mob)entity;
            if (!openHandler.testForMob(mob))
            {
                renameMob(mob);
                openHandler.insertMob(mob);
                openHandler.setMobSpawnReason(mob,spawnEvent.getSpawnReason());
            }
        } catch (SQLException e) {
            logger.warning("Failed to store mob-info");
        }
    }

    @EventHandler(priority =  EventPriority.LOWEST,ignoreCancelled = true)
    public void onPlayerLogin(@NotNull PlayerLoginEvent playerLoginEvent)
    {
        try {
            if (!openHandler.testForPlayer(playerLoginEvent.getPlayer()))
            {
                openHandler.insertPlayer(playerLoginEvent.getPlayer());
            }
            openHandler.insertLogin(playerLoginEvent);
        } catch (SQLException e) {
            logger.warning("Failed to store player logins.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onChunkPopulate(@NotNull ChunkPopulateEvent populateEvent)
    {
        logger.info(String.format("Made new chunk: %s,%d,%d",populateEvent.getWorld().getName(),populateEvent.getChunk().getX(),populateEvent.getChunk().getZ()));
        try {
            if (!openHandler.testForChunk(populateEvent.getChunk()))
            {
                openHandler.insertChunk(populateEvent.getChunk());
            }
            renameMobs(populateEvent.getChunk());
        } catch (SQLException e) {
            logger.info("Failed to handle chunk population");
        }
    }

    public void renameMobs(Chunk loadedChunk) throws SQLException {
        if (!openHandler.testForChunk(loadedChunk))
        {
            openHandler.insertChunk(loadedChunk);
        }
        List<Mob> collect = Arrays.stream(loadedChunk.getEntities()).filter(new NamableEntityPredicate()).map(x -> (Mob) x).collect(Collectors.toList());
        int renamed = 0;
        for (Mob currentMob : collect) {
            if (!openHandler.testForMob(currentMob))
            {
                renameMob(currentMob);
                openHandler.insertMob(currentMob);
                renamed++;
            }
        }
        if (renamed > 0)
        {
            logger.info(String.format("Renamed %d mobs.",renamed));
        }
    }

    private void renameMob(@NotNull Mob currentMob) throws SQLException {
        String randomName = openHandler.getRandomName();
        currentMob.setCustomName(randomName);
        currentMob.setCustomNameVisible(true);
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBlockDestroy(@NotNull BlockBreakEvent blockBreakEvent)
    {
        int score = Math.max(ScoreboardValues.BLOCK_DESTROY,blockBreakEvent.getExpToDrop());
        scoreboard.count(blockBreakEvent.getPlayer(),score);
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBlockFade(@NotNull BlockFadeEvent blockFadeEvent)
    {
        switch (blockFadeEvent.getBlock().getType())
        {
            default:
                newsReceivers.stream().forEach(x -> x.unhandledBlockFade(blockFadeEvent));
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBlockFertilize(@NotNull BlockFertilizeEvent blockFertilizeEvent)
    {
        scoreboard.count(blockFertilizeEvent.getPlayer(),ScoreboardValues.BLOCK_FERTILIZE);
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBlockGrow(@NotNull BlockGrowEvent blockGrowEvent)
    {
        switch (blockGrowEvent.getBlock().getType())
        {
            default:
                newsReceivers.stream().forEach(x -> x.unhandledBlockGrow(blockGrowEvent));
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBlockIgnite(@NotNull BlockIgniteEvent blockIgniteEvent)
    {
        if (blockIgniteEvent.getPlayer() != null)
        {
            scoreboard.count(blockIgniteEvent.getPlayer(), ScoreboardValues.BLOCK_IGNITE);
            return;
        }
        if (blockIgniteEvent.getCause() != BlockIgniteEvent.IgniteCause.SPREAD)
        {
            newsReceivers.stream().forEach(x -> x.fireStarted(blockIgniteEvent.getBlock()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBlockPlaced(@NotNull BlockPlaceEvent blockPlaceEvent)
    {
        scoreboard.count(blockPlaceEvent.getPlayer(),1);
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent entityBlockFormEvent)
    {

    }
}

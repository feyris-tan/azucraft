package moe.yo3explorer.azucraft.newscaster;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;

public interface NewsReceiver
{
    void unhandledBlockFade(BlockFadeEvent blockFadeEvent);
    void unhandledBlockGrow(BlockGrowEvent blockGrowEvent);

    void fireStarted(Block block);
}

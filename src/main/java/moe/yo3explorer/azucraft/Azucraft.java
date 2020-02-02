package moe.yo3explorer.azucraft;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public class Azucraft extends JavaPlugin implements SaveConfigRequestHandler
{
    private AzucraftConfiguration configuration;
    private Connection sqlConnection;
    private SQLOpenHandler sqlOpenHandler;
    private AzucraftListener listener;

    @Override
    public void onDisable() {
        getLogger().info("Stopping Azucraft...");
        if (sqlConnection == null)
        {
            try {
                sqlConnection.close();
            } catch (SQLException e) {
                getLogger().info("Failed to disconnect from DB!");
            }
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("Starting Azucraft...");
        configuration = new AzucraftConfiguration(this.getConfig(),this);
        if (configuration.isDbEnable())
        {
            sqlConnection = configuration.getPgsqlConnection();
            sqlOpenHandler = new SQLOpenHandler(sqlConnection,configuration.getServerUuid());
            listener = new AzucraftListener(sqlOpenHandler,getLogger());
            renameMobsInit();
            getServer().getPluginManager().registerEvents(listener,this);
        }
        else
        {
            getLogger().info("Azucraft PostgreSQL Connectivity is disabled.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, @org.jetbrains.annotations.NotNull Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("azucraftenabled"))
        {
            sender.sendMessage("Azucraft is enabled on this server.");
            return true;
        }

        return false;
    }

    @Override
    public void requestSaveConfig() {
        saveConfig();
    }

    private void renameMobsInit()
    {
        try {
            sqlConnection.setAutoCommit(false);

            for (World world : getServer().getWorlds()) {
                for (Chunk loadedChunk : world.getLoadedChunks()) {
                    listener.renameMobs(loadedChunk);
                }
            }
            sqlConnection.commit();
            sqlConnection.setAutoCommit(true);
        } catch (SQLException e) {
            getLogger().warning("Failed renaming mobs at startup.");
        }
    }


}

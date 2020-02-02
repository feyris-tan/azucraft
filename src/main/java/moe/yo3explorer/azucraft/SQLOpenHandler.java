package moe.yo3explorer.azucraft;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

public class SQLOpenHandler {
    private final Connection connection;
    private final UUID serverUUID;
    private boolean serverIdKnown;
    private int serverId;

    public SQLOpenHandler(Connection sqlConnection,UUID uuid) {
        this.connection = sqlConnection;
        this.serverUUID = uuid;
    }

    private PreparedStatement testForMobCommand;
    public boolean testForMob(Mob mob) throws SQLException {
        if (testForMobCommand == null)
        {
            testForMobCommand = connection.prepareStatement("SELECT dateAdded FROM mobinfo WHERE uuid=?");
        }
        testForMobCommand.setString(1,mob.getUniqueId().toString());
        ResultSet resultSet = testForMobCommand.executeQuery();
        boolean result = resultSet.next();
        resultSet.close();
        return result;
    }

    private PreparedStatement getRandomNameCommand;
    public String getRandomName() throws SQLException {
        if (getRandomNameCommand == null)
        {
            getRandomNameCommand = connection.prepareStatement("SELECT name From mobnames ORDER BY random() LIMIT 1");
        }
        ResultSet resultSet = getRandomNameCommand.executeQuery();
        resultSet.next();
        String result = resultSet.getString(1);
        resultSet.close();
        return result;
    }

    private PreparedStatement insertMobCommand;
    public void insertMob(Mob mob) throws SQLException {
        if (insertMobCommand == null)
        {
            insertMobCommand = connection.prepareStatement(
                    "INSERT INTO mobinfo " +
                            "(uuid,worldserial,cname,x,y,z,type,dead) VALUES " +
                            "(?,?,?,?,?,?,?,?)");
        }
        insertMobCommand.setString(1,mob.getUniqueId().toString());
        insertMobCommand.setInt(2,getWorldSerial(mob.getWorld()));
        insertMobCommand.setString(3,mob.getCustomName());
        insertMobCommand.setInt(4,mob.getLocation().getBlockX());
        insertMobCommand.setInt(5,mob.getLocation().getBlockY());
        insertMobCommand.setInt(6,mob.getLocation().getBlockZ());
        insertMobCommand.setInt(7,mob.getType().ordinal());
        insertMobCommand.setBoolean(8,mob.isDead());
        insertMobCommand.executeUpdate();
    }

    private PreparedStatement getWorldSerialCommand;
    private int getWorldSerial(World world) throws SQLException {
        if (getWorldSerialCommand == null)
        {
            getWorldSerialCommand = connection.prepareStatement("SELECT serial FROM worldinfo WHERE uid=?");
        }
        getWorldSerialCommand.setString(1,world.getUID().toString());
        ResultSet resultSet = getWorldSerialCommand.executeQuery();
        if (resultSet.next())
        {
            int result = resultSet.getInt(1);
            resultSet.close();
            return result;
        }
        else
        {
            resultSet.close();
            insertWorld(world);
            return getWorldSerial(world);
        }
    }

    private PreparedStatement insertWorldCommand;
    private void insertWorld(World world) throws SQLException {
        if (insertWorldCommand == null)
        {
            insertWorldCommand = connection.prepareStatement(
                    "INSERT INTO worldinfo " +
                    "(uid,serverid,structures,difficulty,environment,name,seed) VALUES " +
                    "(?,?,?,?,?,?,?)");
        }
        insertWorldCommand.setString(1,world.getUID().toString());
        insertWorldCommand.setInt(2,getServerId());
        insertWorldCommand.setBoolean(3,world.canGenerateStructures());
        insertWorldCommand.setInt(4,world.getDifficulty().ordinal());
        insertWorldCommand.setInt(5,world.getEnvironment().ordinal());
        insertWorldCommand.setString(6,world.getName());
        insertWorldCommand.setLong(7,world.getSeed());
        insertWorldCommand.executeUpdate();
    }

    private PreparedStatement getServerIdCommand;
    private int getServerId() throws SQLException {
        if (serverIdKnown)
        {
            return serverId;
        }
        if (getServerIdCommand == null)
        {
            getServerIdCommand = connection.prepareStatement("SELECT id FROM serverinfo WHERE uuid=?");
        }
        getServerIdCommand.setString(1,serverUUID.toString());
        ResultSet resultSet = getServerIdCommand.executeQuery();
        if (resultSet.next())
        {
            serverIdKnown = true;
            serverId = resultSet.getInt(1);
            resultSet.close();
        }
        else
        {
            resultSet.close();
            insertCurrentServerInfo();
        }
        return getServerId();
    }

    private PreparedStatement insertCurrentServerInfoCommand;
    private void insertCurrentServerInfo() throws SQLException {
        if (insertCurrentServerInfoCommand == null)
        {
            insertCurrentServerInfoCommand = connection.prepareStatement("INSERT INTO serverinfo " +
                    "(uuid,hostname,javahome,javavendor,javaversion,osarch,osname,osversion,username) VALUES " +
                    "(?,?,?,?,?,?,?,?,?)");
        }
        Properties jvmProps = System.getProperties();
        insertCurrentServerInfoCommand.setString(1,serverUUID.toString());
        insertCurrentServerInfoCommand.setString(2,getHostname());
        insertCurrentServerInfoCommand.setString(3,jvmProps.getProperty("java.home"));
        insertCurrentServerInfoCommand.setString(4,jvmProps.getProperty("java.vendor"));
        insertCurrentServerInfoCommand.setString(5,jvmProps.getProperty("java.version"));
        insertCurrentServerInfoCommand.setString(6,jvmProps.getProperty("os.arch"));
        insertCurrentServerInfoCommand.setString(7,jvmProps.getProperty("os.name"));
        insertCurrentServerInfoCommand.setString(8,jvmProps.getProperty("os.version"));
        insertCurrentServerInfoCommand.setString(9,jvmProps.getProperty("user.name"));
        insertCurrentServerInfoCommand.executeUpdate();
    }

    @Nullable
    private String getHostname()
    {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private PreparedStatement testForChunkCommand;
    public boolean testForChunk(Chunk loadedChunk) throws SQLException {
        if (testForChunkCommand == null)
        {
            testForChunkCommand = connection.prepareStatement("SELECT dateAdded FROM chunkinfo WHERE worldserial = ? AND x = ? AND z = ?");
        }

        testForChunkCommand.setInt(1,getWorldSerial(loadedChunk.getWorld()));
        testForChunkCommand.setInt(2,loadedChunk.getX());
        testForChunkCommand.setInt(3,loadedChunk.getZ());
        ResultSet resultSet = testForChunkCommand.executeQuery();
        boolean result = resultSet.next();
        resultSet.close();
        return result;
    }

    private PreparedStatement insertChunkCommand;
    public void insertChunk(Chunk loadedChunk) throws SQLException {
        if (insertChunkCommand == null)
        {
            insertChunkCommand = connection.prepareStatement("INSERT INTO chunkinfo (worldserial, x, z) VALUES (?,?,?)");
        }
        insertChunkCommand.setInt(1,getWorldSerial(loadedChunk.getWorld()));
        insertChunkCommand.setInt(2,loadedChunk.getX());
        insertChunkCommand.setInt(3,loadedChunk.getZ());
        insertChunkCommand.executeUpdate();
    }

    private PreparedStatement testForPlayerCommand;
    public boolean testForPlayer(Player player) throws SQLException {
        if (testForPlayerCommand == null)
        {
            testForPlayerCommand = connection.prepareStatement("SELECT dateadded FROM playerinfo WHERE uuid = ?");
        }
        testForPlayerCommand.setString(1,player.getUniqueId().toString());
        ResultSet resultSet = testForPlayerCommand.executeQuery();
        boolean result = resultSet.next();
        resultSet.close();
        return result;
    }

    private PreparedStatement insertPlayerCommand;
    public void insertPlayer(Player player) throws SQLException {
        if (insertPlayerCommand == null)
        {
            insertPlayerCommand = connection.prepareStatement("INSERT INTO playerinfo " +
                    "(uuid,name,locale) VALUES " +
                    "(?,?,?)");
        }
        insertPlayerCommand.setString(1,player.getUniqueId().toString());
        insertPlayerCommand.setString(2,player.getName());
        insertPlayerCommand.setString(3,player.getLocale());
        insertPlayerCommand.executeUpdate();
    }

    private PreparedStatement insertLoginCommand;
    public void insertLogin(PlayerLoginEvent playerLoginEvent) throws SQLException {
        if (insertLoginCommand == null)
        {
            insertLoginCommand = connection.prepareStatement("INSERT INTO login " +
                    "(playerserial,ip,result) VALUES " +
                    "(?,?,?)");
        }
        insertLoginCommand.setInt(1,getPlayerSerial(playerLoginEvent.getPlayer()));
        insertLoginCommand.setString(2,playerLoginEvent.getAddress().toString());
        insertLoginCommand.setInt(3,playerLoginEvent.getResult().ordinal());
        insertLoginCommand.executeUpdate();
    }

    private PreparedStatement getPlayerSerialCommand;
    private int getPlayerSerial(Player player) throws SQLException {
        if (getPlayerSerialCommand == null)
        {
            getPlayerSerialCommand = connection.prepareStatement("SELECT serial FROM playerinfo WHERE uuid=?");
        }
        getPlayerSerialCommand.setString(1,player.getUniqueId().toString());
        ResultSet resultSet = getPlayerSerialCommand.executeQuery();
        if (resultSet.next())
        {
            int result = resultSet.getInt(1);
            resultSet.close();
            return result;
        }
        else
        {
            resultSet.close();
            insertPlayer(player);
            return getPlayerSerial(player);
        }
    }

    private PreparedStatement setMobSpawnReasonCommand;
    public void setMobSpawnReason(Mob mob, CreatureSpawnEvent.SpawnReason spawnReason) throws SQLException {
        if (setMobSpawnReasonCommand == null)
        {
            setMobSpawnReasonCommand = connection.prepareStatement("UPDATE mobinfo SET spawnreason = ? WHERE uuid = ?");
        }
        setMobSpawnReasonCommand.setInt(1,spawnReason.ordinal());
        setMobSpawnReasonCommand.setString(2,mob.getUniqueId().toString());
        setMobSpawnReasonCommand.executeUpdate();
    }

    private PreparedStatement getPlayerScoreCommand;
    public long getPlayerScore(Player player) throws SQLException {
        if (getPlayerScoreCommand == null)
        {
            getPlayerSerialCommand = connection.prepareStatement("SELECT score FROM playerinfo WHERE uuid=?");
        }
        getPlayerSerialCommand.setString(1,player.getUniqueId().toString());
        ResultSet resultSet = getPlayerSerialCommand.executeQuery();
        long result = 0;
        if (resultSet.next())
            result = resultSet.getLong(1);
        return result;
    }

    private PreparedStatement setPlayerScoreCommand;
    public void setPlayerScore(Player player, long score) throws SQLException {
        if (setPlayerScoreCommand == null)
        {
            setPlayerScoreCommand = connection.prepareStatement("UPDATE playerinfo SET score = ? WHERE uuid = ?");
        }
        setPlayerScoreCommand.setLong(1,score);
        setPlayerScoreCommand.setString(2,player.getUniqueId().toString());
        setPlayerScoreCommand.executeUpdate();
    }
}

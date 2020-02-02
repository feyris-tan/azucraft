package moe.yo3explorer.azucraft;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.postgresql.jdbc.PgConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class AzucraftConfiguration {
    private final Date firstBoot;
    private final UUID serverUuid;
    private final boolean dbEnable;
    private final String dbIp;
    private final int dbPort;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbDatabase;
    private final String dbSchema;
    private boolean initDone;

    public AzucraftConfiguration(@NotNull FileConfiguration config, SaveConfigRequestHandler azucraft) {
        this.initDone = config.getBoolean("initDone",false);
        if (!initDone) {
            config.set("initDone", true);
            config.set("firstBoot", new Date().getTime());
            config.set("serverUuid", UUID.randomUUID().toString());
            config.set("pgsql.enabled",false);
            config.set("pgsql.ip","127.0.0.1");
            config.set("pgsql.port",5432);
            config.set("pgsql.username","postgres");
            config.set("pgsql.password","");
            config.set("pgsql.database","postgres");
            config.set("pgsql.schema","azucraft");
            azucraft.requestSaveConfig();
        }
        this.firstBoot = new Date(config.getLong("firstBoot"));
        this.serverUuid = UUID.fromString(config.getString("serverUuid"));
        this.dbEnable = config.getBoolean("pgsql.enabled");
        this.dbIp = config.getString("pgsql.ip");
        this.dbPort = config.getInt("pgsql.port");
        this.dbUsername = config.getString("pgsql.username");
        this.dbPassword = config.getString("pgsql.password");
        this.dbDatabase = config.getString("pgsql.database");
        this.dbSchema = config.getString("pgsql.schema");
    }

    public boolean isDbEnable() {
        return dbEnable;
    }

    private Connection pgConnection;
    public Connection getPgsqlConnection()
    {
        if (pgConnection == null)
        {
            String url = String.format("jdbc:postgresql://%s:%d/%s",dbIp,dbPort,dbDatabase);

            Properties properties = new Properties();
            properties.setProperty("user",dbUsername);
            properties.setProperty("password",dbPassword);
            properties.setProperty("ssl","false");
            properties.setProperty("tcpKeepAlive","true");
            properties.setProperty("ApplicationName","Azucraft");
            properties.setProperty("currentSchema","minecraftstats");

            org.postgresql.Driver driver = new org.postgresql.Driver();
            try {
                pgConnection = driver.connect(url,properties);
            } catch (SQLException e) {
                throw new AzucraftException(e);
            }
        }
        return pgConnection;
    }

    public UUID getServerUuid() {
        return serverUuid;
    }
}

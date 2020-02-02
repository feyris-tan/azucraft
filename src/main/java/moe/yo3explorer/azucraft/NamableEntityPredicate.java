package moe.yo3explorer.azucraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import java.util.function.Predicate;
import java.util.logging.Logger;

public class NamableEntityPredicate implements Predicate<Entity> {

    public NamableEntityPredicate()
    {
        seen = new boolean[EntityType.values().length];
        logger = Bukkit.getLogger();
    }

    private boolean[] seen;
    private Logger logger;

    @Override
    public boolean test(Entity entity) {
        boolean result =  entity instanceof LivingEntity;
        if (!result)
        {
            if (!seen[entity.getType().ordinal()])
            {
                seen[entity.getType().ordinal()] = true;
                logger.info(String.format("%s will not be named.",entity.getType().name()));
            }
        }
        return result;
    }
}

package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGEntity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class EntityDictionary {

    // Map to store loaded entities from entities.yml
    public static final Map<String, RPGEntity> entities = new HashMap<>();

    public static final String ENTITY_PREFIX = "entity.";
    public static final String ENTITY_NAME = ENTITY_PREFIX + "name";
    public static final String ENTITY_LEVEL = ENTITY_PREFIX + "level";
    public static final String ENTITY_MAX_HEALTH = ENTITY_PREFIX + "maxHealth";
    public static final String ENTITY_HEALTH = ENTITY_PREFIX + "health";
    public static final String ENTITY_ATTACK = ENTITY_PREFIX + "attack";
    public static final String ENTITY_DEFENSE = ENTITY_PREFIX + "defense";
    public static final String ENTITY_SPEED = ENTITY_PREFIX + "speed";
    public static final String ENTITY_EXPERIENCE_AFTER_DEFEAT = ENTITY_PREFIX + "experienceAfterDefeat";
    public static final String ENTITY_DROPS = ENTITY_PREFIX + "drops";
    public static final String ENTITY_IS_BOSS = ENTITY_PREFIX + "isBoss";
    public static final String ENTITY_IS_FRIENDLY = ENTITY_PREFIX + "isFriendly";
    public static final String ENTITY_HAS_VISIBLE_NAME = ENTITY_PREFIX + "hasVisibleName";

    public static final String ENTITY_TYPE = ENTITY_PREFIX + "type";
    public static final String ENTITY_EQUIPMENT_HELMET = ENTITY_PREFIX + "equipment.helmet";
    public static final String ENTITY_EQUIPMENT_CHESTPLATE = ENTITY_PREFIX + "equipment.chestplate";
    public static final String ENTITY_EQUIPMENT_LEGGINGS = ENTITY_PREFIX + "equipment.leggings";
    public static final String ENTITY_EQUIPMENT_BOOTS = ENTITY_PREFIX + "equipment.boots";
    public static final String ENTITY_EQUIPMENT_MAIN_HAND = ENTITY_PREFIX + "equipment.mainHand";
    public static final String ENTITY_EQUIPMENT_OFF_HAND = ENTITY_PREFIX + "equipment.offHand";
    public static final String ENTITY_BOSS_BAR = ENTITY_PREFIX + "bossBar";
    public static final String ENTITY_UUID = ENTITY_PREFIX + "uuid";
    public static final String ENTITY_LOCATION_WORLD = ENTITY_PREFIX + "location.world";
    public static final String ENTITY_LOCATION_X = ENTITY_PREFIX + "location.x";
    public static final String ENTITY_LOCATION_Y = ENTITY_PREFIX + "location.y";
    public static final String ENTITY_LOCATION_Z = ENTITY_PREFIX + "location.z";

    /**
     * Gets an entity by its ID from the loaded entities.
     * @param id The entity ID
     * @return The RPGEntity or null if not found
     */
    public static RPGEntity getEntity(String id) {
        return entities.get(id);
    }


    public static RPGEntity BANDIT_TUTORIAL_MELEE() {
        RPGEntity rpgEntity = new RPGEntity();
        rpgEntity.visibleName = "Bandit";
        rpgEntity.level = 1;
        rpgEntity.maxHealth = 20.0;
        rpgEntity.health = 20.0;
        rpgEntity.attack = 5.0;
        rpgEntity.defense = 2;
        rpgEntity.speed = 0.2;
        rpgEntity.experienceAfterDefeat = 10;
        rpgEntity.isBoss = false;
        rpgEntity.isFriendly = false;
        rpgEntity.hasVisibleName = true;
        rpgEntity.entityType = EntityType.VINDICATOR;
        return rpgEntity;
    }
}

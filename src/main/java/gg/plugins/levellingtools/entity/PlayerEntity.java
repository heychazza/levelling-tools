package gg.plugins.levellingtools.entity;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Indexed;
import gg.plugins.levellingtools.config.ConfigCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Entity(value = "player", noClassnameStored = true)
public class PlayerEntity extends BaseEntity
{
    @Indexed
    private String uuid;
    private String username;
    private String lowercaseUsername;
    private double experience;
    private int blocksBroken;
    private int level;
    
    public static PlayerEntity getUser(final UUID player) {
        final PlayerEntity playerEntity = ConfigCache.getDB().createQuery(PlayerEntity.class).filter("uuid", player.toString()).get();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        if (playerEntity == null) {
            final PlayerEntity newPlayerEntity = new PlayerEntity();
            newPlayerEntity.setUuid(player.toString());
            newPlayerEntity.setUsername(offlinePlayer.getName());
            newPlayerEntity.setLowercaseUsername(offlinePlayer.getName().toLowerCase());
            newPlayerEntity.setExperience(0);
            newPlayerEntity.setBlocksBroken(0);
            newPlayerEntity.setLevel(1);
            ConfigCache.getDB().save(newPlayerEntity);
            return newPlayerEntity;
        }
        return playerEntity;
    }
    
    public String getUuid() {
        return this.uuid;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getLowercaseUsername() {
        return this.lowercaseUsername;
    }
    
    public double getExperience() {
        return this.experience;
    }
    
    public int getBlocksBroken() {
        return this.blocksBroken;
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
    
    public void setUsername(final String username) {
        this.username = username;
    }
    
    public void setLowercaseUsername(final String lowercaseUsername) {
        this.lowercaseUsername = lowercaseUsername;
    }
    
    public void setExperience(final double experience) {
        this.experience = experience;
    }
    
    public void setBlocksBroken(final int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }
    
    public void setLevel(final int level) {
        this.level = level;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + " [id=" + this.id + ", uuid=" + this.getUuid() + ", lowercaseUsername=" + this.getLowercaseUsername() + ", experience=" + this.getExperience() + "]";
    }
}

package fr.thefirstalpha.mastersdc.chest;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public abstract class MasterChest {
    protected long chest_id;
    protected UUID owner;
    protected Location location;
    protected Inventory inventory;

    public MasterChest(long chest_id, UUID owner, Location location, Inventory inventory) {
        this.chest_id = chest_id;
        this.owner = owner;
        this.location = location;
        this.inventory = inventory;
    }

    public MasterChest(long chest_id, UUID owner, Location location) throws Exception {
        this.chest_id = chest_id;
        this.owner = owner;
        this.location = location;
        Block block = location.getWorld().getBlockAt(location);
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            this.inventory = chest.getInventory();
        } else {
            throw new Exception("Can't find chest at this location " + location.toString());
        }

    }

    public long getChest_id() {
        return chest_id;
    }

    public void setChest_id(long chest_id) {
        this.chest_id = chest_id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean checkLocation(Location loc) {
        return this.location.equals(loc);
    }

    public abstract String getInfo();

    public abstract ChestType getType();

    public String getStringLocation() {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getWorld().getName();
    }

    public enum ChestType {ALPHA, BETA}
}

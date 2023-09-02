package fr.thefirstalpha.mastersdc.chest;

import fr.thefirstalpha.mastersdc.Database;
import fr.thefirstalpha.mastersdc.MasterSDC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class BetaChest extends MasterChest {
    private final long alphaChest;
    private final Set<Material> filter;

    public BetaChest(long chest_id, UUID owner, long alphaChest, Set<Material> filter, Location location) throws Exception {
        super(chest_id, owner, location);
        this.alphaChest = alphaChest;
        this.filter = filter;
    }

    public HashMap<Integer, ItemStack> addItem(ItemStack item) {
        return this.inventory.addItem(item);
    }

    public Set<Material> getFilter() {
        return filter;
    }

    public ChestType getType() {
        return ChestType.BETA;
    }

    @Override
    public String getInfo() {
        MasterChest chest = Database.getChest(alphaChest);
        return ChatColor.BOLD + "======[INFO]======\n" + ChatColor.RESET
                + ChatColor.GRAY + "Type: " + ChatColor.RED + "Beta\n"
                + ChatColor.GRAY + "Owner: " + ChatColor.BLUE + MasterSDC.instance.getServer().getOfflinePlayer(owner).getName() + "\n"
                + ChatColor.GRAY + "location: " + ChatColor.AQUA + getStringLocation() + "\n"
                + ChatColor.GRAY + "Filter: " + ChatColor.GOLD + (getFilter().size() > 0 ? getFilter().toString() : "Autre(s)") + "\n"
                + ChatColor.GRAY + "Alpha Chest: " + ChatColor.YELLOW + ((chest != null) ? chest.getStringLocation() : "Introuvable");
    }

    public long getAlphaChest() {
        return alphaChest;
    }
}

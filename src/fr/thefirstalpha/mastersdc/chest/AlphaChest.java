package fr.thefirstalpha.mastersdc.chest;

import fr.thefirstalpha.mastersdc.Database;
import fr.thefirstalpha.mastersdc.MasterSDC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AlphaChest extends MasterChest {
    private final String name;

    public AlphaChest(long chest_id, UUID owner, String name, Location location) throws Exception {
        super(chest_id, owner, location);
        this.name = name;
    }

    public void dispatchItems() {
        if (inventory == null) {
            Block block = location.getBlock();
            Chest chest = (Chest) block.getState();
            inventory = chest.getInventory();
            MasterSDC.log.info("[SDC] Alpha chest " + chest_id + " inventory is null");
        }
        inventory.forEach(itemStack -> {
            if (itemStack != null) {
                List<BetaChest> betaChest = Database.getLinkedBetaChest(chest_id, itemStack.getType());
                if (betaChest.size() > 0) {
                    for (BetaChest chest : betaChest) {
                        HashMap<Integer, ItemStack> nonfit = chest.addItem(itemStack);
                        if (nonfit.size() > 0) {
                            itemStack.setAmount(nonfit.get(0).getAmount());
                        } else {
                            itemStack.setAmount(0);
                            break;
                        }
                    }
                }
            }
        });

    }

    public ChestType getType() {
        return ChestType.ALPHA;
    }

    public String getInfo() {
        return ChatColor.BOLD + "======[INFO]======\n" + ChatColor.RESET
                + ChatColor.GRAY + "Type: " + ChatColor.RED + "Alpha\n"
                + ChatColor.GRAY + "Owner: " + ChatColor.BLUE + MasterSDC.instance.getServer().getOfflinePlayer(owner).getName() + "\n"
                + ChatColor.GRAY + "location: " + ChatColor.AQUA + getStringLocation() + "\n"
                + ChatColor.GRAY + "Name: " + ChatColor.GOLD + name + "\n";
    }

    public String getName() {
        return name;
    }
}

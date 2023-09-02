package fr.thefirstalpha.mastersdc;

import fr.thefirstalpha.mastersdc.chest.AlphaChest;
import fr.thefirstalpha.mastersdc.chest.MasterChest;
import org.bukkit.Location;

public class CustomRunnable implements Runnable {
    private final Location location;

    public CustomRunnable(Location location) {
        this.location = location;
    }

    @Override
    public void run() {
        MasterSDC.updates.remove(location);
        MasterChest masterChest = Database.getChest(location);
        if (masterChest instanceof AlphaChest)
            MasterSDC.instantUpdateChest((AlphaChest) masterChest);
    }
}

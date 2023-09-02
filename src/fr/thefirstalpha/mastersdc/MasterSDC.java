package fr.thefirstalpha.mastersdc;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import fr.thefirstalpha.mastersdc.chest.AlphaChest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class MasterSDC extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    public static MasterSDC instance;
    public static HashMap<UUID, String[]> commands = new HashMap<>();
    public static List<UUID> persists = new ArrayList<>();
    public static TextComponent prefix = Component.text("[SDC] ").color(NamedTextColor.YELLOW);
    public static Map<Location, Long> updates = new HashMap<>();

    public static boolean lwc_protection = false;
    public static LWC lwc = null;


    public static void updateChest(AlphaChest alphaChest) {
        Bukkit.getScheduler().runTaskLater(MasterSDC.instance, alphaChest::dispatchItems, 2);
    }

    public static void instantUpdateChest(AlphaChest alphaChest) {
        alphaChest.dispatchItems();
    }

    public static boolean canEditChest(Block block, Player player) {
        if (lwc_protection)
            if (lwc.findProtection(block) != null)
                return lwc.canAccessProtection(player, block);
        return true;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        saveDefaultConfig();
        Database db = new Database();
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        new CommandSDC(this);

        if ((this.getServer().getPluginManager().isPluginEnabled("LWC"))) {
            log.info("LWC plugin detected");
            Plugin lwcp = Bukkit.getPluginManager().getPlugin("LWC");
            lwc = ((LWCPlugin) lwcp).getLWC();
            lwc_protection = true;
        }

    }


    @Override
    public void onDisable() {
        super.onDisable();
        try {
            Database.conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

package fr.thefirstalpha.mastersdc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Utils {

    public static List<String> argsFilter(String arg, Collection c) {
        List<String> choice = new ArrayList<String>(c);
        choice.removeIf(value -> !value.contains(arg));
        return choice;
    }

    public static int getMaxAlphaChest(Player player) {
        int max = 0;
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        CachedPermissionData permissionData = user.getCachedData().getPermissionData(QueryOptions.builder(QueryMode.NON_CONTEXTUAL).build());
        for (Map.Entry<String, Boolean> entry : permissionData.getPermissionMap().entrySet()) {
            String perm = entry.getKey();
            if (perm.startsWith("sdc.limit.")) {
                try {
                    int value = Integer.parseInt(perm.replace("sdc.limit.", ""));
                    if (value > max)
                        max = value;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return max;
    }

    public static boolean checkPerm(Player player, String name) {
        if (player.isOp())
            return true;
        return player.hasPermission(name);
    }

    public static void setChestName(Chest chest, String name) {
        //TODO, change name not working
        if (name == null)
            chest.customName(null);
        else
            chest.customName(Component.text(name).style(Style.style(TextDecoration.BOLD)));
        chest.update();
    }
}

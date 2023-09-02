package fr.thefirstalpha.mastersdc;

import fr.thefirstalpha.mastersdc.chest.BetaChest;
import fr.thefirstalpha.mastersdc.chest.AlphaChest;
import fr.thefirstalpha.mastersdc.chest.MasterChest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventManager implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) {
            if (MasterSDC.commands.containsKey(event.getPlayer().getUniqueId())) {
                String[] cmd = MasterSDC.commands.get(event.getPlayer().getUniqueId());

                //Don't clean command if persist activated
                if (!MasterSDC.persists.contains(event.getPlayer().getUniqueId()))
                    MasterSDC.commands.remove(event.getPlayer().getUniqueId());
                event.setCancelled(true);
                if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) && !event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
                    event.getPlayer().sendMessage(Component.text("Vous n'avez pas la permission de faire ça ici.").color(NamedTextColor.RED));
                    return;
                }
                if (!Utils.checkPerm(event.getPlayer(), "deca.sdc.use")) {
                    event.getPlayer().sendMessage(Component.text("Vous n'avez pas la permission.").color(NamedTextColor.RED));
                    return;
                }
                switch (cmd[0]) {
                    case "create":
                        switch (cmd[1]) {
                            case "alpha": {
                                Block block = event.getClickedBlock();
                                Chest chest = (Chest) block.getState();
                                if (!MasterSDC.canEditChest(block, event.getPlayer())) {
                                    event.getPlayer().sendMessage(Component.text("Ce coffre ne vous appartient pas.").color(NamedTextColor.RED));
                                    return;
                                }
                                MasterChest masterChest = Database.createAlphaChest(chest.getInventory().getLocation(), event.getPlayer().getUniqueId(), cmd[2]);
                                if (masterChest != null) {
                                    Utils.setChestName(chest, "Alpha");
                                    chest.getPersistentDataContainer().set(new NamespacedKey(MasterSDC.instance, "SDC"), PersistentDataType.INTEGER, 1);
                                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Coffre Alpha créé.").color(NamedTextColor.GREEN)));
                                } else {
                                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce coffre est déjà enregistré.").color(NamedTextColor.RED)));
                                }
                            }
                            break;
                            case "beta": {
                                Block block = event.getClickedBlock();
                                Chest chest = (Chest) block.getState();
                                if (!MasterSDC.canEditChest(block, event.getPlayer())) {
                                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce coffre ne vous appartient pas.").color(NamedTextColor.RED)));
                                    return;
                                }
                                Set<Material> material = new HashSet<>();
                                if (cmd.length > 3) {
                                    for (int i = 3; i < cmd.length; i++)
                                        material.add(Material.valueOf(cmd[i]));
                                } else if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                                    material.add(event.getPlayer().getInventory().getItemInMainHand().getType());
                                }
                                UUID owner = event.getPlayer().getUniqueId();
                                String name = cmd[2];
                                if (cmd[2].contains(":")) {
                                    String[] parseCMD = cmd[2].split(":");
                                    owner = Bukkit.getPlayerUniqueId(parseCMD[0]);
                                    name = parseCMD[1];
                                }
                                MasterChest alphaChest = Database.getAlphaChest(name, owner);
                                if (alphaChest == null) {
                                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Impossible de trouver le coffre alpha " + cmd[2] + ".").color(NamedTextColor.RED)));
                                    break;
                                }
                                if (owner == null || !owner.equals(event.getPlayer().getUniqueId())) {
                                    if (!Database.getShare(alphaChest.getChest_id()).contains(event.getPlayer().getUniqueId().toString())) {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce joueur ne partage pas ce coffre avec vous.").color(NamedTextColor.RED)));
                                        return;
                                    }
                                }
                                MasterChest masterChest = Database.createBetaChest(chest.getInventory().getLocation(), material, alphaChest.getChest_id(), owner);
                                if (masterChest != null) {
                                    Utils.setChestName(chest, "Beta");
                                    if (material.size() > 0) {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Coffre Beta créé avec un filtre ").color(NamedTextColor.GREEN).append(Component.text(material.toString()).style(Style.style(TextDecoration.BOLD)))));
                                    } else {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Coffre Beta créé.").color(NamedTextColor.GREEN)));
                                    }
                                } else {
                                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce coffre est déjà enregistré.").color(NamedTextColor.RED)));
                                }
                            }
                            break;
                        }

                        break;
                    case "remove": {
                        Block block = event.getClickedBlock();
                        if (block.getType() == Material.CHEST) {
                            Chest chest = (Chest) block.getState();
                            Location loc = chest.getInventory().getLocation();
                            MasterChest masterChest = Database.getChest(loc);
                            if (masterChest != null) {
                                if (!masterChest.getOwner().equals(event.getPlayer().getUniqueId())) {
                                    if (!Database.getShare(((BetaChest) masterChest).getAlphaChest()).contains(event.getPlayer().getUniqueId().toString())) {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce joueur ne partage pas ce coffre avec vous.").color(NamedTextColor.RED)));
                                        return;
                                    }
                                }
                                if (Database.removeChest(masterChest.getChest_id())) {
                                    Utils.setChestName(chest, null);
                                    if (masterChest instanceof AlphaChest) {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Vous venez de détruire un coffre Alpha: " + ((AlphaChest) masterChest).getName()).color(NamedTextColor.YELLOW)));
                                    } else {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Vous venez de détruire un coffre Beta: " + ((BetaChest) masterChest).getFilter()).color(NamedTextColor.YELLOW)));
                                    }
                                } else {
                                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Erreur pendant la suppression du coffre.").color(NamedTextColor.RED)));
                                }
                            }
                        }
                    }
                    break;
                    case "addFilter":
                    case "removeFilter": {
                        Block block = event.getClickedBlock();
                        if (block.getType() == Material.CHEST) {
                            Chest chest = (Chest) block.getState();
                            Location loc = chest.getInventory().getLocation();
                            MasterChest masterChest = Database.getChest(loc);
                            if (masterChest instanceof BetaChest) {
                                if (!masterChest.getOwner().equals(event.getPlayer().getUniqueId())) {
                                    if (!Database.getShare(((BetaChest) masterChest).getAlphaChest()).contains(event.getPlayer().getUniqueId().toString())) {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce joueur ne partage pas ce coffre avec vous.").color(NamedTextColor.RED)));
                                        return;
                                    }
                                }
                                Set<Material> material = new HashSet<>();
                                if (cmd.length > 1) {
                                    for (int i = 1; i < cmd.length; i++)
                                        material.add(Material.valueOf(cmd[i]));
                                } else if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                                    material.add(event.getPlayer().getInventory().getItemInMainHand().getType());
                                }
                                if (cmd[0].equals("addFilter")) {
                                    if (Database.addFilter(masterChest.getChest_id(), material)) {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Filtre appliqué au coffre.").color(NamedTextColor.GREEN)));
                                    } else {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Erreur pendant l'ajout du filtre.").color(NamedTextColor.RED)));
                                    }
                                } else {
                                    if (Database.removeFilter(masterChest.getChest_id(), material)) {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Filtre retiré au coffre.").color(NamedTextColor.GREEN)));
                                    } else {
                                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Erreur pendant l'ajout du filtre.").color(NamedTextColor.RED)));
                                    }
                                }
                            }
                        }
                    }
                    break;
                    case "info":
                        Block block = event.getClickedBlock();
                        Chest chest = (Chest) block.getState();
                        Location loc = chest.getInventory().getLocation();
                        MasterChest masterChest = Database.getChest(loc);
                        if (masterChest != null)
                            event.getPlayer().sendMessage(masterChest.getInfo());
                        else
                            event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce coffre n'est pas enregistré.").color(NamedTextColor.WHITE)));
                        break;
                }
            }
        } else {
            if (MasterSDC.commands.containsKey(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Action annulée.").color(NamedTextColor.RED)));
                MasterSDC.commands.remove(event.getPlayer().getUniqueId());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        Player player = event.getPlayer();
        if (e instanceof ItemFrame) {
            if (player.isSneaking())
                return;
            Location item = e.getLocation().toBlockLocation();
            switch (e.getFacing()) {
                case NORTH:
                    item = item.add(0, 0, 1);
                    break;
                case EAST:
                    item = item.add(-1, 0, 0);
                    break;
                case SOUTH:
                    item = item.add(0, 0, -1);
                    break;
                case WEST:
                    item = item.add(1, 0, 0);
                    break;
                case UP:
                    item = item.add(0, -1, 0);
                    break;
                case DOWN:
                    item = item.add(0, 1, 0);
                    break;
            }
            Block block = item.getBlock();
            if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                event.setCancelled(true);
                Chest chest = (Chest) block.getState();
                if (!MasterSDC.canEditChest(block, player)) {
                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Ce bloc est protégé.").color(NamedTextColor.RED)));
                    return;
                }
                player.openInventory(chest.getInventory());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();

            Location loc = chest.getInventory().getLocation();
            MasterChest masterChest = Database.getChest(loc);
            if (masterChest != null) {
                if (masterChest.getType() == MasterChest.ChestType.ALPHA) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Vous devez d'abord supprimer le coffre alpha.").color(NamedTextColor.RED)));
                    return;
                }
                if (Database.removeChest(masterChest.getChest_id())) {
                    Utils.setChestName(chest, null);
                    if (masterChest instanceof AlphaChest) {
                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Vous venez de détruire un coffre Alpha: " + ((AlphaChest) masterChest).getName()).color(NamedTextColor.YELLOW)));
                    } else {
                        event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Vous venez de détruire un coffre Beta: " + ((BetaChest) masterChest).getFilter()).color(NamedTextColor.YELLOW)));
                    }
                } else {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(MasterSDC.prefix.append(Component.text("Erreur pendant la suppression du coffre.").color(NamedTextColor.RED)));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        Inventory destination = event.getDestination();
        if (destination.getLocation() == null)
            return;
        if (destination.getLocation().getBlock().getState() instanceof Chest) {
            Chest chest = (Chest) destination.getLocation().getBlock().getState();
            if (chest.getCustomName() != null && chest.customName().equals(Component.text("Alpha").style(Style.style(TextDecoration.BOLD)))) {
                if (!MasterSDC.updates.containsKey(chest.getLocation())) {
                    MasterSDC.updates.put(chest.getLocation(), new Date().getTime());
                    Bukkit.getScheduler().runTaskLater(MasterSDC.instance, new CustomRunnable(destination.getLocation()), 5 * 20);
                } else if (MasterSDC.updates.get(chest.getLocation()) + 60000 < new Date().getTime()) {
                    MasterSDC.updates.put(chest.getLocation(), new Date().getTime());
                    Bukkit.getScheduler().runTaskLater(MasterSDC.instance, new CustomRunnable(destination.getLocation()), 5 * 20);
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Location location = null;
        switch (event.getAction()) {
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
                if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.CHEST))
                    location = event.getClickedInventory().getLocation();
                break;
            case MOVE_TO_OTHER_INVENTORY:
                if (event.getView().getTopInventory().getType().equals(InventoryType.CHEST))
                    location = event.getView().getTopInventory().getLocation();
                break;
        }
        if (location != null) {
            MasterChest chest = Database.getChest(location);
            if (chest != null) {
                if (chest instanceof AlphaChest) {
                    AlphaChest alphaChest = (AlphaChest) chest;
                    MasterSDC.updateChest(alphaChest);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDragEvent(InventoryDragEvent event) {
        if (event.getInventory().getLocation() != null && event.getInventory().getType().equals(InventoryType.CHEST)) {
            MasterChest chest = Database.getChest(event.getInventory().getLocation());
            if (chest != null) {
                if (chest instanceof AlphaChest) {
                    AlphaChest alphaChest = (AlphaChest) chest;
                    MasterSDC.updateChest(alphaChest);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        // Clean persist usage
        MasterSDC.persists.remove(event.getPlayer().getUniqueId());
    }
}

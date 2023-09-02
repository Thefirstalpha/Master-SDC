package fr.thefirstalpha.mastersdc;

import fr.thefirstalpha.mastersdc.chest.AlphaChest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandSDC implements CommandExecutor, TabCompleter {

    public CommandSDC(JavaPlugin plugin) {
        plugin.getCommand("sdc").setExecutor(this);
        plugin.getCommand("sdc").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (!Utils.checkPerm((Player) sender, "sdc.use")) {
                sender.sendMessage(Component.text("Vous n'avez pas la permission.").color(NamedTextColor.RED));
                return true;
            }
            if (args.length == 0)
                args = new String[]{"help"};
            switch (args[0]) {
                case "help":
                    MiniMessage.miniMessage().deserialize(MasterSDC.instance.getConfig().getString("messages.help"));
                    break;
                case "create":
                    switch (args[1].toLowerCase()) {
                        case "alpha":
                            if (args.length <= 2) {
                                sender.sendMessage(Component.text("/sdc create alpha <name>").color(NamedTextColor.RED));
                                return true;
                            }
                            if (!Pattern.matches("[a-zA-Z]+", args[2])) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Le nom ne peut contenir que des lettres.").color(NamedTextColor.RED)));
                                return true;
                            }
                            Player player = (Player) sender;
                            int maxAlphaChest = Utils.getMaxAlphaChest(player);
                            if (!player.isOp() && Database.getAlphaChest(player.getUniqueId()).size() >= maxAlphaChest) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Vous avez atteint la limite du nombre de coffre alpha.").color(NamedTextColor.RED)));
                                return true;
                            }
                            if (Database.getAlphaChest(args[2], player.getUniqueId()) != null) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Un coffre alpha nommé " + args[2] + " existe déjà.").color(NamedTextColor.RED)));
                                return true;
                            }
                            sender.sendMessage(MasterSDC.prefix.append(Component.text("Clic droit sur le coffre pour appliquer.").color(NamedTextColor.YELLOW)));
                            MasterSDC.commands.put(((Player) sender).getUniqueId(), args);
                            break;
                        case "beta":
                            if (args.length <= 2) {
                                sender.sendMessage(Component.text("/sdc create beta <name> [material]").color(NamedTextColor.RED));
                                return true;
                            }
                            if (args.length > 3) {
                                for (int i = 3; i < args.length; i++) {
                                    if (Material.getMaterial(args[i]) == null) {
                                        sender.sendMessage(MasterSDC.prefix.append(Component.text(args[i] + " n'est pas un item valide.").color(NamedTextColor.RED)));
                                        return true;
                                    }
                                }
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Clic droit sur le coffre pour appliquer.").color(NamedTextColor.YELLOW)));
                                MasterSDC.commands.put(((Player) sender).getUniqueId(), args);
                            } else {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Clic droit sur le coffre pour appliquer.").color(NamedTextColor.YELLOW)));
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Prenez un item dans votre main pour ajouter un filtre.").color(NamedTextColor.GRAY)));
                                MasterSDC.commands.put(((Player) sender).getUniqueId(), args);
                            }
                            break;
                    }
                    break;
                case "addFilter":
                case "removeFilter":
                    if (args.length > 1) {
                        for (int i = 1; i < args.length; i++) {
                            if (Material.getMaterial(args[i]) == null) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text(args[i] + " n'est pas un item valide.").color(NamedTextColor.RED)));
                                return true;
                            }
                        }
                        sender.sendMessage(MasterSDC.prefix.append(Component.text("Clic droit sur le coffre pour appliquer.").color(NamedTextColor.YELLOW)));
                    } else {
                        sender.sendMessage(MasterSDC.prefix.append(Component.text("Click droit sur le coffre pour appliquer.").color(NamedTextColor.YELLOW)));
                        if (args[0].equals("addFilter"))
                            sender.sendMessage(MasterSDC.prefix.append(Component.text("Prenez un item dans votre main pour appliquer un filtre.").color(NamedTextColor.GRAY)));
                        else
                            sender.sendMessage(MasterSDC.prefix.append(Component.text("Prenez un item dans votre main pour retirer un filtre.").color(NamedTextColor.GRAY)));
                    }
                    MasterSDC.commands.put(((Player) sender).getUniqueId(), args);
                    break;
                case "info":
                case "remove":
                    sender.sendMessage(MasterSDC.prefix.append(Component.text("Clic droit sur le coffre pour appliquer.").color(NamedTextColor.YELLOW)));
                    MasterSDC.commands.put(((Player) sender).getUniqueId(), args);
                    break;
                case "persist":
                    if (MasterSDC.persists.contains(((Player) sender).getUniqueId())) {
                        MasterSDC.persists.remove(((Player) sender).getUniqueId());
                        MasterSDC.commands.remove(((Player) sender).getUniqueId());
                        sender.sendMessage(MasterSDC.prefix.append(Component.text("La persistence est désactivée.").color(NamedTextColor.RED)));
                    } else {
                        MasterSDC.persists.add(((Player) sender).getUniqueId());
                        sender.sendMessage(MasterSDC.prefix.append(Component.text("La persistence est activée, les actions sont donc répétées en boucle.").color(NamedTextColor.GREEN)));
                    }
                    break;
                case "share":
                    if (args.length < 3) {
                        sender.sendMessage(MasterSDC.prefix.append(Component.text("Utilisez /sdc share [add|remove|list] <name>").color(NamedTextColor.RED)));
                        return true;
                    }
                    switch (args[1]) {
                        case "add":
                        case "remove": {
                            if (args.length < 4) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Utilisez /sdc share " + args[1] + " <name> <player>").color(NamedTextColor.RED)));
                                return true;
                            }
                            AlphaChest chest = Database.getAlphaChest(args[2], ((Player) sender).getUniqueId());
                            if (chest == null) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Aucun coffre alpha nommé " + args[2] + " n'existe.").color(NamedTextColor.RED)));
                                return true;
                            }
                            Player player = Bukkit.getPlayer(args[3]);
                            if (player == null) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Le joueur doit être connecté.").color(NamedTextColor.RED)));
                                return true;
                            }
                            if (args[1].equals("add")) {
                                if (Database.addShare(chest.getChest_id(), player.getUniqueId())) {
                                    sender.sendMessage(MasterSDC.prefix.append(Component.text("Joueur " + player.getName() + " ajouté au coffre " + args[2] + ".").color(NamedTextColor.GREEN)));
                                } else {
                                    sender.sendMessage(Component.text("Erreur pendant l'ajout du joueur.").color(NamedTextColor.RED));
                                }
                            } else {
                                if (Database.removeShare(chest.getChest_id(), player.getUniqueId())) {
                                    sender.sendMessage(MasterSDC.prefix.append(Component.text("Joueur " + player.getName() + " retiré du coffre " + args[2] + ".").color(NamedTextColor.GREEN)));
                                } else {
                                    sender.sendMessage(Component.text("Erreur pendant la suppression du joueur.").color(NamedTextColor.RED));
                                }
                            }
                        }
                        break;
                        case "list":
                            AlphaChest chest = Database.getAlphaChest(args[2], ((Player) sender).getUniqueId());
                            if (chest == null) {
                                sender.sendMessage(MasterSDC.prefix.append(Component.text("Aucun coffre alpha nommé " + args[2] + " n'existe.").color(NamedTextColor.RED)));
                                return true;
                            }
                            sender.sendMessage(MasterSDC.prefix.append(Component.text("Liste des membres:").color(NamedTextColor.YELLOW)));
                            sender.sendMessage(Component.text(Database.getShare(chest.getChest_id()).stream().map(UUID::fromString).map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).collect(Collectors.joining(", "))).color(NamedTextColor.YELLOW));
                    }
            }
        } else {
            sender.sendMessage("This command can only be executed by a player.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (!Utils.checkPerm((Player) sender, "deca.sdc.use")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            return Utils.argsFilter(args[0], Arrays.asList("create", "remove", "info", "addFilter", "removeFilter", "share", "persist", "help"));
        } else if (args.length >= 2) {
            if (args.length == 2) {
                if (args[0].equals("create"))
                    return Utils.argsFilter(args[1], Arrays.asList("alpha", "beta"));
                if (args[0].equals("share"))
                    return Utils.argsFilter(args[1], Arrays.asList("add", "remove", "list"));
            } else if (args.length == 3) {
                if ((args[0].equals("create") && args[1].equals("beta")) || args[0].equals("share"))
                    return Utils.argsFilter(args[2], Database.getAlphaChest(((Player) sender).getUniqueId()).stream().map(AlphaChest::getName).collect(Collectors.toList()));
            }
            if (args[0].equals("addFilter") || args[0].equals("removeFilter"))
                return Utils.argsFilter(args[args.length - 1].toUpperCase(), Arrays.stream(Material.values()).map(Enum::toString).collect(Collectors.toList()));
            if (args[0].equals("create") && args[1].equals("beta"))
                return Utils.argsFilter(args[args.length - 1].toUpperCase(), Arrays.stream(Material.values()).map(Enum::toString).collect(Collectors.toList()));
        }
        return new ArrayList<>();
    }
}

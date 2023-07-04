package github.io.jpbtk.Commands;

import github.io.jpbtk.Jankenjoin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Arrays;
import java.sql.*;
import java.util.UUID;

import static org.apache.commons.lang.math.NumberUtils.isNumber;

import github.io.jpbtk.Janken;
import static github.io.jpbtk.Man10Janken.econ;
import static github.io.jpbtk.Man10Janken.plugin;
import static github.io.jpbtk.Man10Janken.prefix;
import static org.bukkit.Bukkit.broadcastMessage;

public class MJA implements CommandExecutor, TabCompleter {

    private String[] completeList = new String[]{"open", "join"};

    public MJA() throws SQLException {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + "§c§lプレイヤーのみ実行可能です。");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(prefix + "§c§l/mja <open/join> <bet>");
            return true;
        } else if (args[0].equals("open")) {
            if(args[1] != null) {
                if (isNumber(args[1])) {
                    if (econ.getBalance(player) >= Integer.parseInt(args[1])) {
                        try {
                            Janken janken = new Janken();
                            janken.janken(player, command, label, args);
                            return true;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        player.sendMessage(prefix + "§c§lお金が足りません。");
                        return true;
                    }
                } else {
                    player.sendMessage(prefix + "§c§l数字を入力してください。");
                    return true;
                }
            } else {
                player.sendMessage(prefix + "§c§l/mja <open/join> <bet>");
                return true;
            }
        }else if (args[0].equals("join")) {
            Jankenjoin jankenjoin = null;
            try {
                jankenjoin = new Jankenjoin();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            jankenjoin.jankenjoin(player, command, label, args);
            return true;
        } else {
            player.sendMessage(prefix + "§c§l/mja <open/join> <bet>");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("mja")) {
            if (args.length == 1) {
                return Arrays.asList(completeList);
            }
        }
        return null;
    }
    Connection con = DriverManager.getConnection(
            plugin.getConfig().getString("db.url"),
            plugin.getConfig().getString("db.user"),
            plugin.getConfig().getString("db.password")
    );
}

package github.io.jpbtk;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

import static github.io.jpbtk.Man10Janken.*;
import static github.io.jpbtk.Man10Janken.plugin;
import static org.bukkit.Bukkit.broadcastMessage;

public class Jankenjoin implements CommandExecutor {
    public Jankenjoin() throws SQLException {
    }
    public boolean jankenjoin(CommandSender sender, Command command, String label, String[] args){
        Player player = (Player) sender;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM jankenplay WHERE p1 = ?");
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                player.sendMessage(prefix + "§c§lじゃんけんを募集している人がいません。");
                return true;
            }else if (rs.getString("p2").equals("null")) {
                if (econ.getBalance(player) >= rs.getInt("bet")) {
                    econ.withdrawPlayer(player, rs.getInt("bet"));
                    PreparedStatement ps2 = con.prepareStatement("UPDATE jankenplay SET p2 = ? WHERE p1 = ?");
                    ps2.setString(1, player.getUniqueId().toString());
                    ps2.setString(2, rs.getString("p1"));
                    ps2.executeUpdate();
                    broadcastMessage(prefix + "§a§l" + player.getName() + "§f§lが§a§lじゃんけんを" + rs.getInt("bet") + "円で参加しました。");
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try {
                            PreparedStatement ps3 = con.prepareStatement("SELECT * FROM jankenplay WHERE p1 = ?");
                            ps3.setString(1, player.getUniqueId().toString());
                            ResultSet rs2 = ps3.executeQuery();
                            if (rs2.next()) {
                                econ.depositPlayer(player, rs2.getInt("bet"));
                                PreparedStatement ps4 = con.prepareStatement("DELETE FROM jankenplay WHERE p1 = ?");
                                ps4.setString(1, player.getUniqueId().toString());
                                ps4.executeUpdate();
                                broadcastMessage(prefix + "§a§l" + player.getName() + "§f§lが§a§lじゃんけんを募集していましたが、時間切れで募集を締め切りました。");
                            }

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }, 1200);
                    return true;
                } else {
                    player.sendMessage(prefix + "§c§lお金が足りません。");
                    return true;
                }
            } else {
                player.sendMessage(prefix + "§c§lじゃんけんを募集している人がいません。");
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    Connection con = DriverManager.getConnection(
            plugin.getConfig().getString("db.url"),
            plugin.getConfig().getString("db.user"),
            plugin.getConfig().getString("db.password")
    );

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}

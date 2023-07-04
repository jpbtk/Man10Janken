package github.io.jpbtk;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

import static github.io.jpbtk.Man10Janken.*;
import static org.bukkit.Bukkit.broadcastMessage;

public class Janken implements CommandExecutor {
    public Janken() throws SQLException {
    }

    public boolean janken(CommandSender sender, Command command, String label, String[] args){
        Player player = (Player) sender;
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO jankenplay (p1, p2, p1hand, p2hand, bet) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, "null");
            ps.setInt(3, 0);
            ps.setInt(4, 0);
            ps.setInt(5, Integer.parseInt(args[1]));
            ps.executeUpdate();
            econ.withdrawPlayer(player, Integer.parseInt(args[1]));
            broadcastMessage(prefix + "§a§l" + player.getName() + "§f§lが§a§lじゃんけんを" + args[1] + "円で募集しています。残り60秒で募集を締め切ります。");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    PreparedStatement ps2 = con.prepareStatement("SELECT * FROM jankenplay WHERE p1 = ?");
                    ps2.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps2.executeQuery();
                    if (rs.next()) {
                        econ.depositPlayer(player, rs.getInt("bet"));
                        PreparedStatement ps3 = con.prepareStatement("DELETE FROM jankenplay WHERE p1 = ?");
                        ps3.setString(1, player.getUniqueId().toString());
                        ps3.executeUpdate();
                        broadcastMessage(prefix + "§a§l" + player.getName() + "§f§lが§a§lじゃんけんを募集していましたが、時間切れで募集を締め切りました。");
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, 1200);
            return true;
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

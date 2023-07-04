package github.io.jpbtk;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import static org.bukkit.Bukkit.broadcastMessage;

import java.sql.*;

import static github.io.jpbtk.Man10Janken.plugin;
import static github.io.jpbtk.Man10Janken.prefix;
import static github.io.jpbtk.Man10Janken.econ;

public class Listeners implements Listener {
    public Listeners() throws SQLException{
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) throws SQLException {
        Player player = event.getPlayer();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM jankenplay WHERE p1 = '" + player.getUniqueId() + "'");
        if (rs.next()) {
            econ.depositPlayer(player, rs.getInt("bet"));
            PreparedStatement ps3 = con.prepareStatement("DELETE FROM jankenplay WHERE p1 = ?");
            ps3.setString(1, player.getUniqueId().toString());
            ps3.executeUpdate();
            broadcastMessage(prefix + "§a§l" + player.getName() + "§f§lが§a§lじゃんけんを募集していましたが、ログアウトしたため募集を締め切りました。");
        }
    }
    Connection con = DriverManager.getConnection(
            plugin.getConfig().getString("db.url"),
            plugin.getConfig().getString("db.user"),
            plugin.getConfig().getString("db.password")
    );
}

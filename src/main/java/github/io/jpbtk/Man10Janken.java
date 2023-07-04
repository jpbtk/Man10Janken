package github.io.jpbtk;

import github.io.jpbtk.Commands.MJA;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public final class Man10Janken extends JavaPlugin {

    public static JavaPlugin plugin;
    private Listeners listeners;
    public static Economy econ = null;
    public static String prefix = "§l[§d§lMa§f§ln§a§l10§2§lJanken§f§l]§f ";

    public Man10Janken() throws SQLException {
    }

    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("Man10Janken has been enabled!");
        try{
            this.listeners = new Listeners();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents((Listener) this.listeners, this);
        try {
            getCommand("mja").setExecutor(new MJA());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        plugin.saveDefaultConfig();
        super.onEnable();
        if(!setupEconomy()){
            getLogger().severe("Vaultが見つかりませんでした。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            PreparedStatement ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS janken (uuid VARCHAR(36), win INT, lose INT, draw INT, pf INT, PRIMARY KEY (uuid))");
            getLogger().info("Table created!");
            ps.executeUpdate();
            PreparedStatement ps2 = con.prepareStatement("CREATE TABLE IF NOT EXISTS jankenplay (p1 VARCHAR(36), p2 VARCHAR(36), p1hand INT, p2hand INT, bet INT, PRIMARY KEY (p1, p2))");
            getLogger().info("Table created!");
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        saveDefaultConfig();
        FileConfiguration config = getConfig();
    }
    @Override
    public void onDisable() {
        getLogger().info("Man10Janken has been disabled!");
        saveDefaultConfig();
        super.onDisable();
    }

    private static Boolean setupEconomy() {
        if(getPlugin().getServer().getPluginManager().getPlugin("Vault") == null){
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
            return false;
        }else{
            econ = rsp.getProvider();
        }
        return econ != null;
    }
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    Connection con = DriverManager.getConnection(
            getConfig().getString("db.url"),
            getConfig().getString("db.user"),
            getConfig().getString("db.password")
    );
}

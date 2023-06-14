package com.example.man10janken;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Man10Janken extends JavaPlugin implements Listener {
    private Map<Player, Integer> betAmounts;
    private Map<Player, Integer> playerBalances;

    @Override
    public void onEnable() {
        betAmounts = new HashMap<>();
        playerBalances = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("mja").setExecutor(new JankenCommand(this));
    }

    private void openJankenInventory(Player player, int betAmount) {
        Inventory inventory = Bukkit.createInventory(null, 9, "CPUと" + betAmount + "円でじゃんけん");

        ItemStack rockItem = createItem(Material.STONE, "グー");
        ItemStack paperItem = createItem(Material.PAPER, "パー");
        ItemStack scissorsItem = createItem(Material.SHEARS, "チョキ");

        inventory.setItem(2, rockItem);
        inventory.setItem(4, paperItem);
        inventory.setItem(6, scissorsItem);

        player.openInventory(inventory);
        betAmounts.put(player, betAmount);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void rotateHand(Player player, ItemStack handItem) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            int handSlot = inventory.first(handItem);

            ItemStack rotatingItem = createItem(Material.CLOCK, "回転中...");
            inventory.setItem(handSlot, rotatingItem);

            Bukkit.getScheduler().runTaskLater(this, () -> {
                JankenResult cpuHand = playJanken();

                ItemStack newHandItem = createItem(getMaterialFromHand(cpuHand), getDisplayNameFromHand(cpuHand));
                inventory.setItem(handSlot, newHandItem);

                int betAmount = betAmounts.get(player);
                int prize = 0;

                JankenResult playerHand = getHandFromItem(handItem);

                if (playerHand == cpuHand) {
                    player.sendMessage("引き分け！ 払い戻し金額はありません。");
                } else if (playerHand == JankenResult.GU && cpuHand == JankenResult.TYOKI ||
                        playerHand == JankenResult.TYOKI && cpuHand == JankenResult.PA ||
                        playerHand == JankenResult.PA && cpuHand == JankenResult.GU) {
                    prize = betAmount * 2;
                    player.sendMessage("勝ち！ " + prize + "円を獲得しました。");
                } else {
                    player.sendMessage("負け！ " + betAmount + "円を失いました。");
                    int playerBalance = playerBalances.getOrDefault(player, 0);
                    playerBalance -= betAmount;
                    playerBalances.put(player, playerBalance);
                }

                if (prize > 0) {
                    int playerBalance = playerBalances.getOrDefault(player, 0);
                    playerBalance += prize;
                    playerBalances.put(player, playerBalance);
                }

                player.closeInventory();
                betAmounts.remove(player);
            }, 40L);  // 2秒後にCPUの手を決定
        }, 20L);  // 1秒後に回転中の表示に切り替え
    }

    private JankenResult getHandFromItem(ItemStack itemStack) {
        if (itemStack.getType() == Material.STONE) {
            return JankenResult.GU;
        } else if (itemStack.getType() == Material.PAPER) {
            return JankenResult.PA;
        } else if (itemStack.getType() == Material.SHEARS) {
            return JankenResult.TYOKI;
        }
        return null;
    }

    private JankenResult playJanken() {
        Random random = new Random();
        int cpuHand = random.nextInt(3);  // 0:グー, 1:パー, 2:チョキ

        if (cpuHand == 0) {
            return JankenResult.GU;
        } else if (cpuHand == 1) {
            return JankenResult.PA;
        } else {
            return JankenResult.TYOKI;
        }
    }

    private Material getMaterialFromHand(JankenResult hand) {
        switch (hand) {
            case GU:
                return Material.STONE;
            case PA:
                return Material.PAPER;
            case TYOKI:
                return Material.SHEARS;
            default:
                return Material.AIR;
        }
    }

    private String getDisplayNameFromHand(JankenResult hand) {
        switch (hand) {
            case GU:
                return "グー";
            case PA:
                return "パー";
            case TYOKI:
                return "チョキ";
            default:
                return "";
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (betAmounts.containsKey(player)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                rotateHand(player, clickedItem);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.STICK) {
            openJankenInventory(player, 100);  // デフォルトの賭け金額を100に設定
        }
    }

    public int getPlayerBalance(Player player) {
        return playerBalances.getOrDefault(player, 0);
    }

    public void setPlayerBalance(Player player, int balance) {
        playerBalances.put(player, balance);
    }
}

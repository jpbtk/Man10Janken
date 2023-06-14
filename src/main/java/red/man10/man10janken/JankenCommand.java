package com.example.man10janken;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JankenCommand implements CommandExecutor {
    private Man10Janken plugin;

    public JankenCommand(Man10Janken plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 2 && args[0].equalsIgnoreCase("cpu")) {
                int betAmount;
                try {
                    betAmount = Integer.parseInt(args[1]);
                    if (betAmount <= 0) {
                        player.sendMessage("金額は1以上の整数で指定してください。");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("金額は整数で指定してください。");
                    return true;
                }

                int playerBalance = plugin.getPlayerBalance(player);
                if (playerBalance < betAmount) {
                    player.sendMessage("賭け金額が所持金を超えています。");
                    return true;
                }

                plugin.openJankenInventory(player, betAmount);
            } else {
                player.sendMessage("使用方法: /mja cpu <賭け金額>");
            }
        } else {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます。");
        }
        return true;
    }
}

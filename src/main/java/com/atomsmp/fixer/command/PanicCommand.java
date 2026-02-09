package com.atomsmp.fixer.command;

import com.atomsmp.fixer.AtomSMPFixer;
import com.atomsmp.fixer.util.BotUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class PanicCommand implements CommandExecutor {

    private final AtomSMPFixer plugin;

    public PanicCommand(AtomSMPFixer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("atomsmpfixer.panic")) {
            sender.sendMessage(ChatColor.RED + "Bunu yapmak için yetkin yok!");
            return true;
        }

        // Config values
        int minMinutes = plugin.getConfigManager().getInt("panik-modu.min-oynama-suresi", 30);
        String logFile = plugin.getConfigManager().getString("panik-modu.log-dosyasi", "panic-bans.log");
        
        // Ticks: Minutes * 60 seconds * 20 ticks
        long minTicks = minMinutes * 60L * 20L;

        sender.sendMessage(ChatColor.RED + "PANİK MODU BAŞLATILIYOR! " + minMinutes + " dakikadan az oynayanlar yasaklanıyor...");

        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("atomsmpfixer.bypass") || p.isOp()) continue;

            int playedTicks = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
            
            if (playedTicks < minTicks) {
                String ip = p.getAddress().getAddress().getHostAddress();
                String name = p.getName();

                // Ban IP
                Bukkit.getBanList(BanList.Type.IP).addBan(
                    ip, 
                    "Panik Modu: Bot şüphesi (" + minMinutes + "dk altı oynama)", 
                    null, 
                    sender.getName()
                );

                // Kick
                p.kickPlayer(ChatColor.RED + "Sunucu güvenlik nedeniyle panik moduna alındı.\nBot olarak işaretlendiniz.");

                // Log
                BotUtils.logPanicBan(plugin, name, ip, logFile);
                
                count++;
            }
        }

        sender.sendMessage(ChatColor.RED + "Panik modu tamamlandı. " + ChatColor.YELLOW + count + ChatColor.RED + " oyuncu yasaklandı.");
        sender.sendMessage(ChatColor.GRAY + "Yasaklananlar '" + logFile + "' dosyasına kaydedildi.");

        return true;
    }
}

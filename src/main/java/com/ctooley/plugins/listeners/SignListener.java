package com.ctooley.plugins.listeners;

import com.ctooley.plugins.SpawnerShop;
import com.ctooley.plugins.util.ShopSign;
import com.ctooley.plugins.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class SignListener implements Listener {

    private final SpawnerShop plugin;
    private final Util util;

    public SignListener(SpawnerShop plugin, Util util) {
        this.plugin = plugin;
        this.util = util;
    }

    @EventHandler
    public void onSignPlacement(SignChangeEvent e) {
        Player p = e.getPlayer();
        if (e.getLine(0).equals(ChatColor.stripColor(util.translateColors(plugin.config.getString("options.sign-title"))))) {
            if ((p.hasPermission("spawnershop.signs.create")) || p.isOp()) {
                String method = e.getLine(1);
                String spawnerType = e.getLine(2);
                if (method.equalsIgnoreCase("Buy")) {
                    String price = e.getLine(3).replace(plugin.config.getString("options.currency-sign"), "");

                    ConfigurationSection spawnerSection = plugin.spawnerFile.getConfig().getConfigurationSection("spawners");
                    if(spawnerSection.contains(spawnerType.toUpperCase())) {
                        e.setLine(0, util.translateColors(plugin.config.getString("options.sign-title")));

                        if (util.isInt(price)) {
                            int price1 = Integer.parseInt(price);
                            e.setLine(3, plugin.config.getString("options.currency-sign") + NumberFormat.getNumberInstance(Locale.forLanguageTag(plugin.config.getString("options.language"))).format(price1));
                        } else {
                            util.sendMessage(p, true, plugin.config.getString("options.invalid-price"));
                            e.setLine(3, plugin.config.getString("options.currency-sign") + NumberFormat.getNumberInstance(Locale.forLanguageTag(plugin.config.getString("options.language"))).format(spawnerSection.getInt(spawnerType.toUpperCase() + ".buy-price")));
                        }
                    } else {
                        sendFormatMessage(e);
                    }
                } else if(method.equalsIgnoreCase("Sell")) {
                    String price = e.getLine(3);
                    ConfigurationSection spawnerSection = plugin.spawnerFile.getConfig().getConfigurationSection("spawners");
                    if(spawnerSection.contains(spawnerType.toUpperCase())) {
                        e.setLine(0, util.translateColors(plugin.config.getString("options.sign-title")));

                        if (util.isInt(price)) {
                            int price1 = Integer.parseInt(price);
                            e.setLine(3, plugin.config.getString("options.currency-sign") + NumberFormat.getNumberInstance(Locale.forLanguageTag(plugin.config.getString("options.language"))).format(price1));
                        } else {
                            util.sendMessage(p, true, plugin.config.getString("options.invalid-price"));
                            e.setLine(3, plugin.config.getString("options.currency-sign") + NumberFormat.getNumberInstance(Locale.forLanguageTag(plugin.config.getString("options.language"))).format(plugin.getConfig().getInt("spawners." + spawnerType + ".sell-price")));
                        }
                    } else {
                        sendFormatMessage(e);
                    }
                } else {
                   sendFormatMessage(e);
                }
            } else {
                util.sendMessage(p, true, plugin.config.getString("options.no-permission"));
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) 
    {
        Player p = e.getPlayer();
        if(e.getClickedBlock() == null)
        {
            return;
        }
        boolean isSign = util.isSign(e.getClickedBlock().getType());
        if ((e.getAction() != Action.RIGHT_CLICK_BLOCK) || !isSign) return;

        ShopSign sign = new ShopSign((Sign) e.getClickedBlock().getState(), plugin);
        if (!sign.isValid()) return;

        if ((!p.hasPermission("spawnershop.signs.use")) && !p.isOp()) 
        {
            util.sendMessage(p, true, plugin.config.getString("options.no-permission"));
            return;
        }

        String spawner = sign.getSpawnerType();
        String method = sign.getMethod();
        int price = sign.getPrice();

        if(!p.hasPermission("spawnershop." + method + "." + spawner.toLowerCase()) && !p.hasPermission("spawnershop." + method + ".all")) 
        {
            util.sendMessage(p, true, plugin.config.getString("options.no-permission"));
            return;
        }
        
        if (method.equalsIgnoreCase("Buy"))
        {
            util.handleSale(p, true, false, spawner, price);
        }
        else 
        {
            if(p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR) 
            {
                if(p.getInventory().getItemInMainHand().getType() == Material.SPAWNER) 
                {
                    if(!p.getInventory().getItemInMainHand().hasItemMeta() || !p.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) return;
                    String name = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
                    if(name.contains(ChatColor.COLOR_CHAR+"")) 
                    {
                        if(ChatColor.stripColor(name).replace(" Spawner","").equalsIgnoreCase(spawner)) 
                        {
                            util.handleSale(p, false, false, spawner, price);
                        }
                    }
                }
            }
        }
    }

    private void sendFormatMessage(SignChangeEvent e) 
    {
        Player p = e.getPlayer();
        p.sendMessage("You have incorrectly formatted a SPAWNERSHOP sign!");
        p.sendMessage("Here is the correct FORMAT!");
        e.setLine(0, "[SpawnerShop]");
        e.setLine(1, "Buy/Sell");
        e.setLine(2, "<MobType>");
        e.setLine(3, "Price");
    }
}
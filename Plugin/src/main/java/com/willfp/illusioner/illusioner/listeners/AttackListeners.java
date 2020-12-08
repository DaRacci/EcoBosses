package com.willfp.illusioner.illusioner.listeners;

import com.willfp.illusioner.illusioner.IllusionerManager;
import com.willfp.illusioner.util.NumberUtils;
import com.willfp.illusioner.util.internal.OptionedSound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackListeners implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onIllusionerAttack(EntityDamageByEntityEvent event) {
        if(!event.getDamager().getType().equals(EntityType.ILLUSIONER))
            return;

        Player temp = null;

        if(event.getEntity() instanceof Player) {
            temp = (Player) event.getEntity();
        } else if(event.getEntity() instanceof Projectile) {
            if(((Projectile) event.getEntity()).getShooter() instanceof Player) {
                temp = (Player) ((Projectile) event.getEntity()).getShooter();
            }
        }

        if(temp == null)
            return;

        Player player = temp;

        OptionedSound hitSound = IllusionerManager.OPTIONS.getGameplayOptions().getHitSound();
        if(hitSound.isBroadcast()) {
            player.getWorld().playSound(event.getEntity().getLocation(), hitSound.getSound(), hitSound.getVolume(), hitSound.getPitch());
        } else {
            player.playSound(event.getEntity().getLocation(), hitSound.getSound(), hitSound.getVolume(), hitSound.getPitch());
        }

        IllusionerManager.OPTIONS.getGameplayOptions().getEffectOptions().forEach(effectOption -> {
            if(NumberUtils.randFloat(0, 100) > effectOption.getChance())
                return;

            player.addPotionEffect(new PotionEffect(effectOption.getEffectType(), effectOption.getDuration(), effectOption.getLevel() - 1));
        });

        if(IllusionerManager.OPTIONS.getGameplayOptions().isShuffle()) {
            if(NumberUtils.randFloat(0, 100) < IllusionerManager.OPTIONS.getGameplayOptions().getShuffleChance()) {
                List<ItemStack> hotbar = new ArrayList<>();
                for(int i = 0; i<9; i++) {
                    hotbar.add(player.getInventory().getItem(i));
                }
                Collections.shuffle(hotbar);
                int i2 = 0;
                for(ItemStack item : hotbar) {
                    player.getInventory().setItem(i2, item);
                    i2++;
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1, 0.5f);
            }
        }

        IllusionerManager.OPTIONS.getGameplayOptions().getSummonerOptions().forEach(summonerOption -> {
            if(NumberUtils.randFloat(0, 100) > summonerOption.getChance())
                return;

            Location loc = player.getLocation().add(NumberUtils.randInt(2,6), 0, NumberUtils.randInt(2,6));
            while(!loc.getBlock().getType().equals(Material.AIR)) {
                loc.add(0, 1, 0);
            }
            player.getWorld().spawnEntity(loc, summonerOption.getType());

            OptionedSound summonSound = IllusionerManager.OPTIONS.getGameplayOptions().getSummonSound();
            if(summonSound.isBroadcast()) {
                player.getWorld().playSound(event.getEntity().getLocation(), summonSound.getSound(), summonSound.getVolume(), summonSound.getPitch());
            } else {
                player.playSound(event.getEntity().getLocation(), summonSound.getSound(), summonSound.getVolume(), summonSound.getPitch());
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onIllusionerDamage(EntityDamageEvent event) {
        if(!event.getEntity().getType().equals(EntityType.ILLUSIONER))
            return;

        if(IllusionerManager.OPTIONS.getGameplayOptions().isIgnoreExplosionDamage()) {
            if(event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION))
                event.setCancelled(true);
        }
    }
}
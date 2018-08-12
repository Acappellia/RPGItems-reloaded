/*
 *  This file is part of RPG Items.
 *
 *  RPG Items is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  RPG Items is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with RPG Items.  If not, see <http://www.gnu.org/licenses/>.
 */
package think.rpgitems.power.impl;

import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import think.rpgitems.I18n;
import think.rpgitems.commands.AcceptedValue;
import think.rpgitems.commands.Getter;
import think.rpgitems.commands.Property;
import think.rpgitems.commands.Setter;
import think.rpgitems.power.PowerRightClick;

import static think.rpgitems.utils.PowerUtils.checkCooldown;
import static think.rpgitems.utils.PowerUtils.getNearbyEntities;

/**
 * Power aoe.
 * <p>
 * On right click the aoe power will apply {@link #type effect}
 * to all entities within the {@link #range range} for {@link #duration duration} ticks
 * at power {@link #amplifier amplifier}.
 * By default, the user will be targeted by the potion
 * as well if not set via {@link #selfapplication selfapplication}.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
public class PowerAOE extends BasePower implements PowerRightClick {

    /**
     * Cooldown time of this power
     */
    @Property(order = 0)
    public long cooldown = 20;
    /**
     * Amplifier of the potion
     */
    @Property(order = 4, required = true)
    public int amplifier = 1;
    /**
     * Duration of the potion
     */
    @Property(order = 3)
    public int duration = 15;
    /**
     * Range of the potion
     */
    @Property(order = 1)
    public int range = 5;
    /**
     * Whether the potion will be apply to the user
     */
    @Property(order = 5)
    public boolean selfapplication = true;
    /**
     * Type of the potion
     */
    @Property(order = 2)
    @Setter("setType")
    @Getter("getType")
    @AcceptedValue({"SPEED",
            "SLOW",
            "FAST_DIGGING",
            "SLOW_DIGGING",
            "INCREASE_DAMAGE",
            "HEAL",
            "HARM",
            "JUMP",
            "CONFUSION",
            "REGENERATION",
            "DAMAGE_RESISTANCE",
            "FIRE_RESISTANCE",
            "WATER_BREATHING",
            "INVISIBILITY",
            "BLINDNESS",
            "NIGHT_VISION",
            "HUNGER",
            "WEAKNESS",
            "POISON",
            "WITHER",
            "HEALTH_BOOST",
            "ABSORPTION",
            "SATURATION",
            "GLOWING",
            "LEVITATION",
            "LUCK",
            "UNLUCK",
            "SLOW_FALLING",
            "CONDUIT_POWER",
            "DOLPHINS_GRACE",
    })
    public PotionEffectType type;
    /**
     * Display text of this power. Will use default text in case of null
     */
    @Property
    public String name = null;
    /**
     * Cost of this power
     */
    @Property
    public int consumption = 0;

    @Override
    public void init(ConfigurationSection s) {
        cooldown = s.getLong("cooldown", 20);
        duration = s.getInt("duration", 60);
        amplifier = s.getInt("amplifier", 1);
        range = s.getInt("range", 5);
        selfapplication = s.getBoolean("selfapplication", true);
        type = PotionEffectType.getByName(s.getString("type", "HARM"));
        name = s.getString("name");
        consumption = s.getInt("consumption", 0);
    }

    @Override
    public void save(ConfigurationSection s) {
        s.set("cooldown", cooldown);
        s.set("range", range);
        s.set("duration", duration);
        s.set("amplifier", amplifier);
        s.set("selfapplication", selfapplication);
        s.set("type", type.getName());
        s.set("name", name);
        s.set("consumption", consumption);
    }

    @Override
    public void rightClick(final Player player, ItemStack stack, Block clicked, PlayerInteractEvent event) {
        if (!checkCooldown(this, player, cooldown, true)) return;
        if (!getItem().consumeDurability(stack, consumption)) return;
        PotionEffect effect = new PotionEffect(type, duration, amplifier - 1);
        if (selfapplication)
            player.addPotionEffect(effect);
        player.getWorld().playEffect(player.getLocation(), Effect.POTION_BREAK, type.getColor().asRGB());
        for (Entity ent : getNearbyEntities(this, player.getLocation(), player, range))
            if (ent instanceof LivingEntity && !player.equals(ent))
                ((LivingEntity) ent).addPotionEffect(effect);
    }

    @Override
    public String getName() {
        return "aoe";
    }

    @Override
    public String displayText() {
        return name != null ? name : I18n.format("power.aoe.display", type.getName(), amplifier, duration, selfapplication ? I18n.format("power.aoe.selfapplication.including") : I18n.format("power.aoe.selfapplication.excluding"), range, (double) cooldown / 20d);
    }

    public void setType(String effect) {
        type = PotionEffectType.getByName(effect);
    }

    public String getType() {
        return type.getName();
    }
}
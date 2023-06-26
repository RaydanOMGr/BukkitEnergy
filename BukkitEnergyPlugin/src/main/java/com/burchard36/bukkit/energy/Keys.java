package com.burchard36.bukkit.energy;

import com.burchard36.bukkit.BukkitEnergyPlugin;
import org.bukkit.NamespacedKey;

/**
 * A static list of {@link org.bukkit.NamespacedKey} used when writing/reading from PDC
 */
public class Keys {
    /* Will always be INTEGER */
    public static final NamespacedKey storedEnergyKey = new NamespacedKey(BukkitEnergyPlugin.getInstance(), "stored_energy");
    /* Will always be INTEGER */
    public static final NamespacedKey maxEnergyKey = new NamespacedKey(BukkitEnergyPlugin.getInstance(), "max_energy");
    /* Will always be a MAP */
    public static final NamespacedKey allowedFaces = new NamespacedKey(BukkitEnergyPlugin.getInstance(), "allowed_faces");

}

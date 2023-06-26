package com.burchard36.bukkit;

import com.burchard36.bukkit.capability.EnergyFactory;
import com.burchard36.bukkit.energy.BukkitEnergy;
import com.burchard36.bukkit.energy.IEnergyStorage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class BukkitEnergyPlugin extends JavaPlugin implements Listener {

    private static BukkitEnergyPlugin INSTANCE;
    private final HashMap<Class<?>, EnergyFactory<?>> registeredEnergyFactorys = new HashMap<>();

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    private void onServerSave(final WorldSaveEvent saveEvent) {
        final World world = saveEvent.getWorld();
    }

    /**
     * Used to get an instance of BukkitEnergy, plugins should be using this.
     * @return instance of this class
     */
    public static BukkitEnergyPlugin getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a {@link EnergyFactory} for easily managing IEnergyStorage blocks
     * @param energyStorage A Class implementing {@link IEnergyStorage} you want to get/register
     * @return the singleton instance of the EnergyFactory
     * @param <T> the generic
     */
    public <T extends IEnergyStorage> EnergyFactory<T> getEnergyFactory(Class<T> energyStorage) {
        this.registeredEnergyFactorys.computeIfAbsent(energyStorage, (v) -> {
            final EnergyFactory<T> energyFactory = new EnergyFactory<>(energyStorage);
            Bukkit.getPluginManager().registerEvents(energyFactory, this);
            return energyFactory;
        });
        return (EnergyFactory<T>) this.registeredEnergyFactorys.get(energyStorage);
    }

    /**
     * Returns the default energy factory, it is highly recommended that plugins simply use this one as it will
     * lead to better compatibility across the board.
     * @return a singleton {@link EnergyFactory} you may use to get & create energy blocks
     */
    public EnergyFactory<BukkitEnergy> getDefaultEnergyFactory() {
        return this.getEnergyFactory(BukkitEnergy.class);
    }
}

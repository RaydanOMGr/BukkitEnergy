package com.burchard36.bukkit.capability;

import com.burchard36.bukkit.energy.EnergyStorageData;
import com.burchard36.bukkit.BukkitEnergyPlugin;
import com.burchard36.bukkit.energy.IEnergyStorage;
import com.burchard36.bukkit.energy.Keys;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

;
public class EnergyFactory<T extends IEnergyStorage> extends SimpleReflection<T> implements Listener {

    private final ConcurrentHashMap<Location, T> energyBlocksRegistered = new ConcurrentHashMap<>();

    public EnergyFactory(Class<T> clazz) {
        super(clazz);
    }

    /**
     * To better make this feel vanilla, we will write the IEnergyStorage -> PDC on every world save
     * This will also save on performance splitting and making out own save
     * (We can still create a custom event for other plugins to listen to if this will be needed)
     *
     * And yes, only the world save will have the IES -> PDC flashed in this event
     * @param saveEvent {@link WorldSaveEvent} provided by Bukkit/Spigot/Paper/Whatever weird fucking fork your using
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public final void onWorldSave(final WorldSaveEvent saveEvent) {
        final World savedWorld = saveEvent.getWorld();
        this.energyBlocksRegistered.forEach((location, energyBlock) -> {
            final World locationWorld = location.getWorld();
            assert locationWorld != null;
            /* UUID Check will be slightly faster than .equals on full World objects */
            if (locationWorld.getUID().equals(savedWorld.getUID())) {
                EnergyStorageData.deserializeTo(energyBlock); // flashes the data from a IEnergyStorage variables to the Block PDC
            }
        });
    }

    /**
     * Checks if the given location is a IEnergyBlock
     * @param location {@link Location} to check
     * @return true if the given Location is a IEnergyStorage block
     */
    public final boolean isEnergyBlock(final Location location) {
        return new CustomBlockData(location.getBlock(), BukkitEnergyPlugin.getInstance()).has(Keys.storedEnergyKey);
    }

    /**
     * Checks if the given Block is a IEnergyBlock
     * @param block {@link Block} to check
     * @return true if the given Location is a IEnergyStorage block
     */
    public final boolean isEnergyBlock(final Block block) {
        return isEnergyBlock(block.getLocation());
    }

    /**
     * Creates a new IEnergyBlock is one does not already exist at the given Block location
     * @param block {@link Block} to use to create the energy block
     * @return an empty optional is the block already is a IEnergyStorage, a {@link IEnergyStorage} of the given {@param block} otherwise
     */
    public final @NonNull Optional<T> createEnergyBlock(final @NonNull Block block) {
        if (this.isEnergyBlock(block)) return Optional.empty();
        //Bukkit.broadcastMessage("createEnergyBlock");
        final T energyImpl = this.newInstance(block);
        EnergyStorageData.deserializeTo(energyImpl);
        return Optional.of(this.energyBlocksRegistered.getOrDefault(block.getLocation(), energyImpl));
    }

    /**
     * Gets a Block as a IEnergyStorage block
     * @param block {@link Block} to get
     * @return an Empty {@link Optional} if the given Block is not a IEnergyStorage
     */
    public final @NonNull Optional<T> getEnergyBlock(final @NonNull Block block) {
        final Location location = block.getLocation();
        if (!this.isEnergyBlock(location)) return Optional.empty();
        this.energyBlocksRegistered.computeIfAbsent(location, (v) -> {
            final T energyImpl = this.newInstance(block);
            final PersistentDataContainer dataContainer = new CustomBlockData(block, BukkitEnergyPlugin.getInstance());
            if (!dataContainer.isEmpty()) {
                energyImpl.deserialize(EnergyStorageData.serializeFrom(dataContainer));
            }

            //Bukkit.broadcastMessage("Computing for absent returning %s".formatted(energyImpl.toString()));
            return energyImpl;
        });

        return Optional.of(this.energyBlocksRegistered.get(location));
    }
}


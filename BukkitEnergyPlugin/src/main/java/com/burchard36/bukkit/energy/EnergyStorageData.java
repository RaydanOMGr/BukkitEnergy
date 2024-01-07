package com.burchard36.bukkit.energy;

import com.burchard36.bukkit.BukkitEnergyPlugin;
import com.burchard36.bukkit.enums.IOType;
import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyStorageData {

    protected int storedEnergy = 0;
    protected int maxEnergy = Integer.MAX_VALUE;
    protected final ConcurrentHashMap<BlockFace, IOType> allowedFaces = new ConcurrentHashMap<>();
    protected EnergyStorageData(final int storedEnergy, final int maxEnergy,
                                final ConcurrentHashMap<BlockFace, IOType> allowedFaces) {
        this.storedEnergy = storedEnergy;
        this.maxEnergy = maxEnergy;
        this.allowedFaces.putAll(allowedFaces);
    }

    /**
     * Serialized a {@link EnergyStorageData} from a {@link PersistentDataContainer}, typically from a block using JeffLib
     * @param dataContainer a {@link PersistentDataContainer} that gets provided
     * @return a {@link EnergyStorageData} for use  in {@link BukkitEnergy}
     */
    public static EnergyStorageData serializeFrom(final PersistentDataContainer dataContainer) {
        final int storedEnergy = dataContainer.getOrDefault(Keys.storedEnergyKey, PersistentDataType.INTEGER, 0);
        final int maxEnergy = dataContainer.getOrDefault(Keys.maxEnergyKey, PersistentDataType.INTEGER, Integer.MAX_VALUE);
        final HashMap<String, String> allowedFacesSerialized = dataContainer.getOrDefault(Keys.allowedFaces, DataType.asHashMap(PersistentDataType.STRING, PersistentDataType.STRING), new HashMap<>());
        final ConcurrentHashMap<BlockFace, IOType> allowedFaces = new ConcurrentHashMap<>();
        allowedFacesSerialized.forEach((blockFaceString, ioTypeString) -> {
            final BlockFace blockFace = BlockFace.valueOf(blockFaceString);
            final IOType ioType = IOType.valueOf(ioTypeString);
            allowedFaces.put(blockFace, ioType);
        });
        return new EnergyStorageData(storedEnergy, maxEnergy, allowedFaces);
    }

    /**
     * Flashes all the data from a {@link IEnergyStorage} to the {@link PersistentDataContainer} of the {@link Block}
     * @param energyImpl an implementation of {@link IEnergyStorage} (
     * @return the {@link PersistentDataContainer} modified
     * @param <T> the implementation generic
     */
    public static <T extends IEnergyStorage> PersistentDataContainer deserializeTo(final @NonNull T energyImpl) {
        final PersistentDataContainer dataContainer = new CustomBlockData(energyImpl.getBlock(), BukkitEnergyPlugin.getInstance());
        final EnergyStorageData storageData = energyImpl.serialize();

        dataContainer.set(Keys.storedEnergyKey, PersistentDataType.INTEGER, storageData.storedEnergy);
        dataContainer.set(Keys.maxEnergyKey, PersistentDataType.INTEGER, storageData.maxEnergy);
        final HashMap<String, String> allowedFacesSerialized = new HashMap<>();
        storageData.allowedFaces.forEach(((blockFace, ioType) -> allowedFacesSerialized.put(blockFace.name(), ioType.name())));
        dataContainer.set(Keys.allowedFaces, DataType.asHashMap(PersistentDataType.STRING, PersistentDataType.STRING), allowedFacesSerialized);

        return dataContainer;
    }

    /**
     * Basic builder class to initialize {@link EnergyStorageData}
     */
    public static class Builder {
        final NamespacedKey storedEnergyKey = new NamespacedKey(BukkitEnergyPlugin.getInstance(), "stored_energy");

        private int maxStorageAmount = Integer.MAX_VALUE;
        private int storedEnergy = 0;
        private ConcurrentHashMap<BlockFace, IOType> allowedFaces;

        public Builder(int storedEnergy) {
            this.storedEnergy = storedEnergy;
        }

        public Builder() {

        }

        /**
         * Sets the stored energy of this storage builder for serialization/deserialization
         * @param energyStored the amount of energy this stored container is holding
         * @return instance of this Builder
         */
        public final Builder setEnergyStored(int energyStored) {
            this.storedEnergy = energyStored;
            return this;
        }

        public final Builder setMaxEnergyStorage(int maxStorageAmount) {
            this.maxStorageAmount = maxStorageAmount;
            return this;
        }

        public final Builder setFaceIOTypes(ConcurrentHashMap<BlockFace, IOType> allowedFaces) {
            this.allowedFaces = allowedFaces;
            return this;
        }

        public final Builder setFaceIOType(BlockFace face, IOType type) {
            this.allowedFaces.put(face, type);
            return this;
        }

        /**
         * Builds an instance of {@link EnergyStorageData} using data provided from this Builder
         * @return a new instance of {@link EnergyStorageData}
         */
        public final EnergyStorageData build() {
            return new EnergyStorageData(this.storedEnergy, this.maxStorageAmount, this.allowedFaces);
        }
    }
}

package com.burchard36.bukkit.energy;

import com.burchard36.bukkit.enums.IOType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An example & Standard implementation of {@link IEnergyStorage}
 *
 * Most plugins should use this as a standard energy
 */
public class BukkitEnergy implements IEnergyStorage {
    protected final Block block;
    protected final AtomicInteger storedEnergy = new AtomicInteger(0);
    protected final AtomicInteger maxStoredEnergy = new AtomicInteger(Integer.MAX_VALUE);
    protected final ConcurrentHashMap<BlockFace, IOType> allowedFaces = new ConcurrentHashMap<>();


    public BukkitEnergy(final @NonNull Block block) {
        this.block = block;
        List.of(BlockFace.values()).forEach((face) -> this.allowedFaces.put(face, IOType.BOTH));
    }

    public BukkitEnergy(final @NonNull Location location) {
        this.block = location.getBlock();
        List.of(BlockFace.values()).forEach((face) -> this.allowedFaces.put(face, IOType.BOTH));
    }

    @Override
    public final void deserialize(final @NonNull EnergyStorageData storageObject) {
        this.storedEnergy.set(storageObject.storedEnergy);
        this.maxStoredEnergy.set(storageObject.maxEnergy);
        this.allowedFaces.putAll(storageObject.allowedFaces);
    }

    @Override
    public final @NonNull EnergyStorageData serialize() {
        return new EnergyStorageData.Builder(this.getStoredEnergy())
                .setMaxEnergyStorage(this.getMaxEnergyStorage())
                .setFaceIOTypes(this.allowedFaces)
                .build();
    }

    @Override
    public final int getStoredEnergy() {
        return this.storedEnergy.get();
    }

    @Override
    public final int getMaxEnergyStorage() {
        return this.maxStoredEnergy.get();
    }

    @Override
    public void setMaxEnergyStored(int amount) {
        this.maxStoredEnergy.set(amount);
    }

    @Override
    public final void generateEnergy(int amount) {
        int newEnergy = this.storedEnergy.get() + amount;
        if (newEnergy > this.getMaxEnergyStorage()) newEnergy = this.getMaxEnergyStorage();
        this.storedEnergy.set(newEnergy);
    }

    @Override
    public final boolean burnEnergy(int amount) {
        int newEnergy = this.storedEnergy.get() - amount;
        if (newEnergy < 0) {
            newEnergy = 0;
            this.storedEnergy.set(0);
            return false;
        }
        this.storedEnergy.set(newEnergy);
        return true;
    }

    @Override
    public final boolean canExtract(BlockFace face) {
        final IOType type = this.allowedFaces.get(face);
        return type == IOType.BOTH || type == IOType.OUTPUT;
    }

    @Override
    public final void toggleFaceIOType(BlockFace face, IOType ioType) {
        this.allowedFaces.put(face, ioType);
    }

    @Override
    public final boolean canReceive(BlockFace face) {
        final IOType type = this.allowedFaces.get(face);
        return type == IOType.BOTH || type == IOType.INPUT;
    }

    @Override
    public final int receiveEnergy(final @NonNull BlockFace face, int amount, boolean simulate) {
        if (!this.canReceive(face)) {
            return 0;
        }

        int energyReceived = amount + this.getStoredEnergy();
        if (energyReceived > this.getMaxEnergyStorage()) energyReceived = this.getMaxEnergyStorage();
        if (simulate) return energyReceived;
        this.storedEnergy.set(energyReceived);
        return this.storedEnergy.get();
    }

    @Override
    public final int extractEnergy(final @NonNull BlockFace face, final int amount, final boolean simulate) {
        if (!this.canExtract(face)) return 0;

        int newEnergyBalance = this.getStoredEnergy() - amount;
        if (newEnergyBalance < 0) {
            final int before = amount - this.getStoredEnergy();
            this.storedEnergy.set(0);
            if (before == amount) return 0; // Container is empty
            return before; // there was actually some power that was moved
        }
        if (simulate) return 0;
        this.storedEnergy.set(newEnergyBalance);
        return amount;
    }

    @Override
    public final @NonNull Block getBlock() {
        return this.block;
    }
}

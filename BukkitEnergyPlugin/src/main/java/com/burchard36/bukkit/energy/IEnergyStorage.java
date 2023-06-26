package com.burchard36.bukkit.energy;

import com.burchard36.bukkit.enums.IOType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public interface IEnergyStorage {

    /**
     * Used for loading the stored energy of the Block from PDC
     * @param storageObject the data of this block from PDC check {@link EnergyStorageData}
     */
    void deserialize(final EnergyStorageData storageObject);

    /**
     * Method used when the block data gets saved into PDC
     * @return the storage data of this block, see {@link EnergyStorageData.Builder}
     */
    EnergyStorageData serialize();

    /**
     * Returns the stored energy of this EnergyStorage
     * @return how much energy is stored
     * <br>
     * @since 1.0.0
     * @author Burchard36
     */
    int getStoredEnergy();

    /**
     * Returns the MAX amount of storage this EnergyStorage may have
     * @return how much energy this storage may hold
     * <br>
     * @since 1.0.0
     * @author Burchard36
     */
    int getMaxEnergyStorage();

    /**
     * Sets the MAX amount of storage this EnergyStorage may have
     * @param amount the new max storage amount
     * <br>
     * @since 1.0.0
     * @author Burchard36
     */
    void setMaxEnergyStored(final int amount);

    /**
     * Adds power to this block via "generations" means, typically use for generator blocks
     * @param amount Amount to add
     * @return the amount of energy in storage
     */
    void generateEnergy(final int amount);

    /**
     * Removes power to this block via "Spending" means, typically when a block is using power
     * @param amount amount fot remove
     * @return true if the storage container had enough power to burn
     */
    boolean burnEnergy(final int amount);

    /**
     * Returns true if this storage may accept energy on this {@link BlockFace}
     * @param face the {@link BlockFace} that energy will be extracted on
     * @return true if energy may be extracted on the BlockFace
     * <br>
     * @since 1.0.0
     * @author Burchard36
     */
    boolean canExtract(BlockFace face);

    /**
     * Marks a face of this block to allow/disallow input/output
     * @param face {@link BlockFace} to toggle
     * @param ioType {@link IOType} to set, only INPUT, OUTPUT, DISABLED, BOTH are available
     * <br>
     * @since 1.0.0
     * @author Burchard36
     */
    void toggleFaceIOType(final BlockFace face, final IOType ioType);

    /**
     * Returns true if this energy may be inputted on a {@link BlockFace}
     * @param face the {@link BlockFace} that energy will be received on
     * @return true if energy may be inputted on this {@link BlockFace}
     * <br>
     * @since 1.0.0
     * @author Burchard36
     */
    boolean canReceive(final BlockFace face);

    /**
     * Adds energy to this storage container and returns the amount added
     * @param face the {@link BlockFace} that is receiving energy
     * @param amount The amount of energy to add
     * @param simulate If true, the energy added will be simulated, meaning this method returns 0
     * @return the amount of energy that was added to the storage, returns 0 is {@param simulate} is true
     * <br>
     * @since 1.0.0
     * @author Burchard36
     */
    int receiveEnergy(final BlockFace face, int amount, boolean simulate);

    /**
     * Extracts energy from this storage container and returns the amount removed
     * @param face the {@link BlockFace} that is outputting energy
     * @param amount The amount of energy to extract
     * @param simulate If true, the energy added will be simulated, meaning this method returns 0
     * @return the amount of energy that was removed from the storage, returns 0 if {@param simulate} is true
     */
    int extractEnergy(final BlockFace face, int amount, boolean simulate);

    /**
     * Returns the {@link Block} associated with this implementation
     * @return a {@link Block}.... Do I really have to explain this one
     */
    Block getBlock();

}

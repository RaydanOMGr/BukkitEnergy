package com.burchard36.example;

import com.burchard36.bukkit.BukkitEnergyPlugin;
import com.burchard36.bukkit.capability.EnergyFactory;
import com.burchard36.bukkit.energy.BukkitEnergy;
import com.burchard36.bukkit.enums.IOType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Example extends JavaPlugin implements Listener {

    private EnergyFactory<BukkitEnergy> energyFactory;
    private final List<Location> placedLamps = new ArrayList<>();
    private final List<Location> placedFurnaces = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.energyFactory = BukkitEnergyPlugin.getInstance().getDefaultEnergyFactory();
        Bukkit.getPluginManager().registerEvents(this, this);

        /* Tasks that ticks every tick that will handle transfering energy from furnaces -> redstone lamps
         * As well as spending any energy said lamps have
         */
        new BukkitRunnable() {
            @Override
            public void run() {
                placedFurnaces.forEach((furnaceLocation) -> {
                    final Block furnaceBlock = furnaceLocation.getBlock();
                    final Optional<BukkitEnergy> energyBlockOptional = energyFactory.getEnergyBlock(furnaceBlock);
                    energyBlockOptional.ifPresent((energyBlock) -> {
                        final Furnace furnace = (Furnace) furnaceLocation.getBlock().getState();
                        if (furnace.getBurnTime() > 0) { // for this example, an always furnace furnace generates 100 energy a tick
                            energyBlock.generateEnergy(100);
                        }

                        // Only send power if there is lamps above
                        if (furnaceBlock.getRelative(BlockFace.UP).getType() == Material.REDSTONE_LAMP) {
                            int amountExtracted = energyBlock.extractEnergy(BlockFace.UP, 100, false);
                            powerVerticleLamps(furnaceBlock, amountExtracted);
                        }
                    });
                });

                placedLamps.forEach((lampLocation) -> {
                    final Block lampBlock = lampLocation.getBlock();
                    final Optional<BukkitEnergy> energyBlockOptional = energyFactory.getEnergyBlock(lampBlock);
                    energyBlockOptional.ifPresent((energyBlock) -> {
                        final Lightable powerable = (Lightable) lampBlock.getBlockData();
                        if (energyBlock.burnEnergy(25)) { // lamps cost 5 energy a tick
                            powerable.setLit(true);

                        } else powerable.setLit(false);

                        lampBlock.setBlockData(powerable);
                    });
                });
            }
        }.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onBreak(final BlockBreakEvent breakEvent) {
        final Location breakLocation = breakEvent.getBlock().getLocation();
        final Material brokenType = breakEvent.getBlock().getType();
        switch (brokenType) {
            case REDSTONE_LAMP -> placedLamps.remove(breakLocation);
            case FURNACE -> placedFurnaces.remove(breakLocation);
        }
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent placeEvent) {
        final Block placedBlock = placeEvent.getBlock();
        this.handleFurnacePlace(placedBlock);
        this.handleBatteryPlace(placedBlock);
    }

    @EventHandler
    public void checkEnergyStorage(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack inHand = player.getInventory().getItemInMainHand();
        final Block interactedBlock = event.getClickedBlock();
        if (inHand.getType() != Material.STICK) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        if (interactedBlock == null) return;
        if (!this.energyFactory.isEnergyBlock(interactedBlock)) return;

        final BukkitEnergy energyStorage = this.energyFactory.getEnergyBlock(interactedBlock).get();
        Bukkit.broadcastMessage("This container has %s energy stored".formatted(energyStorage.getStoredEnergy()));
        event.setCancelled(true);
    }

    public void powerVerticleLamps(final Block start, int passthroughEnergy) {
        Block upAdjacent = start.getRelative(BlockFace.UP);
        if (upAdjacent.getType() != Material.REDSTONE_LAMP) {
            return;
        }
        final Optional<BukkitEnergy> blockEnergyOptional = this.energyFactory.getEnergyBlock(upAdjacent);
        blockEnergyOptional.ifPresent((blockEnergy) -> {
            blockEnergy.receiveEnergy(BlockFace.DOWN, passthroughEnergy, false);
            if (upAdjacent.getRelative(BlockFace.UP).getType() == Material.REDSTONE_LAMP) {
                // Lamps can only output 50 power
                int amountExtracted = blockEnergy.extractEnergy(BlockFace.UP, 50, false);
                this.powerVerticleLamps(upAdjacent, amountExtracted);
            }
        });

    }

    /**
     * Example for placing a "Generator" block
     * @param placedBlock {@link Block} from a {@link BlockPlaceEvent}
     */
    private void handleFurnacePlace(final Block placedBlock) {
        if (placedBlock.getType() != Material.FURNACE) return;

        final Optional<BukkitEnergy> energyBlockOptional = this.energyFactory.createEnergyBlock(placedBlock); // register newly placed furnace as a generator
        energyBlockOptional.ifPresent((bukkitEnergy) -> {
            Bukkit.broadcastMessage("Was present! Setting settings");
            List.of(BlockFace.values()).forEach((face) -> bukkitEnergy.toggleFaceIOType(face, IOType.DISABLED));
            bukkitEnergy.toggleFaceIOType(BlockFace.UP, IOType.OUTPUT); // set the top of the furnace as an output
            bukkitEnergy.setMaxEnergyStored(25000);
        });

        this.placedFurnaces.add(placedBlock.getLocation());
    }

    /**
     * Example for a basic battery that lights up when power is received on any side,
     * this battery will also work as a pass-through, meaning adjacent batteries placed to it will receive & be powered
     * @param placedBlock {@link Block} from a {@link BlockPlaceEvent}
     */
    private void handleBatteryPlace(final Block placedBlock) {
        if (placedBlock.getType() != Material.REDSTONE_LAMP) return;

        final Optional<BukkitEnergy> energyBlockOptional = this.energyFactory.createEnergyBlock(placedBlock);

        this.placedLamps.add(placedBlock.getLocation());
        // by default all energy blocks i/o from all sides
    }
}

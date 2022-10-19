/*
BoatFly plugin by Mitchell (Creaous) of RED7 STUDIOS
 */

package com.redsevenstudios.boatfly;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.korbsti.soaromaac.Main;
import me.korbsti.soaromaac.api.SoaromaAPI;
import net.minecraft.util.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class BoatFly extends JavaPlugin {

    // Create an instance of ourselves.
    public static BoatFly plugin;

    // Hook into the protocol manager.
    public static ProtocolManager protocolManager;

    // Hook into the SoaromaAPI.
    public SoaromaAPI api;

    @Override
    public void onEnable() {
        // Tell the console that the plugin is enabled.
        System.out.println("BoatFly enabled");
        // Give the console a notice about getting kicked for flying.
        System.out.println("If you get kicked for flying, turn on allow-flight in the server.properties.");

        // If the SoaromaSAC plugins exists.
        if(Bukkit.getPluginManager().getPlugin("SoaromaSAC") != null)
        {
            // If the SoaromaSAC plugin is enabled.
            if(Bukkit.getPluginManager().getPlugin("SoaromaSAC").isEnabled()) {
                // Set the API to use the plugin.
                api = new SoaromaAPI((Main) (Bukkit.getPluginManager().getPlugin("SoaromaSAC")));
            }
        }

        // Set plugin to this.
        plugin = this;

        // Get the protocol manager and set it to the variable.
        protocolManager = ProtocolLibrary.getProtocolManager();

        // Add a packet listener.
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST, PacketType.Play.Client.STEER_VEHICLE) {
            // When we are receiving a packet.
            @Override
            public void onPacketReceiving(PacketEvent event) {
                // Get the packet and create a variable.
                PacketContainer packet = event.getPacket();
                // Get the player from the event.
                Player player = event.getPlayer();

                // Get the boat vehicle.
                Entity b = player.getVehicle();
                // If the boat doesn't exist, stop.
                if (b == null) { return; }
                if (b instanceof Player) { return; }
                // If the boat is well a boat.
                if (b.getType() == EntityType.BOAT) {
                    // If the player has the permission to use boat fly.
                    if (player.hasPermission("boatfly.use")) {
                        // Credit to the Wurst hacked client for part of this section.
                        // Original code here: https://github.com/Wurst-Imperium/Wurst7/blob/master/src/main/java/net/wurstclient/hacks/BoatFlyHack.java

                        // Get the current velocity of the boat.
                        Vector velocity = b.getVelocity();

                        // Default motion
                        double motionX = velocity.getX();
                        double motionY = 0;
                        double motionZ = velocity.getZ();

                        // Set the Y axis to read from the packet.
                        motionY = packet.getBooleans().read(0) ? 0.3 : 0;

                        // If the player is sprinting.
                        if (event.getPlayer().isSprinting())
                            // Set the Y axis to the velocity.
                            motionY = velocity.getY();

                        // If there is AIR under the player.
                        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
                        {
                            // If the X velocity of the player isn't 0.
                            if (player.getVelocity().getX() != 0)
                            {
                                // Get the speed of the player and multiply by 10.
                                double speed = event.getPlayer().getWalkSpeed() * 10;
                                // Get the yaw of the player and use some random MathHelper function.
                                float yawRad = b.getLocation().getYaw() * MathHelper.d;

                                // A note for the math helper function.
                                // I actually don't know what it does but Wurst used RADIANS_PER_DEGREE which isn't in here.
                                // I randomly went through functions and this one seems to work.

                                // Use a sine function to calculate the negative of the yaw and multiply the speed on it.
                                motionX = Math.sin(-yawRad) * speed;
                                // Use a sine function to calculate the positive of the yaw and multiply the speed on it.
                                motionZ = Math.cos(yawRad) * speed;
                            }
                        }

                        // If the SoaromaSAC api is hooked.
                        if (api != null)
                        {
                            // Disable SoaromaSAC for 100 ticks.
                            // ONLY for this player.
                            api.setDisabler(event.getPlayer(), 100);
                        }

                        // Set the velocity of the boat to the values.
                        b.setVelocity(new Vector(motionX, motionY, motionZ));
                    }
                }
            }
        });
    }

    @Override
    public void onDisable() {
        // Tell console that the plugin is disabled.
        System.out.println("BoatFly disabled");
        // Set the plugin to null.
        plugin = null;
    }
}
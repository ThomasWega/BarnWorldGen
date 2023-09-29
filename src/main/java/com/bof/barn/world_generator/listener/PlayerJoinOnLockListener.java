package com.bof.barn.world_generator.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Cancel the player join when the world generation is happening
 */
public class PlayerJoinOnLockListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("TO ADD - The server loading..."));
    }
}

package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.service.portals.EntityPortalResult;
import com.bgsoftware.superiorskyblock.api.service.portals.PortalsManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalsListener implements Listener {

    private final LazyReference<PortalsManagerService> portalsManager = new LazyReference<PortalsManagerService>() {
        @Override
        protected PortalsManagerService create() {
            return plugin.getServices().getService(PortalsManagerService.class);
        }
    };

    private final SuperiorSkyblockPlugin plugin;

    public PortalsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        // TODO - do we really need this? Why not use EntityPortalEnterEvent below?

        PortalType portalType = (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) ? PortalType.NETHER : PortalType.ENDER;

        EntityPortalResult portalResult = this.portalsManager.get().handlePlayerPortal(superiorPlayer, e.getFrom(),
                portalType, e.getTo(), true);

        switch (portalResult) {
            case DESTINATION_WORLD_DISABLED:
            case PORTAL_NOT_IN_ISLAND:
                return;
            case PLAYER_IMMUNED_TO_PORTAL:
            case SCHEMATIC_GENERATING_COOLDOWN:
            case DESTINATION_NOT_ISLAND_WORLD:
            case PORTAL_EVENT_CANCELLED:
            case INVALID_SCHEMATIC:
            case WORLD_NOT_UNLOCKED:
            case DESTINATION_ISLAND_NOT_PERMITTED:
            case SUCCEED:
                e.setCancelled(true);
                return;
            default:
                throw new IllegalStateException("No handling for result: " + portalResult);
        }
    }
}

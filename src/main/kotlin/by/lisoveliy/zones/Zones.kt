package by.lisoveliy.zones

import by.lisoveliy.zones.services.ZoneManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.world.WorldTickCallback
import net.minecraft.world.level.Level
import net.minecraft.world.level.WorldGenLevel
import org.slf4j.LoggerFactory

class Zones : ModInitializer {
    private val zoneManager = ZoneManager()
    private val logger = LoggerFactory.getLogger("zones")
    override fun onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register{ world: Level ->
            zoneManager.tick(world)
        }
    }
}

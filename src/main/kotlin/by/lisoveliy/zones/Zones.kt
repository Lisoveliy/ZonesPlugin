package by.lisoveliy.zones

import by.lisoveliy.zones.models.jsonSerializable.Config
import by.lisoveliy.zones.services.StorageManager
import by.lisoveliy.zones.services.ZoneManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.level.Level
import org.slf4j.LoggerFactory
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files

class Zones : ModInitializer {
    private var zoneManager: ZoneManager? = null
    private var storageManager: StorageManager? = null
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun onInitialize() {
        initConfig()
        initEvents()
        logger.info("Zones ready!")
    }

    private fun initConfig(){
        logger.info("Loading config...")
        val dir = FabricLoader.getInstance().configDir
        val file = dir.resolve("zones.json").toFile()
        file.parentFile.mkdirs()
        var writeDefault = true
        storageManager = StorageManager(file)
        try {
            Files.createFile(file.toPath())
        }catch (_: FileAlreadyExistsException){
            writeDefault = false
        }
        if(writeDefault){
            storageManager!!.config = Config.defaultConfig
            storageManager!!.write()
        }
        storageManager!!.read()
        zoneManager = ZoneManager(storageManager!!.config!!.zones)
    }

    private fun initEvents(){
        logger.info("Loading events...")
        ServerTickEvents.END_WORLD_TICK.register{ world: Level ->
            zoneManager!!.tick(world)
        }
    }
}

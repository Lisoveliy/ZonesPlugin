package by.lisoveliy.zones.services

import by.lisoveliy.zones.extensions.PositionExtensions.getIntegerVector
import by.lisoveliy.zones.models.Zone
import net.minecraft.core.Holder
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.BoundingBox
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.HashMap

class ZoneManager {
    private val logger = LoggerFactory.getLogger("zone.zoneManager")
    private val zones: MutableList<Zone> = mutableListOf()
    private val playerZones: HashMap<UUID, MutableList<Zone>> = java.util.HashMap()
    private var blockTick = false

    init {
        val box = BoundingBox(-3, -3, -3, 3, 3, 3)
        zones.add(Zone(box, "Деревня"))
    }

    private fun getZonesFromPosition(playerPos: Vec3i): List<Zone> {
        val playerZones = mutableListOf<Zone>()
        zones.forEach { zone ->
            if (zone.position.isInside(playerPos))
                playerZones.add(zone)
        }
        return playerZones
    }

    fun tick(world: Level) {
        if (blockTick || world.isClientSide)
            return
        blockTick = true

        val players = world.players()
        playerZones.forEach { existingPlayer ->
            if (!players.any { x -> x.uuid == existingPlayer.key } && players.isNotEmpty()) {
                playerZones.remove(existingPlayer.key)
            }
        }
        players.forEach { player ->
            if (playerZones[player.uuid] == null)
                playerZones[player.uuid] = mutableListOf()

            val enteredZones = mutableListOf<Zone>()
            val exitedZones = mutableListOf<Zone>()
            val zones = this.getZonesFromPosition(
                player.position()
                    .getIntegerVector()
            )
            logger.info("entered zones: {}", zones)
            logger.info("current player zones: {}", playerZones[player.uuid])

            zones.forEach { zone ->
                if (!playerZones[player.uuid]!!.contains(zone))
                    enteredZones.add(zone)
            }
            playerZones[player.uuid]!!.forEach { playerZone ->
                if (!zones.contains(playerZone))
                    exitedZones.add(playerZone)
            }

            updateZones(enteredZones, exitedZones, player)
        }
        blockTick = false
    }

    private fun updateZones(enteredZones: List<Zone>, exitedZones: List<Zone>, player: Player) {
        enteredZones.forEach { zone: Zone ->
            playerZones[player.uuid]!!.add(zone)
            (player as ServerPlayer).connection.send(
                ClientboundSoundPacket(
                    Holder.direct(SoundEvents.EXPERIENCE_ORB_PICKUP),
                    SoundSource.WEATHER,
                    player.position().x,
                    player.position().y,
                    player.position().z,
                    1f,
                    1f,
                    1
                )
            )
            player.connection.send(ClientboundSetTitleTextPacket(Component.literal("§a${zone.name}")))
            player.connection.send(ClientboundSetSubtitleTextPacket(Component.literal("§6Вы в зоне")))
        }
        exitedZones.forEach { zone: Zone ->
            playerZones[player.uuid]!!.remove(zone)
            (player as ServerPlayer).connection.send(
                ClientboundSoundPacket(
                    Holder.direct(SoundEvents.EXPERIENCE_ORB_PICKUP),
                    SoundSource.WEATHER,
                    player.position().x,
                    player.position().y,
                    player.position().z,
                    1f,
                    1f,
                    1
                )
            )
            player.playSound(SoundEvents.EXPERIENCE_BOTTLE_THROW, 1f, 1f)
            player.connection.send(ClientboundSetTitleTextPacket(Component.literal("§5${zone.name}")))
            player.connection.send(ClientboundSetSubtitleTextPacket(Component.literal("§4Вы покинули зону")))
        }
    }
}
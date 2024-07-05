package by.lisoveliy.zones.services

import by.lisoveliy.zones.extensions.PositionExtensions.getIntegerVector
import by.lisoveliy.zones.models.Zone
import net.minecraft.core.Holder
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import java.util.*
import kotlin.collections.HashMap

class ZoneManager
    (zones: List<Zone>) {
//    private val logger = LoggerFactory.getLogger(this::class.java)
    private val zones: MutableList<Zone> = mutableListOf()
    private val playerZones: HashMap<UUID, MutableList<Zone>> = java.util.HashMap()
    private var blockTick = false

    init {
        this.zones.addAll(zones)
    }

    fun tick(world: Level) {
        //Prevent concurrent and unstable behavior
        if (blockTick || world.isClientSide)
            return
        //lock mutex
        blockTick = true
        //dimension players
        val players = world.players()

        //remove player zones on disconnected players
        playerZones.forEach { existingPlayer ->
            if (!players.any { x -> x.uuid == existingPlayer.key } && players.isNotEmpty()) {
                playerZones.remove(existingPlayer.key)
            }
        }

        //update players zones
        players.forEach { player ->
            //add player zones for connected players
            if (playerZones[player.uuid] == null)
                playerZones[player.uuid] = mutableListOf()

            //calculate zone changes for tick
            val enteredZones = mutableListOf<Zone>()
            val exitedZones = mutableListOf<Zone>()
            val zones = this.getZonesFromPosition(
                player.position()
                    .getIntegerVector(), world.dimensionTypeId().location().path
            )
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

    private fun getZonesFromPosition(playerPos: Vec3i, dimensionId: String): List<Zone> {
        val playerZones = mutableListOf<Zone>()
        zones.forEach { zone ->
            if (zone.position.toMinecraft().isInside(playerPos) && zone.dimensionId == dimensionId)
                playerZones.add(zone)
        }
        return playerZones
    }

    private fun updateZones(enteredZones: List<Zone>, exitedZones: List<Zone>, player: Player) {
        enteredZones.forEach { zone: Zone ->
            playerZones[player.uuid]!!.add(zone)
            (player as ServerPlayer)
            player.connection.send(ClientboundSetTitleTextPacket(Component.literal("Зона §6\"${zone.name}\"")))
            player.connection.send(ClientboundSetSubtitleTextPacket(Component.literal("§aДобро пожаловать!")))
            player.connection.send(
                ClientboundSoundPacket(
                    Holder.direct(SoundEvents.PLAYER_LEVELUP),
                    SoundSource.NEUTRAL,
                    player.position().x,
                    player.position().y,
                    player.position().z,
                    1f,
                    1f,
                    0
                )
            )
        }
        exitedZones.forEach { zone: Zone ->
            playerZones[player.uuid]!!.remove(zone)
            (player as ServerPlayer).connection.send(
                ClientboundSoundPacket(
                    Holder.direct(SoundEvents.EXPERIENCE_ORB_PICKUP),
                    SoundSource.NEUTRAL,
                    player.position().x,
                    player.position().y,
                    player.position().z,
                    1f,
                    1f,
                    0
                )
            )
            player.connection.send(ClientboundSetTitleTextPacket(Component.literal("Зона §6\"${zone.name}\" §4покинута!")))
            player.connection.send(ClientboundSetSubtitleTextPacket(Component.literal("§5Счастливой дороги!")))
        }
    }
}
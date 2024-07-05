package by.lisoveliy.zones.services

import by.lisoveliy.zones.extensions.PositionExtensions.getIntegerVector
import by.lisoveliy.zones.extensions.PacketExtensions
import by.lisoveliy.zones.models.zoneManager.TitleAnimationParams
import by.lisoveliy.zones.models.zoneManager.Zone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val titleAnimationParams = TitleAnimationParams.defaultAnimationParams
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
        runBlocking {
        //Update zone state
        exitedZones.forEach { zone: Zone ->
            playerZones[player.uuid]!!.remove(zone)
        }
        enteredZones.forEach { zone: Zone ->
            playerZones[player.uuid]!!.add(zone)
        }

        //Send animation
            launch {
                //Prevent spam on zones collision
                if(exitedZones.count() > enteredZones.count())
                exitedZones.forEach { zone: Zone ->
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
                    val tap = titleAnimationParams.divided(exitedZones.count())
                    player.connection.send(PacketExtensions.fromTitleAnimationParams(tap))
                    player.connection.send(ClientboundSetTitleTextPacket(Component.literal("Зона §6\"${zone.name}\" §4покинута!")))
                    player.connection.send(ClientboundSetSubtitleTextPacket(Component.literal("§5Счастливой дороги!")))
                    delay(tap.fullDuration.toLong())
                }

                enteredZones.forEach { zone: Zone ->
                    val tap = titleAnimationParams.divided(enteredZones.count())
                    (player as ServerPlayer)
                    player.connection.send(PacketExtensions.fromTitleAnimationParams(tap))
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
                    delay(tap.fullDuration.toLong())
                }
            }
        }
    }
}
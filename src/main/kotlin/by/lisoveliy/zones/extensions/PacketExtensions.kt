package by.lisoveliy.zones.extensions

import by.lisoveliy.zones.models.zoneManager.TitleAnimationParams
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket

object PacketExtensions {
    fun fromTitleAnimationParams(titleAnimationParams: TitleAnimationParams): ClientboundSetTitlesAnimationPacket {
        return ClientboundSetTitlesAnimationPacket(
            titleAnimationParams.fadeIn,
            titleAnimationParams.duration,
            titleAnimationParams.fadeOut
        )
    }
}
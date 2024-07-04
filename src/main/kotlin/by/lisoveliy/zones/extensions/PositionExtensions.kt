package by.lisoveliy.zones.extensions

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

object PositionExtensions {
    fun Vec3.getIntegerVector(): Vec3i {
        return Vec3i(this.x.toInt(), this.y.toInt(), this.z.toInt())
    }
}
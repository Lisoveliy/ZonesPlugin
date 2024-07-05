package by.lisoveliy.zones.models.jsonSerializable




class BoundingBox(val minX: Int, val minY: Int, val minZ: Int, val maxX: Int, val maxY: Int, val maxZ: Int){
    fun toMinecraft(): net.minecraft.world.level.levelgen.structure.BoundingBox{
        return net.minecraft.world.level.levelgen.structure.BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }
}
package by.lisoveliy.zones.models.jsonSerializable

import by.lisoveliy.zones.models.zoneManager.Zone


class Config(val zones: List<Zone> = listOf()){
    companion object{
        val defaultConfig: Config = Config(listOf(Zone("Спавн", BoundingBox(-160, -999, -160, 160, 999, 160), "overworld")))
    }
}
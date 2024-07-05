package by.lisoveliy.zones.models.zoneManager

import by.lisoveliy.zones.models.jsonSerializable.BoundingBox
import com.google.gson.annotations.SerializedName

class Zone(val name: String, val position: BoundingBox, @SerializedName("dimension")val dimensionId: String)
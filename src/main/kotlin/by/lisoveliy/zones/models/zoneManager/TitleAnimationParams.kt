package by.lisoveliy.zones.models.zoneManager

class TitleAnimationParams(val fadeIn: Int, val duration: Int, val fadeOut: Int) {

    val fullDuration: Int = fadeIn + duration + fadeOut

    fun divided(titleCount: Int): TitleAnimationParams {
        val fadeMultiplier = if (titleCount > 1) titleCount * 0.2 else titleCount.toDouble()
        val durationMultiplier = if (titleCount > 1) titleCount * 0.5 else titleCount.toDouble()
        return TitleAnimationParams(
            Math.round(fadeIn / fadeMultiplier).toInt(),
            Math.round(duration / durationMultiplier).toInt(),
            Math.round(fadeOut / fadeMultiplier).toInt()
        )
    }

    companion object {
        val defaultAnimationParams = TitleAnimationParams(4, 24, 6)
    }
}
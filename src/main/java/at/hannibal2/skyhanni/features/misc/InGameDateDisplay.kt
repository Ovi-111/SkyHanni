package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.formatted
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class InGameDateDisplay {
    private val config get() = SkyHanniMod.feature.gui.inGameDateConfig
    private val monthAndDatePattern = ".*((Early|Late) )?(Winter|Spring|Summer|Autumn) [0-9]{1,2}(nd|rd|th|st).*".toPattern()
    private var display = ""

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!LorenzUtils.inSkyBlock) return
        if (!config.useScoreboard && !event.repeatSeconds(config.refreshSeconds)) return
        if (config.useScoreboard && !event.repeatSeconds(1)) return

        checkDate()
    }

    private fun checkDate() {
        val date = SkyBlockTime.now()
        var theBaseString: String
        if (config.useScoreboard) {
            val list = ScoreboardData.sidebarLinesFormatted //we need this to grab the moon/sun symbol
            val year = "Year ${date.year}"
            var monthAndDate = "??" //initalize as "??" as fallback value in case none of the scoreboard lines match
            for (line in list) { monthAndDatePattern.matchMatcher(line) { monthAndDate = line } }
            val time = list.find{ it.lowercase().contains("am ") || it.lowercase().contains("pm ") } ?: "??"
            theBaseString = "$monthAndDate, $year ${time.trim()}".removeColor()
            if (!config.includeSunMoon) theBaseString = theBaseString.replace("☽", "").replace("☀", "").replace("࿇", "")
        } else {
            theBaseString = date.formatted()
            if (config.includeSunMoon) {
                if ((date.hour >= 6) && (date.hour < 17)) theBaseString = "$theBaseString ☀"
                else theBaseString = "$theBaseString ☽"
            }
        }
        display = theBaseString
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.position.renderString(display, posLabel = "In-game Date Display")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}

package com.enderzombi102.jv

import com.enderzombi102.jv.constants.MainViewStyle
import com.enderzombi102.jv.views.MainView
import org.fusesource.jansi.AnsiConsole
import tornadofx.App
import tornadofx.launch

class JourneyViewApp : App( MainView::class, MainViewStyle::class )

fun main( argv: Array<String> ) {
    AnsiConsole.systemInstall()
    launch<JourneyViewApp>( argv )
}

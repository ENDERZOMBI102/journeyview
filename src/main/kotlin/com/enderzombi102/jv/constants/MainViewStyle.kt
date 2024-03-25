package com.enderzombi102.jv.constants

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.cssclass

class MainViewStyle : Stylesheet() {
    companion object {
        val red by cssclass()
        val reset by cssclass()

        const val dangerColor = "#FF6666"
    }

    init {
        red {
            baseColor = Color.valueOf(dangerColor)
        }

        reset {
            baseColor = Color.WHITE
        }
    }
}
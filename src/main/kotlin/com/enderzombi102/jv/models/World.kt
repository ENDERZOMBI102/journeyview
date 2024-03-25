package com.enderzombi102.jv.models

import java.io.File

class World(val directory: File) {
    var dimensions: List<Dimension> = listOf()

    init {
        val dimensions: MutableList<Dimension> = mutableListOf()

        this.directory.listFiles()!!.forEach {
            if(it.isDirectory) {
                val dimension = Dimension(it)
                if (dimension.mapTypes.isNotEmpty() || dimension.layers.isNotEmpty()) {
                    dimensions.add(dimension)
                }
            }
        }

        this.dimensions = dimensions.toList()
    }

    override fun toString(): String {
        return directory.name
    }
}

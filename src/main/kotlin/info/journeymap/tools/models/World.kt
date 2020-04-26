package info.journeymap.tools.models

import java.io.File

class World(val directory: File) {
    var dimensions: List<Dimension> = listOf()

    init {
        val dimensions: MutableList<Dimension> = mutableListOf()

        this.directory.listFiles()!!.filter { it.name.startsWith("DIM") }.forEach {
            dimensions.add(Dimension(it))
        }

        this.dimensions = dimensions.toList()
    }

    override fun toString(): String {
        return directory.name
    }
}

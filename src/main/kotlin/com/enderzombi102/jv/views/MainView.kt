package com.enderzombi102.jv.views

import com.enderzombi102.jv.constants.GridType
import com.enderzombi102.jv.constants.MapType
import com.enderzombi102.jv.constants.WorldType
import com.enderzombi102.jv.controllers.MainController
import com.enderzombi102.jv.images.ImageStitcher
import com.enderzombi102.jv.models.Dimension
import com.enderzombi102.jv.models.World
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class MainView : View() {
    val controller: MainController by inject()
    val taskStatus: TaskStatus by inject()

    override val root = vbox {
        this.paddingAll = 10
        this.spacing = 10.0

        // Minecraft directory input
        hbox {
            alignment = Pos.CENTER_LEFT
            spacing = 10.0

            label("Minecraft Directory")

            textfield(controller.minecraftDirectoryPath) {
                hgrow = Priority.ALWAYS

                bindClass(controller.textInputClass)

                setOnAction {
                    controller.minecraftDirectoryPath.set(this.text)
                }
                tooltipProperty().bind(controller.textInputTooltip)

                focusedProperty().addListener { _, _, newValue ->
                    if (!newValue) {
                        controller.minecraftDirectoryPath.set(this.text)
                    }
                }
            }

            button("Browse") {
                hgrow = Priority.ALWAYS
                minWidth = 50.0

                action {
                    var openingDirectory = controller.minecraftDirectory

                    if (openingDirectory != null && (!openingDirectory.exists() || !openingDirectory.isDirectory)) {
                        openingDirectory = null
                    }

                    val directory = chooseDirectory("Select Minecraft Directory", openingDirectory)
                    controller.minecraftDirectory = directory
                }
            }
        }

        separator { }

        // Map settings
        gridpane {
            alignment = Pos.CENTER_LEFT
            hgap = 10.0
            vgap = 10.0
            useMaxWidth = true

            (0..3).forEach {
                constraintsForColumn(it).percentWidth = when {
                    (it % 2) == 0 -> 20.0
                    else          -> 30.0
                }
            }

            row { // Row 1
                label("World Type")
                combobox<WorldType>(controller.worldTypeProperty(), controller.validWorldTypes) {
                    useMaxWidth = true
                    enableWhen(controller.textInputValid)
                }

                label("World")
                combobox<World>(controller.world, controller.validWorlds) {
                    useMaxWidth = true
                    enableWhen(controller.textInputValid)
                }
            }
            row { // Row 2
                label("Dimension")
                combobox<Dimension>(controller.dimension, controller.validDimensions) {
                    useMaxWidth = true
                    enableWhen(controller.textInputValid)
                }

                label("Map Type")
                combobox<MapType>(controller.mapTypeProperty(), controller.validMapTypes) {
                    useMaxWidth = true
                    enableWhen(controller.textInputValid)
                }
            }
            row { // Row 3
                label("Surface Layer")
                spinner<Int>(items = controller.validLayers, property = controller.layer) {
                    useMaxWidth = true

                    enableWhen(controller.textInputValid.and(controller.mapTypeProperty().isEqualTo(MapType.UNDERGROUND)))
                }

                label("Grid")
                combobox<GridType>(controller.gridTypeProperty(), controller.gridTypes) {
                    useMaxWidth = true

                    enableWhen(controller.textInputValid)
                }
            }
        }

        separator { }

        // Progress/action button
        hbox {
            alignment = Pos.CENTER_LEFT
            spacing = 10.0

            label(taskStatus.message)

            progressbar(taskStatus.progress) {
                hgrow = Priority.ALWAYS
                useMaxWidth = true
            }
            button("Export") {
                hgrow = Priority.ALWAYS

                enableWhen(
                        controller.textInputValid
                                .and(controller.world.isNotNull)
                                .and(controller.worldTypeProperty().isNotNull)
                                .and(controller.dimension.isNotNull)
                                .and(controller.mapTypeProperty().isNotNull)
                                .and(taskStatus.running.not())
                )
                action {
                    val targetFiles = chooseFile(
                            "Save map as...",
                            filters = arrayOf(FileChooser.ExtensionFilter("PNG files", "*.png")),
                            mode = FileChooserMode.Save
                    )

                    if (targetFiles.isNotEmpty()) {
                        val target = targetFiles[0]

                        val dimDirectory: File = (if (controller.mapType == MapType.UNDERGROUND) {
                            controller.dimension.value.getLayerDirectory(controller.layer.value)
                        } else {
                            controller.dimension.value.getMapDirectory(controller.mapType)
                        }) ?: return@action  // Should never be null, but checked for safety

                        val stitcher = ImageStitcher(dimDirectory)

                        runAsync {
                            stitcher.stitch(target, controller.gridType, this)
                        }
                    }
                }
            }
        }
    }

    init {
        currentStage?.isResizable = false

        this.setWindowMinSize(600, 230)
        this.title = "JourneyMap Tools"

        this.controller.validateMinecraftDirectory()
    }
}

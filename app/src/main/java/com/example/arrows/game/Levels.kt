package com.example.arrows.game

/**
 * Hand-crafted puzzle levels for "The Arrows Game", ensuring authentic gameplay.
 */
object Levels {
    data class LevelDefinition(
        val levelNumber: Int,
        val title: String,
        val rows: Int,
        val cols: Int,
        val grid: List<List<ArrowDirection?>>
    )

    val allLevels = listOf(
        // Level 1: 3x3 Warm-up
        LevelDefinition(
            levelNumber = 1,
            title = "First Steps",
            rows = 3,
            cols = 3,
            grid = listOf(
                listOf(ArrowDirection.UP, ArrowDirection.UP, ArrowDirection.RIGHT),
                listOf(ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.RIGHT),
                listOf(ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.DOWN)
            )
        ),
        // Level 2: 4x4 Corner Traps
        LevelDefinition(
            levelNumber = 2,
            title = "Perimeter Check",
            rows = 4,
            cols = 4,
            grid = listOf(
                listOf(ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.RIGHT, ArrowDirection.DOWN),
                listOf(ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.DOWN, ArrowDirection.RIGHT),
                listOf(ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.DOWN),
                listOf(ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.DOWN)
            )
        ),
        // Level 3: 4x4 The Spiral
        LevelDefinition(
            levelNumber = 3,
            title = "The Vortex",
            rows = 4,
            cols = 4,
            grid = listOf(
                listOf(ArrowDirection.RIGHT, ArrowDirection.RIGHT, ArrowDirection.RIGHT, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.LEFT)
            )
        ),
        // Level 4: 5x5 Dense Maze
        LevelDefinition(
            levelNumber = 4,
            title = "Crossfire",
            rows = 5,
            cols = 5,
            grid = listOf(
                listOf(ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.UP),
                listOf(ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.RIGHT),
                listOf(ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.DOWN),
                listOf(ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.RIGHT),
                listOf(ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.RIGHT, ArrowDirection.DOWN)
            )
        ),
        // Level 5: 5x5 Mastermind
        LevelDefinition(
            levelNumber = 5,
            title = "The Gauntlet",
            rows = 5,
            cols = 5,
            grid = listOf(
                listOf(ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.DOWN, ArrowDirection.RIGHT),
                listOf(ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.DOWN, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.RIGHT, ArrowDirection.DOWN),
                listOf(ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.UP, ArrowDirection.LEFT, ArrowDirection.DOWN),
                listOf(ArrowDirection.LEFT, ArrowDirection.UP, ArrowDirection.RIGHT, ArrowDirection.LEFT, ArrowDirection.UP)
            )
        )
    )

    /**
     * Initializes an ArrowGameState from a level definition.
     */
    fun createGameState(level: LevelDefinition): ArrowGameState {
        val arrows = mutableMapOf<String, ArrowItem>()
        for (r in 0 until level.rows) {
            for (c in 0 until level.cols) {
                val dir = level.grid.getOrNull(r)?.getOrNull(c)
                if (dir != null) {
                    val id = "arrow_${r}_${c}"
                    arrows[id] = ArrowItem(
                        id = id,
                        row = r,
                        col = c,
                        direction = dir
                    )
                }
            }
        }
        return ArrowGameState(rows = level.rows, cols = level.cols, arrows = arrows)
    }
}


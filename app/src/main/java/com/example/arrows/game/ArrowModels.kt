package com.example.arrows.game

/**
 * Direction enum representing the four cardinal directions for each arrow.
 */
enum class ArrowDirection(val dRow: Int, val dCol: Int, val rotationDegrees: Float) {
    UP(-1, 0, 0f),
    RIGHT(0, 1, 90f),
    DOWN(1, 0, 180f),
    LEFT(0, -1, 270f);
}

/**
 * Represents an individual arrow in the puzzle grid.
 */
data class ArrowItem(
    val id: String,
    val row: Int,
    val col: Int,
    val direction: ArrowDirection,
    val isEscaped: Boolean = false,
    val isEscaping: Boolean = false,
    val isShaking: Boolean = false
)

/**
 * Game state containing the grid dimensions and arrow states.
 */
data class ArrowGameState(
    val rows: Int,
    val cols: Int,
    val arrows: Map<String, ArrowItem>,
    val movesCount: Int = 0,
    val isWon: Boolean = false
) {
    /**
     * Checks if the arrow at the given coordinates has an unobstructed path to the board edge.
     */
    fun canEscape(arrow: ArrowItem): Boolean {
        var r = arrow.row + arrow.direction.dRow
        var c = arrow.col + arrow.direction.dCol

        while (r in 0 until rows && c in 0 until cols) {
            // If another active (not escaped) arrow occupies this cell, the path is blocked!
            val blockingArrow = arrows.values.find { it.row == r && it.col == c && !it.isEscaped }
            if (blockingArrow != null) {
                return false
            }
            r += arrow.direction.dRow
            c += arrow.direction.dCol
        }
        return true
    }

    /**
     * Taps an arrow. If free to escape, marks it as escaped. If blocked, marks as shaking.
     */
    fun tapArrow(arrowId: String): ArrowGameState {
        val arrow = arrows[arrowId] ?: return this
        if (arrow.isEscaped) return this

        return if (canEscape(arrow)) {
            val updated = arrows.toMutableMap()
            updated[arrowId] = arrow.copy(isEscaped = true, isShaking = false)
            val remaining = updated.values.count { !it.isEscaped }
            copy(
                arrows = updated,
                movesCount = movesCount + 1,
                isWon = remaining == 0
            )
        } else {
            // Blocked: trigger collision shake
            val updated = arrows.toMutableMap()
            updated[arrowId] = arrow.copy(isShaking = true)
            copy(arrows = updated, movesCount = movesCount + 1)
        }
    }

    /**
     * Clears shake state after animation.
     */
    fun clearShake(arrowId: String): ArrowGameState {
        val arrow = arrows[arrowId] ?: return this
        if (!arrow.isShaking) return this
        val updated = arrows.toMutableMap()
        updated[arrowId] = arrow.copy(isShaking = false)
        return copy(arrows = updated)
    }
}


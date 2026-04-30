package com.example.xpenso.AddExpenses


val OPERATORS = setOf('+', '-', '*', '/')

/**
 * Full expression evaluator (left-to-right with proper precedence).
 */
fun evaluateExpression(expr: String): String {
    return try {
        val clean = expr.trim()
        if (clean.isEmpty()) return "0"
        val result = object : Any() {
            var pos = -1; var ch = 0
            fun eat(c: Int): Boolean {
                while (ch == ' '.code) nextChar()
                return if (ch == c) { nextChar(); true } else false
            }
            fun nextChar() { ch = if (++pos < clean.length) clean[pos].code else -1 }
            fun parse(): Double { nextChar(); val x = parseExpr(); return x }
            fun parseExpr(): Double { var x = parseTerm(); while (true) x = when { eat('+'.code) -> x + parseTerm(); eat('-'.code) -> x - parseTerm(); else -> return x } }
            fun parseTerm(): Double { var x = parseFactor(); while (true) x = when { eat('*'.code) -> x * parseFactor(); eat('/'.code) -> x / parseFactor(); else -> return x } }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                val startPos = pos
                while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()

                return clean.substring(startPos, pos).toDouble()
            }
//            fun parseFactor(): Double {
//                if (eat('+'.code)) return parseFactor()
//                if (eat('-'.code)) return -parseFactor()
//                val startPos = pos
//                if (eat('('.code)) { val x = parseExpr(); eat(')'.code); return x }
//                while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
//                return clean.substring(startPos, pos).toDouble()
//            }
        }.parse()
        val long = result.toLong()
        if (result == long.toDouble()) long.toString()
        else result.toBigDecimal().setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    } catch (e: Exception) { expr }
}

/** Returns true if [expr] contains an operator after the first character. */
fun hasOperator(expr: String): Boolean = expr.drop(1).any { it in OPERATORS }

/**
 * Returns the "current segment" — the number currently being entered.
 *
 * Rules:
 *  - Before any operator is typed  → show the whole expression (it's just a number).
 *  - After an operator is typed but the user hasn't started a new number yet → show "" (show 0 placeholder).
 *  - After the user types at least one digit after an operator → show only that new segment.
 *  - After "=" → show the result (whole expr, which is now a plain number).
 */
fun displaySegment(expr: String): String {

    if (expr.isEmpty()) return ""

    // Fix negative number display
    if (expr.startsWith("-") && expr.drop(1).none { it in OPERATORS }) {
        return expr
    }

    val last = expr.last()

    val lastOpIndex = expr.indexOfLast { it in OPERATORS }

    // No operator yet
    if (lastOpIndex == -1) return expr

    // Last number after operator
    val lastNumber = expr.substring(lastOpIndex + 1)

    // Previous number before operator
    val previousPart = expr.substring(0, lastOpIndex)
    val previousOpIndex = previousPart.indexOfLast { it in OPERATORS }

    val previousNumber =
        if (previousOpIndex == -1)
            previousPart
        else
            previousPart.substring(previousOpIndex + 1)

    return if (last in OPERATORS) {
        previousNumber + last
    } else {
        lastNumber
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXPENSE DIALOG — main entry point
// ─────────────────────────────────────────────────────────────────────────────

fun cleanAmount(value: Double): String {
    val long = value.toLong()
    return if (value == long.toDouble()) {
        long.toString()
    } else {
        value.toBigDecimal()
            .stripTrailingZeros()
            .toPlainString()
    }
}
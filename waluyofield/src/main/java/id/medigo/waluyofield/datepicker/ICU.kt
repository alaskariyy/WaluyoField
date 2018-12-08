package id.medigo.waluyofield.datepicker

object ICU {

    /**
     * This method is directly copied from {libcore.icu.ICU}. The method is simple enough
     * that it probably won't change.
     */
    fun getDateFormatOrder(pattern: String): CharArray {
        val result = CharArray(3)
        var resultIndex = 0
        var sawDay = false
        var sawMonth = false
        var sawYear = false

        var i = 0
        while (i < pattern.length) {
            val ch = pattern[i]
            if (ch == 'd' || ch == 'L' || ch == 'M' || ch == 'y') {
                if (ch == 'd' && !sawDay) {
                    result[resultIndex++] = 'd'
                    sawDay = true
                } else if ((ch == 'L' || ch == 'M') && !sawMonth) {
                    result[resultIndex++] = 'M'
                    sawMonth = true
                } else if (ch == 'y' && !sawYear) {
                    result[resultIndex++] = 'y'
                    sawYear = true
                }
            } else if (ch == 'G') {
                // Ignore the era specifier, if present.
            } else if (ch in 'a'..'z' || ch in 'A'..'Z') {
                throw IllegalArgumentException("Bad pattern character '"
                        + ch + "' in " + pattern)
            } else if (ch == '\'') {
                if (i < pattern.length - 1 && pattern[i + 1] == '\'') {
                    ++i
                } else {
                    i = pattern.indexOf('\'', i + 1)
                    if (i == -1) {
                        throw IllegalArgumentException("Bad quoting in $pattern")
                    }
                    ++i
                }
            } else {
                // Ignore spaces and punctuation.
            }
            ++i
        }
        return result
    }

}
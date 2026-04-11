package com.autokeyboard.data.provider

/**
 * Parses AI responses into structured variants and emojis.
 * Handles the ---VARIANT--- delimiter and EMOJIS: line format.
 */
object ResponseParser {

    data class ParsedResponse(
        val variants: List<String>,
        val emojis: List<String>
    )

    /**
     * Parses the full AI response text into variants and emojis.
     */
    fun parse(rawResponse: String): ParsedResponse {
        val trimmed = rawResponse.trim()

        // Extract emojis line
        val emojiRegex = Regex("""EMOJIS?:\s*(.+)""", RegexOption.IGNORE_CASE)
        val emojiMatch = emojiRegex.find(trimmed)
        val emojis = emojiMatch?.groupValues?.get(1)?.let { emojiLine ->
            extractEmojis(emojiLine)
        } ?: emptyList()

        // Remove emoji line from response
        val textWithoutEmojis = if (emojiMatch != null) {
            trimmed.substring(0, emojiMatch.range.first).trim()
        } else {
            trimmed
        }

        // Split by variant delimiter
        val variants = textWithoutEmojis
            .split("---VARIANT---")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { cleanVariant(it) }

        return ParsedResponse(
            variants = variants.ifEmpty { listOf(textWithoutEmojis) },
            emojis = emojis.take(5)
        )
    }

    /**
     * Extracts emoji characters from a text line.
     */
    private fun extractEmojis(text: String): List<String> {
        // Match emoji characters using Unicode ranges
        val emojiPattern = Regex(
            "[\\x{1F600}-\\x{1F64F}]|[\\x{1F300}-\\x{1F5FF}]|" +
            "[\\x{1F680}-\\x{1F6FF}]|[\\x{1F1E0}-\\x{1F1FF}]|" +
            "[\\x{2600}-\\x{26FF}]|[\\x{2700}-\\x{27BF}]|" +
            "[\\x{1F900}-\\x{1F9FF}]|[\\x{1FA00}-\\x{1FA6F}]|" +
            "[\\x{1FA70}-\\x{1FAFF}]|[\\x{231A}-\\x{231B}]|" +
            "[\\x{23E9}-\\x{23F3}]|[\\x{23F8}-\\x{23FA}]|" +
            "[\\x{200D}]|[\\x{FE0F}]|[\\x{20E3}]|" +
            "[\\x{E0020}-\\x{E007F}]"
        )

        // Simpler approach: split by whitespace and take items that look like emojis
        return text.trim().split(Regex("\\s+"))
            .filter { it.isNotBlank() && !it.all { c -> c.isLetterOrDigit() || c == ':' } }
            .take(5)
    }

    /**
     * Cleans a variant text by removing leading/trailing quotes, numbering, etc.
     */
    private fun cleanVariant(text: String): String {
        var cleaned = text.trim()
        // Remove leading "1." or "2." numbering
        cleaned = cleaned.replace(Regex("""^\d+\.\s*"""), "")
        // Remove surrounding quotes
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
            (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length - 1)
        }
        return cleaned.trim()
    }
}

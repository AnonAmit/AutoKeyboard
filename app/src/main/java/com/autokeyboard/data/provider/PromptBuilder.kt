package com.autokeyboard.data.provider

import com.autokeyboard.data.model.Tone

/**
 * Assembles system and user prompts for rewrite operations.
 * Each tone maps to a specific system personality.
 */
object PromptBuilder {

    /**
     * Builds the system prompt that defines the AI's behavior.
     */
    fun buildSystemPrompt(tone: Tone, customPrompt: String?, variantCount: Int): String {
        val toneInstruction = if (tone == Tone.CUSTOM && !customPrompt.isNullOrBlank()) {
            "Follow this custom style instruction exactly: $customPrompt"
        } else {
            tone.systemPromptFragment
        }

        return """
            |You are AutoKeyboard, an AI writing assistant embedded in a mobile keyboard.
            |Your job is to rewrite the user's message according to their chosen tone.
            |
            |RULES:
            |1. $toneInstruction
            |2. Generate exactly $variantCount different variants of the rewrite.
            |3. Separate each variant with the delimiter: ---VARIANT---
            |4. Keep the core meaning and intent of the original message.
            |5. Keep rewrites concise — suitable for messaging apps.
            |6. Do NOT add quotes around the rewritten text.
            |7. After all variants, on a new line write: EMOJIS: followed by exactly 5 contextual emojis separated by spaces.
            |
            |Example output format:
            |First rewrite variant here
            |---VARIANT---
            |Second rewrite variant here
            |EMOJIS: 🙏 ⏳ 🏃‍♂️ 💼 🤝
        """.trimMargin()
    }

    /**
     * Builds the user message portion of the prompt.
     */
    fun buildUserPrompt(input: String): String {
        return "Rewrite this message:\n\"$input\""
    }

    /**
     * Builds a prompt specifically for emoji suggestion.
     */
    fun buildEmojiPrompt(input: String, tone: Tone): String {
        return """
            |Given this message and the "${tone.displayName}" tone, suggest exactly 5 contextual emojis.
            |Message: "$input"
            |Reply with ONLY the 5 emojis separated by spaces, nothing else.
        """.trimMargin()
    }
}

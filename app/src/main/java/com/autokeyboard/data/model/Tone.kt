package com.autokeyboard.data.model

/**
 * Available rewrite tones — matches the tone_selector_panel HTML design.
 * 8 presets + 1 custom option.
 */
enum class Tone(
    val displayName: String,
    val emoji: String,
    val systemPromptFragment: String
) {
    PROFESSIONAL(
        displayName = "Professional",
        emoji = "💼",
        systemPromptFragment = "Rewrite in a professional, business-appropriate tone. Use formal language, proper grammar, and maintain clarity."
    ),
    FRIENDLY(
        displayName = "Friendly",
        emoji = "😊",
        systemPromptFragment = "Rewrite in a warm, friendly tone. Be approachable and personable while keeping the message clear."
    ),
    CASUAL(
        displayName = "Casual",
        emoji = "✌️",
        systemPromptFragment = "Rewrite in a casual, laid-back tone. Use conversational language as if texting a friend."
    ),
    FLIRTY(
        displayName = "Flirty",
        emoji = "😘",
        systemPromptFragment = "Rewrite in a flirty, playful tone. Add charm and subtle romantic undertones while keeping it tasteful."
    ),
    WITTY(
        displayName = "Witty",
        emoji = "😏",
        systemPromptFragment = "Rewrite with wit and clever humor. Be sharp, use wordplay, and make the reader smile."
    ),
    POETIC(
        displayName = "Poetic",
        emoji = "🎭",
        systemPromptFragment = "Rewrite in a poetic, lyrical style. Use metaphors, vivid imagery, and elegant language."
    ),
    GEN_Z(
        displayName = "Gen-Z",
        emoji = "🔥",
        systemPromptFragment = "Rewrite in Gen-Z internet slang. Use abbreviations, lowercase, current slang, and casual vibes. no cap fr fr."
    ),
    FORMAL(
        displayName = "Formal",
        emoji = "🎩",
        systemPromptFragment = "Rewrite in a highly formal, dignified tone. Use sophisticated vocabulary and structured sentences."
    ),
    CONCISE(
        displayName = "Concise",
        emoji = "🎯",
        systemPromptFragment = "Rewrite this to be as short and direct as possible. Remove fluff, keep only the core message."
    ),
    ENTHUSIASTIC(
        displayName = "Enthusiastic",
        emoji = "🤩",
        systemPromptFragment = "Rewrite this with high energy and enthusiasm! Use exclamation marks and optimistic language."
    ),
    SARCASTIC(
        displayName = "Sarcastic",
        emoji = "🙃",
        systemPromptFragment = "Rewrite this with a heavy dose of sarcasm and irony. Make it dry and mocking."
    ),
    CONFIDENT(
        displayName = "Confident",
        emoji = "😎",
        systemPromptFragment = "Rewrite this to sound extremely confident, authoritative, and assured."
    ),
    EMPATHETIC(
        displayName = "Empathetic",
        emoji = "🤍",
        systemPromptFragment = "Rewrite this to be deeply empathetic, understanding, and kind."
    ),
    PERSUASIVE(
        displayName = "Persuasive",
        emoji = "🗣️",
        systemPromptFragment = "Rewrite this to be highly persuasive and convincing, using strong rhetorical techniques."
    ),
    PASSIVE_AGGRESSIVE(
        displayName = "Passive Agg.",
        emoji = "☕",
        systemPromptFragment = "Rewrite this to be passive-aggressive. Sound polite on the surface but clearly annoyed underneath."
    ),
    PIRATE(
        displayName = "Pirate",
        emoji = "🏴‍☠️",
        systemPromptFragment = "Rewrite this like a stereotype of an 18th-century pirate. Throw in some 'arrs' and pirate slang."
    ),
    CUSTOM(
        displayName = "Custom",
        emoji = "✨",
        systemPromptFragment = "" // User provides their own prompt
    );

    companion object {
        fun fromName(name: String): Tone =
            entries.find { it.name.equals(name, ignoreCase = true) } ?: PROFESSIONAL
    }
}

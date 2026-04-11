package com.autokeyboard.data.provider.local

import com.autokeyboard.data.model.Tone

/**
 * Chat prompt templates for local on-device models.
 * Each model family uses a different chat template format.
 */
object LocalPromptTemplates {

    private fun toneInstruction(tone: Tone, customPrompt: String?): String {
        return if (tone == Tone.CUSTOM && !customPrompt.isNullOrBlank()) {
            customPrompt
        } else {
            tone.systemPromptFragment
        }
    }

    /** Phi-3 chat template */
    fun buildPhiPrompt(input: String, tone: Tone, customPrompt: String?): String {
        val inst = toneInstruction(tone, customPrompt)
        val sysTag = "\u003C|system|\u003E"
        val usrTag = "\u003C|user|\u003E"
        val asstTag = "\u003C|assistant|\u003E"
        val endTag = "\u003C|end|\u003E"
        return "${sysTag}\n${inst}${endTag}\n${usrTag}\nRewrite: \"${input}\"${endTag}\n${asstTag}\n"
    }

    /** Gemma chat template */
    fun buildGemmaPrompt(input: String, tone: Tone, customPrompt: String?): String {
        val inst = toneInstruction(tone, customPrompt)
        val startTurn = "\u003Cstart_of_turn\u003E"
        val endTurn = "\u003Cend_of_turn\u003E"
        return "${startTurn}user\n${inst}\n\nRewrite: \"${input}\"\n${endTurn}\n${startTurn}model\n"
    }

    /** Qwen chat template */
    fun buildQwenPrompt(input: String, tone: Tone, customPrompt: String?): String {
        val inst = toneInstruction(tone, customPrompt)
        val imStart = "\u003C|im_start|\u003E"
        val imEnd = "\u003C|im_end|\u003E"
        return "${imStart}system\n${inst}${imEnd}\n${imStart}user\nRewrite: \"${input}\"${imEnd}\n${imStart}assistant\n"
    }

    /** Llama 3 chat template */
    fun buildLlamaPrompt(input: String, tone: Tone, customPrompt: String?): String {
        val inst = toneInstruction(tone, customPrompt)
        val headerSys = "\u003C|begin_of_text|\u003E\u003C|start_header_id|\u003Esystem\u003C|end_header_id|\u003E"
        val headerUsr = "\u003C|start_header_id|\u003Euser\u003C|end_header_id|\u003E"
        val headerAsst = "\u003C|start_header_id|\u003Eassistant\u003C|end_header_id|\u003E"
        val eot = "\u003C|eot_id|\u003E"
        return "${headerSys}\n\n${inst}${eot}\n${headerUsr}\n\nRewrite: \"${input}\"${eot}\n${headerAsst}\n\n"
    }
}

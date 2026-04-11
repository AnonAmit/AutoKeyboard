# The Design System: Editorial Intelligence

This design system is a high-end implementation of Material 3 principles, specifically tailored for a premium, AI-driven Android keyboard experience. It moves beyond "stock" Android aesthetics to create a signature visual identity—one that feels like a precision tool for digital expression.

---

## 1. Overview & Creative North Star: "The Obsidian Architect"

The Creative North Star for this system is **The Obsidian Architect**. The interface should feel like a single, seamless slab of dark glass where the AI "breathes" through light and motion rather than lines and boxes. 

We reject the "boxed-in" look of traditional keyboards. Instead, we embrace **Physicality through Tone**. By using intentional asymmetry in layout and shifting background depths, we create a UI that feels carved from a single material rather than assembled from parts. 

---

## 2. Color & Surface Philosophy

The palette is anchored in an OLED-safe, deep-space aesthetic. We utilize a "Dark-First" strategy where the black of the screen becomes an active design element.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning. Boundaries must be defined solely through background color shifts or subtle tonal transitions. A container’s edge is defined by where its color ends and the next begins, not by a stroke.

### Surface Hierarchy & Nesting
Depth is achieved through the stacking of `surface-container` tiers. 
- **Base Layer:** `surface` (#0c0c1f) is the canvas.
- **Sectioning:** Use `surface-container-low` (#111127) for subtle grouping.
- **Key Actions/Tiles:** Use `surface-container-highest` (#23233f) or the custom `key-tiles` token (#2D2D44) to bring primary touch targets toward the user.

### The "Glass & Gradient" Rule
Floating elements (like AI predictive bubbles) should utilize **Glassmorphism**. Apply `surface-variant` with a 60% opacity and a 16dp backdrop blur. To provide "soul," CTAs use a linear gradient from `primary` (#b6a0ff) to `primary-dim` (#7e51ff), angled at 135 degrees.

---

## 3. Typography: Editorial Authority

We use a dual-typeface system to balance technical precision with high-end editorial flair.

- **Display & Headlines:** **Plus Jakarta Sans.** This typeface provides a modern, geometric personality that feels distinct from system defaults. It is used for onboarding, settings headers, and AI "moments."
- **Body & Labels:** **Inter.** Chosen for its extreme legibility at small scales (crucial for keyboard secondary symbols) and its neutral, high-tech character.

**Hierarchy Strategy:**
- **Display-LG (3.5rem):** Reserved for atmospheric hero moments.
- **Headline-SM (1.5rem):** Standard for settings category headers.
- **Label-MD (0.75rem):** The workhorse for keyboard key secondary characters (symbols/numbers).

---

## 4. Elevation & Depth: Tonal Layering

Traditional drop shadows are largely abandoned in favor of **Tonal Lift**.

- **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-low` section. The contrast creates a soft, natural lift that is easier on the eyes in low-light environments.
- **Ambient Shadows:** When an element must "float" (e.g., a Bottom Sheet), use an extra-diffused shadow: `Blur: 32dp, Y: 8dp, Opacity: 8%`. The shadow color must be tinted with `primary` to avoid a "muddy" look.
- **The "Ghost Border" Fallback:** If a border is required for accessibility, it must be the `outline-variant` token at **15% opacity**. This creates a "suggestion" of a boundary rather than a hard wall.

---

## 5. Components

### Keyboard Keys
- **Dimensions:** 42dp height, 4dp radius (`sm`).
- **Styling:** Keys use `key-tiles` (#2D2D44). No borders. 
- **Active State:** On press, the key shifts to `primary-container` (#a98fff) with a subtle `primary` glow.

### Pill Buttons (AI Actions)
- **Radius:** 24dp (custom token `full`).
- **Styling:** Primary buttons use the `primary` to `primary-dim` gradient. Text is `on-primary` (#340090) set in **Inter Bold**.

### Bottom Sheets (The "Drawer")
- **Radius:** 24dp (`xl`) on top corners only.
- **Surface:** `surface-container-high` (#1d1d37).
- **Interaction:** No visible "close" button. Use a subtle `outline-variant` drag handle (32dp wide, 4dp height) at 20% opacity.

### Input Fields
- **Styling:** Forbid the use of "filled" or "outlined" boxes. Use a "Minimal Underline" style using `outline-variant` at 30% opacity, which glows to `secondary` (#4af8e3) when the AI is processing.

---

## 6. Do's and Don'ts

### Do
- **Use Breathing Room:** Use the 8pt grid to create generous whitespace between the keyboard and the app content.
- **Leverage Asymmetry:** In settings or menus, use varied horizontal padding to break the "grid-block" feel.
- **Prioritize OLED:** Ensure the `background` remains #0c0c1f or #000000 to save battery and increase contrast.

### Don'ts
- **No Divider Lines:** Never use a 1px line to separate list items. Use an 8dp vertical gap or a subtle background shift between items.
- **No Pure Greys:** Avoid #808080. All neutrals must be tinted with the `primary` or `surface-tint` hues to maintain the "obsidian" atmosphere.
- **No High-Opacity Borders:** Hard borders break the illusion of the "single slab" of glass.
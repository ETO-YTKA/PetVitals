---
name: PetVitals
description: A calm Android care journal for pet health, routines, records, and shared access.
colors:
  primary: "#075E63"
  primary-hover: "#064D51"
  primary-container: "#D9F0ED"
  on-primary: "#FFFFFF"
  canvas: "#F6FAF8"
  surface: "#FFFFFF"
  surface-raised: "#EEF6F3"
  surface-muted: "#E6EFEC"
  ink: "#17201F"
  ink-muted: "#52625F"
  ink-subtle: "#6E7D7A"
  border: "#D5E1DD"
  border-strong: "#A9BBB6"
  success: "#2F6B45"
  success-container: "#DDEFE2"
  warning: "#7A5A00"
  warning-container: "#FFF1BF"
  error: "#A33A32"
  error-container: "#F9DEDA"
typography:
  display:
    fontFamily: "Nunito Sans, Android system, sans-serif"
    fontSize: "32sp"
    fontWeight: 800
    lineHeight: 1.12
    letterSpacing: "-0.01em"
  headline:
    fontFamily: "Nunito Sans, Android system, sans-serif"
    fontSize: "24sp"
    fontWeight: 750
    lineHeight: 1.2
    letterSpacing: "0"
  title:
    fontFamily: "Nunito Sans, Android system, sans-serif"
    fontSize: "18sp"
    fontWeight: 700
    lineHeight: 1.28
    letterSpacing: "0"
  body:
    fontFamily: "Nunito Sans, Android system, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: 1.45
    letterSpacing: "0"
  label:
    fontFamily: "Nunito Sans, Android system, sans-serif"
    fontSize: "14sp"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "0.01em"
rounded:
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  pill: "999dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "24dp"
  xxl: "32dp"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    height: "52dp"
    padding: "0 20dp"
  button-primary-pressed:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label}"
    rounded: "{rounded.md}"
    height: "52dp"
    padding: "0 20dp"
  button-tonal:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.primary}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    height: "48dp"
    padding: "0 18dp"
  card-care-summary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.lg}"
    padding: "16dp"
  input-default:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.md}"
    height: "56dp"
    padding: "0 16dp"
---

# Design System: PetVitals

## 1. Overview

**Creative North Star: "The Calm Care Journal"**

PetVitals should feel like a practical care journal that a pet owner can trust before a vet visit, after a stressful incident, or during ordinary daily routines. The interface is warm without becoming childish, clinical enough to support health information without feeling like hospital software, and structured enough that records, medications, food routines, and shared access remain easy to scan.

This is a restrained product system. Color is used for action, status, and wayfinding, not decoration. Screens should feel composed and breathable, with clear hierarchy, generous touch targets, and familiar Android affordances. The product should disappear into the task: add the medication, check the food routine, find the last record, share the right pet details.

**Key Characteristics:**
- Restrained daily-app density: enough information to act, never a dense admin table by default.
- One calm teal accent: primary actions, active navigation, focus, and selected states only.
- Journal-like surfaces: layered off-white and soft green-gray neutrals, not beige, cream, or sterile blue-white hospital panels.
- Familiar Material structure: top bars, bottom navigation, sheets, inline forms, and confirmation patterns before destructive changes.
- Pet-centered warmth through copy, empty states, and gentle shapes, not cartoons or novelty controls.

## 2. Colors

The palette is a cool, humane teal system on neutral journal surfaces. It keeps the app calm in stressful care moments and avoids both cold medical blue and playful saturated pet-app color.

### Primary
- **Care Teal**: The single accent for primary buttons, active bottom navigation, selected chips, focus rings, and important status highlights.
- **Deep Care Teal**: The pressed and hover state for Care Teal. Use only as an interaction response or for high-contrast text on pale teal containers.
- **Quiet Teal Container**: A low-emphasis fill for selected filters, helpful info panels, and non-critical callouts.

### Secondary
- **Success Green**: Positive health or sync confirmation. Use with Success Container for inline banners and chips.
- **Amber Reminder**: Upcoming or attention-needed care reminders. Use with Warning Container and clear text, never as decorative yellow blocks.
- **Record Error Red**: Validation, failed saves, denied permissions, and destructive confirmations. Use sparingly and always with direct recovery copy.

### Neutral
- **Journal Canvas**: Default app background for authenticated screens.
- **Clean Page Surface**: Primary content containers, sheets, cards, and form surfaces.
- **Soft Care Layer**: Secondary surface for bottom navigation, grouped form regions, and low-emphasis panels.
- **Quiet Divider**: Default 1dp borders, dividers, input outlines, and card separation.
- **Care Ink**: Primary text and icons. Never use pure black.
- **Muted Care Ink**: Secondary labels, metadata, helper text, and timestamps. This must remain readable against Journal Canvas and Clean Page Surface.

### Named Rules
**The One Accent Rule.** Care Teal is the only accent color. It should occupy less than 10 percent of a normal screen so actions and selected states remain clear.

**The No-Hospital Rule.** Do not drift into sterile medical blue-gray dashboards. Surfaces may be clean, but the color temperature must stay humane and journal-like.

**The Status Means Status Rule.** Green, amber, and red are semantic only. They communicate success, warning, or error, never decoration.

## 3. Typography

**Display Font:** Nunito Sans with Android system sans fallback.
**Body Font:** Nunito Sans with Android system sans fallback.
**Label/Mono Font:** Nunito Sans for labels; use Android system monospace only for IDs, timestamps that require alignment, or debug-only values.

**Character:** Use one rounded humanist sans family to keep the app approachable and consistent. The type should read as calm product UI, not a pet brand poster. Hierarchy comes from weight, spacing, and color, not oversized display treatment.

### Hierarchy
- **Display** (800, 32sp, 1.12 line-height): Screen-level moments such as onboarding, empty pet lists, or a major pet profile heading. Use rarely.
- **Headline** (750, 24sp, 1.2 line-height): Main screen titles and primary section headers.
- **Title** (700, 18sp, 1.28 line-height): Card titles, list item names, medication names, pet names, and form group headers.
- **Body** (400, 16sp, 1.45 line-height): Descriptions, record notes, helper copy, and normal form text. Prose should stay under 75 characters per line when possible.
- **Label** (700, 14sp, 0.01em letter-spacing): Buttons, field labels, chips, tabs, and compact metadata. Avoid all-caps except for very short technical labels.

### Named Rules
**The No-Cute-Type Rule.** Do not use playful display fonts, bouncy letters, or cartoon-style type for pet warmth. Warmth comes from tone and spacing, not novelty typography.

**The Product Scale Rule.** Keep type sizes fixed and stable across Android screens. Do not use oversized responsive hero typography inside app surfaces.

## 4. Elevation

PetVitals is flat by default and uses tonal layering before shadows. Depth should come from surface changes, spacing, and 1dp dividers. Shadows are reserved for floating action buttons, bottom sheets, dialogs, and transient overlays where Android users expect elevation.

### Shadow Vocabulary
- **Action Lift** (`0 2dp 6dp rgba(23, 32, 31, 0.14)`): Floating action buttons and raised primary actions only.
- **Sheet Lift** (`0 -4dp 16dp rgba(23, 32, 31, 0.12)`): Modal bottom sheets and elevated panels that slide over content.
- **Dialog Lift** (`0 8dp 24dp rgba(23, 32, 31, 0.18)`): Confirmation dialogs and permission-critical decisions.

### Named Rules
**The Flat-At-Rest Rule.** Content cards, routine rows, and record lists do not need decorative shadows. If everything floats, nothing feels important.

**The Elevation Means Interruption Rule.** Shadows indicate a temporary layer above the task, such as a sheet, dialog, menu, or floating action.

## 5. Components

### Buttons
- **Shape:** Full pill at rest for primary and tonal actions (999dp), with a more grounded pressed shape (12dp) for tactile Material feedback.
- **Primary:** Care Teal fill, white label, 52dp height, 20dp horizontal padding, label typography. Use one primary action per screen region.
- **Hover / Focus:** Use Deep Care Teal for pressed state and a 2dp Care Teal focus ring outside the component. Do not add neon glow.
- **Secondary / Ghost:** Use Quiet Teal Container for tonal buttons and transparent ghost buttons with Care Teal text for low-risk navigation.
- **Loading:** Replace label with an inline progress treatment only when the action blocks the screen. Prefer preserving button width to prevent layout shift.

### Chips
- **Style:** Rounded pills (999dp) with Quiet Teal Container when selected and Clean Page Surface with Quiet Divider when unselected.
- **State:** Selected chips use Care Teal text and a clear selected icon only when the icon improves recognition. Unselected chips stay neutral.
- **Use:** Pet filters, record categories, medication status, food schedule tags, and permission roles.

### Cards / Containers
- **Corner Style:** Gently rounded rectangles (16dp) for meaningful grouped content. Avoid 24dp or larger card radii.
- **Background:** Clean Page Surface on Journal Canvas, or Soft Care Layer for secondary panels.
- **Shadow Strategy:** No default card shadow. Use Quiet Divider or tonal contrast instead.
- **Border:** 1dp Quiet Divider where needed for separation. Never use colored side-stripe borders.
- **Internal Padding:** 16dp for compact cards, 24dp for empty states and pet profile summaries.

### Inputs / Fields
- **Style:** Label above input, rounded 12dp container, Clean Page Surface fill, Quiet Divider outline, 56dp minimum height.
- **Focus:** Care Teal 2dp outline or indicator. The label stays visible above the field.
- **Error / Disabled:** Error uses Record Error Red with direct helper text below. Disabled fields use Soft Care Layer and Muted Care Ink, not low-contrast gray.
- **Validation:** Show inline errors close to the field. Avoid modal validation summaries for normal forms.

### Navigation
- **Bottom Navigation:** Use Material bottom navigation for primary authenticated destinations. Active item uses Care Teal icon and label. Inactive items use Muted Care Ink.
- **Top Bars:** Simple title, back navigation where needed, and one trailing action max unless the screen is explicitly a management surface.
- **Sheets and Dialogs:** Use sheets for progressive tasks and dialogs for destructive or permission-sensitive confirmation. Do not reach for modals before inline editing or sheet flows.

### Care Summary Rows
- **Purpose:** The default pattern for medication, food, and record summaries.
- **Structure:** Pet or record identity first, current status second, next useful action last. Use metadata in Muted Care Ink and semantic chips only when they clarify state.
- **Empty State:** Teach the first action with concise copy and a single primary button. Do not write only "No data".

## 6. Do's and Don'ts

### Do:
- **Do** use familiar Material 3 and Android interaction patterns so care workflows feel predictable.
- **Do** keep Care Teal rare and meaningful: primary action, active navigation, focus, selection.
- **Do** use inline confirmation and clear recovery copy for failed saves, permission problems, and validation errors.
- **Do** use generous touch targets of at least 48dp for all interactive controls.
- **Do** prioritize scan order: pet identity, current care state, next action, then metadata.
- **Do** use skeleton loading for record lists, pet cards, and profile summaries instead of centered spinners when layout is known.

### Don't:
- **Don't** create sterile medical dashboards, harsh hospital-like interfaces, or cold clinical patterns.
- **Don't** use childish or cartoonish pet-app visuals that reduce trust.
- **Don't** make routine care feel like dense admin data entry with cramped tables and technical controls.
- **Don't** use gradient text, neon glows, glassmorphism, or decorative blur cards.
- **Don't** use colored side-stripe borders on cards, callouts, records, or alerts.
- **Don't** pair large soft shadows with 1dp decorative borders on the same card.
- **Don't** over-round cards or inputs past 16dp. Reserve full pills for chips and buttons.
- **Don't** use pure black, low-contrast gray helper text, or inactive states with saturated color.
- **Don't** invent custom form controls, scrollbars, or non-standard modals for visual flavor.
- **Don't** hide health-related changes behind unclear feedback. Every save, failure, delete, and permission change needs a visible result.

---
name: Kinetic Minimal
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#47464f'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#787680'
  outline-variant: '#c8c5d0'
  surface-tint: '#5b598c'
  primary: '#070235'
  on-primary: '#ffffff'
  primary-container: '#1e1b4b'
  on-primary-container: '#8683ba'
  inverse-primary: '#c4c1fb'
  secondary: '#4e45d5'
  on-secondary: '#ffffff'
  secondary-container: '#6860ef'
  on-secondary-container: '#fffbff'
  tertiary: '#000f07'
  on-tertiary: '#ffffff'
  tertiary-container: '#002819'
  on-tertiary-container: '#179c6e'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e3dfff'
  primary-fixed-dim: '#c4c1fb'
  on-primary-fixed: '#181445'
  on-primary-fixed-variant: '#444173'
  secondary-fixed: '#e3dfff'
  secondary-fixed-dim: '#c3c0ff'
  on-secondary-fixed: '#100069'
  on-secondary-fixed-variant: '#372abf'
  tertiary-fixed: '#85f8c4'
  tertiary-fixed-dim: '#68dba9'
  on-tertiary-fixed: '#002114'
  on-tertiary-fixed-variant: '#005137'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '600'
    lineHeight: 44px
    letterSpacing: -0.03em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 34px
    letterSpacing: -0.025em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: -0.015em
  title-sm:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.04em
  metric-display:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1.25rem
  margin-mobile: 1rem
  margin-tablet: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.25rem
---

## Brand & Style
The design system embodies Swiss-inspired functional minimalism tailored for a modern, focused workout logging experience. Its core purpose is to remove friction between an athlete and their session: zero visual clutter, distraction-free typography, and deliberate negative space that lets performance metrics take center stage.

Targeted at disciplined college students and fitness enthusiasts, the tone is understated, architectural, and serious without feeling intimidating. It avoids gamified tropes, neon gradients, and aggressive visual noise. The aesthetic leans on pristine hierarchy, hairline borders, and muted tonal layers, punctuated only by a surgical accent color when an action or state demands absolute clarity.

## Colors
The palette is built around architectural neutrals, warm-tinted paper surfaces, and a precise indigo accent.

- **Background & Canvas**: Pure surfaces utilize `#FAFAF9` (warm off-white) to prevent cold glare under gym lighting, layered over `#F4F4F5` for inset modules and grouped controls.
- **Text & Ink**: Text hierarchies rely on charcoal and deep slate: `#0F172A` for primary headlines and values, `#475569` for body copy, and `#94A3B8` for secondary labels and timestamps.
- **Accents**: 
  - The primary action tone is `#4338CA` (deep electric indigo), used sparingly for primary execution buttons, active set toggles, and completed states.
  - A tertiary accent `#059669` (sleek emerald) provides positive confirmation, personal records (PRs), and rest timer completion.
- **Borders & Dividers**: Hairlines strictly use `#E2E8F0` on light canvases and `#CBD5E1` for hover or focus boundaries.

## Typography
Typographic rhythm follows a strict modernist grid using **Inter**. The hierarchy relies on negative tracking at larger scale to convey precision and engineering rigor.

- Numerical tracking (weights, sets, reps, rest timers) uses tabular numbers (`font-variant-numeric: tabular-nums`) across all metrics to prevent layout shifts during active timing and increment logging.
- `metric-display` is dedicated to workout logging counters, weight indicators, and timer views.
- Labels are set with slightly opened tracking (`+0.02em` to `+0.04em`) to maintain legibility when rendered uppercase or in small column headers (e.g., `SET`, `PREVIOUS`, `KG`, `REPS`).

## Layout & Spacing
The layout model uses a single-column fluid container optimized for handheld mobile interaction, constrained to a maximum width of 480px on larger screens.

- **Mobile Viewports (< 640px)**: The view employs a 16px (`1rem`) outer margin to maximize screen real estate for quick input grids (e.g., set/rep tables). Vertical padding between exercise groupings is fixed at 24px (`space-lg`).
- **Tablet & Desktop Shells**: The content remains centered within an elevated card or neutral canvas shell with 32px (`2rem`) margins.
- **Rhythm**: Element-to-element spacing follows a strict 4px/8px interval. Micro spacing (`space-xs`, `space-sm`) separates unit labels from numerical figures, while `space-md` separates rows within exercise blocks.

## Elevation & Depth
Depth is created through tonal layering and razor-thin borders rather than heavy drop shadows.

- **Layer 0 (Canvas)**: Baseline background color `#FAFAF9`.
- **Layer 1 (Cards & Modules)**: Pure white `#FFFFFF` surface framed with a 1px solid border (`#E2E8F0`). No shadow is cast in resting states.
- **Layer 2 (Floating Elements / Bottom Action Bars)**: Pure white `#FFFFFF` with a 1px top border (`#E2E8F0`) and an ambient, ultra-diffused drop shadow: `0 8px 24px -4px rgba(15, 23, 42, 0.04)`.
- **Active & Pressed States**: Surface tone shifts slightly to `#F8FAFC` on tap, accompanied by an instantaneous 0.99 scale transformation.

## Shapes
Geometry is refined, soft, and balanced:
- Default containers, cards, and input fields utilize `rounded-lg` (12px to 16px) for an approachable, architectural form that feels comfortable in hand.
- Small indicators, tags, and pills utilize full rounded radii (`rounded-full`) for quick visual grouping.
- Inputs, set checkmarks, and data-entry fields share an identical corner radius of 10px to maintain geometric consistency across complex tables.

## Components

### Buttons
- **Primary**: Deep electric indigo (`#4338CA`), text in pure white (`#FFFFFF`), height 48px, radius `rounded-lg` (12px). Typography: `title-sm`. Active state darkens to `#3730A3`.
- **Secondary**: Crisp white background (`#FFFFFF`), 1px solid hairline border (`#E2E8F0`), slate text (`#0F172A`).
- **Ghost/Tertiary**: No background or border; uses `#475569` text with 8px horizontal padding for auxiliary actions like "Cancel" or "Add Warmup Set".

### Input Fields & Steppers
- Numeric fields for Weight and Reps feature an understated `#F8FAFC` fill with a 1px hairline border (`#E2E8F0`).
- Text is centered, set in `Inter 16px SemiBold` tabular figures to eliminate zoom behavior on iOS web views.
- Focused states transition the border to `#4338CA` with zero outer glow.

### Workout Set Rows & Tables
- Standard sets are arranged in a horizontal grid (`Set #`, `Previous`, `Weight (kg)`, `Reps`, `Checkmark`).
- Hairline dividers (`1px solid #F1F5F9`) separate rows within the white parent card.
- Completed sets transition row background to a faint wash (`#F0FDF4`) with an emerald-tinted check button.

### Chips & Badges
- Used for muscle group tags (e.g., "Quads", "Chest") or equipment requirements.
- Background `#F1F5F9`, text `#475569`, border-free, padding 4px 10px, radius `rounded-full`, typography `label-sm`.

### Checkboxes (Set Loggers)
- Square-round toggle button (32x32px, radius 8px).
- Unchecked: `#F1F5F9` background with no border.
- Checked: `#059669` (emerald green) background with a centered white check icon.

### Rest Timer & Floating Sheet
- Anchored to the viewport bottom.
- Background `#FFFFFF` with hairline border top (`#E2E8F0`).
- Prominent countdown using `metric-display` typography alongside quick-add chip controls (`+30s`, `+60s`).
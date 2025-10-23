# Changelog

## [2.0.1] - 2025-10-22

### Fixes and polish

- Block placement trigger is now precise: only fires when a block is actually placed.
  - No more false triggers when right-clicking chests, crafting tables, etc. while holding a block.
- All triggers consistently respect Trigger Chance.
  - Block placement no longer bypasses chance; it now uses the same gate as other triggers.
- Trigger Chance defaults to 0.50 (50%).
  - Existing configs still on the old default (0.10) are auto-migrated to 0.50 on load.
  - Value is clamped to [0.0, 1.0]. 0.0 = never, 1.0 = always.
- You can now save/quit reliably during an active effect.
  - The effect is cleanly ended when the game is paused so the pause menu actions work.
- Skull overlay size is normalized across GUI scales to match the look at GUI scale 3.
  - Added a “Skull Size (scale)” slider (0.1–2.0, default 0.4) under Visual Effects.
- Promo & Support tweaks:
  - TikTok link color set to pink (FF69B4) and GitHub link added.

### Internal

- Minor refactors and config normalization improvements.

## [2.0.0] - 2025-10-22

### 🎨 Visual Stuff

#### Custom Skulls
- You can finally add your own skull images! Just drop PNG files into `.minecraft/config/phonkedit/images/`
  - Images get auto-resized to 256×256 while keeping the aspect ratio
  - Works with any size image, we'll center it for you
  - skull20.png is special - it shows up when PHONK6 plays
  - All the default skulls get exported to that folder so you can edit them
- **Better Skull Rendering**:
  - Fixed the annoying clipping issue where skulls got cut off at the edges
  - Made them smaller (40% of original size) so they actually fit on screen
  - Everything scales properly now
- **Way Better Drop Shadow**:
  - Shadow is tighter and actually looks good now
  - Smooth fadeout instead of harsh edges
  - More subtle opacity so it doesn't look like MS Paint
  - Optimized so it doesn't tank your FPS
- **Motion Blur**:
  - Blur follows the shake effect
  - You can adjust how intense it is
  - Smooth transitions that don't look janky
  - Only happens during the initial shake
- **Screen Shake**:
  - Shake intensity is configurable now
  - Lasts 0.5 seconds with smooth easing
  - Shakes both the screen and skulls
  - Adds some actual impact to the effect

#### Freeze Frame
- **Grayscale filter** - makes the frozen screen black and white (optional)
- **Dark overlay** - dims the background so the skulls pop more
- **Cinematic bars** - adds those vertical black bars for that 9:16 phone video vibe
- **Better capture** - uses proper screenshot methods so it actually works

### 🎵 Audio

#### Music System
- **12 different phonk tracks** instead of just a few
- **Random selection** - picks a different track each time
- **Pitch shifter**:
  - Randomly adjusts pitch between 0.95-1.05 by default
  - Keeps it between 0.5-2.0 so it doesn't sound terrible
  - Adds variety without being too noticeable
- **PHONK6 is special** - triggers the skull20.png if you have a custom one
- **Auto-stop**:
  - Effect ends when the track finishes
  - Has a backup timer just in case

### ⚙️ Settings

You can configure pretty much everything now:

**What You Can Toggle**:
- Grayscale filter on/off
- Dark overlay on/off
- Cinematic bars on/off
- Skull graphics on/off
- Skull shake on/off
- Motion blur on/off

**Fine Tuning**:
- Blur intensity (0-5, how spread out it is)
- Blur ease (0.1-5, how fast it fades)
- Shake intensity (1.0 is normal, higher = more crazy)

**Audio**:
- Min/max pitch range (0.5-2.0)

**When It Triggers**:
- Breaking blocks ✓
- Placing blocks ✓
- Hitting entities ✓
- Taking damage ✓
- Low health (under 3 hearts) ✓
- Being in the air for 1.3+ seconds ✓
- Trigger chance (default 10%)

**Control Settings**:
- Lock mouse during effect
- Lock camera during effect
- Pause world during effect

All settings save to `.minecraft/config/phonkedit.json` and you can edit them in-game if you have Mod Menu installed.

### 🎮 How It Works

#### Triggers
- Can trigger from 6 different actions at once
- 3 second cooldown after joining a world (so it doesn't spam on load)
- 0.3 second delay between trigger and effect (feels more natural)
- Won't trigger if already active
- 10% chance by default (configurable)

#### Movement Lock
- Completely freezes your character
- Saves your velocity and sprint state
- Gives it back when the effect ends
- All inputs disabled while frozen

#### Camera Lock
- Locks your view direction
- Stays locked for the whole effect
- Returns to normal control after

### 🐛 Bug Fixes

- Fixed crash on Minecraft 1.21.1
- Fixed skulls getting cut off at screen edges
- Fixed custom images not loading properly
- Fixed memory leaks with textures
- Fixed multiple effects playing at once
- Shadow renders correctly now
- Blur looks smooth instead of weird
- Screen shake works properly
- Cinematic bars calculate correctly for all screen sizes
- Music doesn't keep playing after effect ends
- Pitch stays in valid range
- Config values automatically get clamped to valid ranges
- Swapped min/max values get corrected automatically
- Everything saves properly



**Note**: Version 2.0.0 is basically a complete rewrite with way more features and actually good visuals.

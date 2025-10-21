# Phonk Edit - New Implementation

## Current Status: **In Progress**

### What We Know About the Renderer Library (v2.1.4)

**Package Structure:**
- Main package: `me.x150.renderer`
- Key classes found:
  - `me.x150.renderer.util.RenderUtils` - Utility methods for rendering
  - `me.x150.renderer.util.Color` - Color utilities
  - `me.x150.renderer.render.ExtendedDrawContext` - Extended drawing context
  - `me.x150.renderer.render.SimpleGuiRenderState` - GUI rendering state
  - `me.x150.renderer.event.RenderEvents` - Event system
  - `me.x150.renderer.shader.Shaders` - Shader utilities

### Problem

The Renderer library source code shows **obfuscated class names** (e.g., `class_243` instead of `Vec3d`), which means:
1. The library was compiled against obfuscated Minecraft code
2. The sources JAR isn't properly remapped for developers
3. We need to either:
   - Use the library's API blindly (trial and error)
   - Find proper documentation/examples
   - Stick with custom OpenGL code

### Next Steps

**Option A:** Continue with custom OpenGL (proven to work)
- Copy the working overlay code from the original mod
- Optimize and polish it
- Add Renderer later for future enhancements only

**Option B:** Research and implement Renderer properly
- Find the GitHub repo documentation
- Look for example mods using this library
- Reverse-engineer the API from the compiled classes

**Option C:** Use a different, better-documented rendering library
- Fabric Rendering API v1 (built into Fabric API)
- Other community rendering libraries

### Current Build Issue

The mod doesn't compile due to Fabric API changes. Need to either:
1. Use an older Fabric API version that's compatible
2. Update the code to work with the newer API

## Recommendation

I recommend **Option A** - use the proven custom OpenGL solution. The Renderer library is:
- Poorly documented (obfuscated sources)
- Potentially incompatible with our Minecraft version
- Overkill for our simple overlay needs

The custom GL code in the original mod is actually **superior** for this use case.

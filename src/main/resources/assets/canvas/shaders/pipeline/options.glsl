/******************************************************
  canvas:shaders/pipeline/options.glsl
******************************************************/

#define DIFFUSE_MODE_NORMAL 0
#define DIFFUSE_MODE_SKY_ONLY 1
#define DIFFUSE_MODE_NONE 2

#define DIFFUSE_SHADING_MODE DIFFUSE_MODE_NORMAL

#define HANDHELD_LIGHT_RADIUS 0

// define if lighting should be noised to prevent mach banding
// will only be enabled if smooth light is also enabled
//#define ENABLE_LIGHT_NOISE

// These won't go here if they are re-enabled
//#ifndef VANILLA_LIGHTING
//uniform sampler2D frxs_dither;
//uniform sampler2D frxs_hdLightmap;
//#endif

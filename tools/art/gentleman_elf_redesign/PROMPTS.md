# 概念图生成提示词

生成方式：Codex 内置 `image_gen`，分类为 `stylized-concept`。本地图片仅作为身份、比例和动作参考；产物用于后续像素重绘。

## 三视图

```text
Use case: stylized-concept
Asset type: production character turnaround reference for a 32x32 pixel-art dungeon boss
Primary request: create a clean three-view turnaround of the exact Fairy Gentleman character shown in the references, for rebuilding a game sprite. Show front view, right-facing side view, and back view as three separated full-body figures.
Input images: Image 1 is the identity and color anchor; Image 2 is the right-facing locomotion/proportion anchor; Image 3 is the collapsed/death anatomy anchor.
Scene/backdrop: flat neutral medium-gray studio background, no scenery.
Subject: grotesque yet charming obese turquoise-green gelatinous fairy gentleman; tiny white top hat, very long drooping elf ears, black necktie with white collar, four angular translucent cyan insect wings, massive horizontal mouth across the belly with square teeth and a long tongue, small upper face with closed smug eyes, oversized dripping arms, tiny black lower legs. Hold a small green goblet only in the front view; hands relaxed in side/back views.
Style/medium: polished 2D game character concept art, crisp hard-edged cel shading, highly readable silhouette, simplified shapes suitable for 32x32 pixel translation, top-left lighting.
Composition/framing: three equal figures aligned on one baseline with generous separation; orthographic front, exact right-facing profile, exact back; consistent scale and anatomy.
Color palette: limited turquoise/teal/near-black/icy-cyan/white palette matching the references.
Constraints: preserve character identity and asymmetrical anatomy; make tie, top hat, belly mouth, ears, goblet and four wings unmistakable; no labels, no text, no logo, no UI, no watermark; no extra characters; no perspective distortion; no anti-aliased glow.
Avoid: humanoid elf body, thin body, conventional handsome gentleman, green clothing, weapons, extra limbs, extra wings, photorealism, 3D render.
```

## 动作概念图

```text
Use case: stylized-concept
Asset type: production action-pose reference sheet for a 32x32 pixel-art dungeon boss
Primary request: create a clean action pose sheet for the exact Fairy Gentleman identity in Image 1, informed by the original movement/attack/leap/death references. Every active pose faces right. Arrange ten separated, equally scaled full-body key poses in a strict 5-column by 2-row grid: idle relaxed, idle breathing high, walk contact, walk passing, toast/cast raising goblet, attack anticipation with huge arm pulled back, attack impact with arm/body lunging right, airborne belly-first leap to the right, hurt recoil, collapsed death/core pose.
Input images: Image 1 is the approved turnaround and identity anchor. Images 2-5 are original-game pose/anatomy references only.
Scene/backdrop: flat neutral medium-gray studio background, no floor scenery.
Subject: grotesque charming obese turquoise gelatinous Fairy Gentleman with tiny white top hat, very long drooping ears, black tie and white collar, four cyan crystalline wings, belly mouth and tongue, huge dripping arms, tiny dark legs, small green goblet.
Style/medium: polished 2D game character animation concept, crisp hard-edged cel shading, simplified blocky masses suitable for translation to 32x32 pixels, top-left lighting.
Composition/framing: ten discrete silhouettes, no overlap, same baseline within each row, generous gutters, every active pose faces right; exaggerate action silhouettes and center of gravity.
Color palette: limited turquoise, teal shadow, mint highlight, near-black, cyan-white.
Constraints: preserve exact identity and body proportions across every pose; tie and ears must remain clearly visible; movement is a heavy gelatinous bounce, not human walking; attack impact and leap must show obvious rightward direction; no labels, no text, no logo, no UI, no watermark.
Avoid: extra characters, humanoid anatomy, slim body, weapons, inconsistent wing counts, front-facing active poses, motion blur, photorealism, 3D render.
```

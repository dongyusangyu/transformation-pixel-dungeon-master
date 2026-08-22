# 绅士精灵素材重绘对比

本目录保存《边狱公司》异想体 Fairy Gentleman（F-01-11-12）在高塔 Boss 中使用的两条 32×32 像素化实验路线。当前游戏素材没有被覆盖，两套结果均保持与 `GentlemanElfSprite` 一致的 25 帧、800×32 横向帧表。

## 资料依据

- 本地身份基准：`D:\桌面\绅士精灵设计资料\绅士精灵原始形象.jpg`
- 本地动作基准：移动、两组攻击、飞跃、死亡截图
- 联网角色核对：[Fairy Gentleman](https://limbuscompany.wiki.gg/wiki/Fairy_Gentleman)
- 联网战斗与动作核对：[Fairy Gentleman/Enemy](https://limbuscompany.wiki.gg/wiki/Fairy_Gentleman/Enemy)
- 联网原作素材目录：[Abnormality Battle Sprites](https://limbuscompany.wiki.gg/wiki/Category:Abnormality_Battle_Sprites)

角色身份锚点固定为：青绿色胶质肥硕躯体、细长下垂耳、小白礼帽、黑领带与白领、腹部巨口和舌头、酒杯、四片晶体感透明翼。所有战斗动作默认朝右。

## 概念图

- `concepts/gentleman_elf_turnaround.png`：正面、右侧面、背面三视图
- `concepts/gentleman_elf_action_sheet.png`：待机、移动、敬酒、攻击、飞跃、受击、死亡动作参考

概念图由内置图像生成工具生成；完整提示词保存在 `PROMPTS.md`。

## 路线 A：池化压缩后修复

输出：`outputs/gentleman_elf_route_a_pooled.png`

1. 将动作概念图按 5×2 姿势网格切分。
2. 去除中性灰背景并保留主要连通轮廓。
3. 使用 BOX 面积池化压入 32×32 单帧。
4. 映射至固定 12 色以内的色板。
5. 清除半透明边缘、重建 1px 外轮廓，并恢复礼帽、领口和领带等小尺寸身份特征。

优点是胶质高光、躯体不规则性和原概念姿势保留较多。缺点是大图细节压入 32×32 后仍有局部噪点，帧与帧之间的内部色块会轻微跳动。

## 路线 B：依据概念图重新像素绘制

输出：`outputs/gentleman_elf_route_b_handdrawn.png`

该路线不采样概念图像素。脚本只读取设计结论，以 11 色限制色板、1px 轮廓和手工多边形/像素块从零构造待机、移动、攻击、施法、冲刺、飞跃、受击、死亡关键姿势。

优点是 1× 下轮廓最清楚，朝向、腹部巨口、领带、礼帽和动作阶段稳定，后续逐帧修改也最容易。缺点是胶质高光层次比路线 A 少，近距离放大时更偏符号化。

## 推荐结论

推荐路线 B 作为实际游戏素材基础，路线 A 作为色彩与体积参考。

| 对比项 | 路线 A | 路线 B |
|---|---:|---:|
| 原作体积与胶质细节 | 更好 | 中等 |
| 1× 轮廓辨识 | 中等 | 更好 |
| 动作方向与阶段区分 | 良好 | 更好 |
| 帧间一致性 | 中等 | 更好 |
| 后续维护和定点修改 | 较难 | 更容易 |
| 与项目硬边像素风格贴合 | 良好 | 更好 |

Boss 在地图中的显示尺寸很小，轮廓和动作信息的优先级高于局部高光。因此路线 B 的可玩性价值更高；若正式替换，可在 B 的躯干上选择性吸收 A 的 2～3 处浅绿高光，而不引入 A 的细碎色块。

## 预览与验证

- `outputs/gentleman_elf_route_a_preview_5x.png`
- `outputs/gentleman_elf_route_b_preview_5x.png`
- `outputs/gentleman_elf_route_a_animation.gif`
- `outputs/gentleman_elf_route_b_animation.gif`
- `outputs/gentleman_elf_route_comparison.png`
- `outputs/validation.json`

重新生成与验证：

```powershell
python tools\art\gentleman_elf_redesign\build_gentleman_elf_variants.py
```

验证会检查 800×32 尺寸、25 个非空帧、纯 0/255 Alpha、最多 16 色、至少 20 种独立轮廓，以及前 20 帧中领带的最低可读像素数。

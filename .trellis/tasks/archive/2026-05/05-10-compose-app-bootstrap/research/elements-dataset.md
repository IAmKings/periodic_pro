# Research: 化学元素静态数据集（118 元素 JSON / CSV）调研

- **Query**: 为 Android Compose 周期表 App 选择可商用、字段完整、社区维护良好的 118 元素静态数据集
- **Scope**: external（开源社区 + 官方数据源）
- **Date**: 2026-05-10

---

## 1. 候选数据源

### 1.1 Bowserinator / Periodic-Table-JSON （社区事实标准）

- 仓库：https://github.com/Bowserinator/Periodic-Table-JSON
- ⭐ Stars：约 712（截至最近一次 push 2024-03-07）
- 主要维护语言：Python（脚本）；数据为 JSON / CSV
- License：**CC BY-SA 3.0**（来源 Wikipedia；GitHub 显示为 "Other / NOASSERTION"）
- 字段覆盖：`number, symbol, name, appearance, atomic_mass, boil, melt, category, density, discovered_by, named_by, period, group, phase, source(wiki url), summary, xpos/ypos, shells[], electron_configuration, electron_configuration_semantic, electron_affinity, electronegativity_pauling, ionization_energies[], cpk-hex, block, bohr_model_image, bohr_model_3d, spectral_img, image{title,url,attribution}` —— 涵盖全部需求字段。
- 提供格式：`PeriodicTableJSON.json`（数组）+ `periodic-table-lookup.json`（按符号索引）+ `PeriodicTableCSV.csv`
- 大小：JSON 文件 ≈ **250 KB**（含 summary + 图片 URL + Wiki source；如果剔除 image / bohr_model / spectral_img 可压到 ~80–100 KB）
- 元素覆盖：1–118 全；超重元素 95–118 部分字段可能为 `null`
- 中文：**无**，仅英文
- 备注：被大量第三方应用复用（含 SemiCode-Analyzer、boraoksuzoglu/periodic-table-api 等），稳定性高

### 1.2 PubChem Periodic Table（官方权威）

- 来源：https://pubchem.ncbi.nlm.nih.gov/periodic-table/
- 直接下载：`https://pubchem.ncbi.nlm.nih.gov/rest/pug/periodictable/CSV` （CSV，118 行 × 17 列）
- License：**美国政府公共领域作品**（NLM/NCBI），通常视为 public domain；少数字段可能继承上游数据源 license（详见 https://www.nlm.nih.gov/copyright.html）。商用友好。
- 字段覆盖：AtomicNumber, Symbol, Name, AtomicMass, CPKHexColor, ElectronConfiguration, Electronegativity, AtomicRadius, IonizationEnergy, ElectronAffinity, OxidationStates, StandardState, MeltingPoint, BoilingPoint, Density, GroupBlock, YearDiscovered（17 列，**无 summary、无 discovered_by 详细人名、无电子壳层数组**）
- 大小：CSV ≈ **15–20 KB**，转 JSON 后 ≈ **30–40 KB**
- 中文：**无**官方中文版；社区有 `PubChemElements_all_cn.csv`（豆瓣用户 HomeAnimator 2021-07 整理，添加 `Cname, Pinyin` 列），但 license 不明、维护停滞
- 适合作为权威「字段值」校对源

### 1.3 sjf8203 / periodic-table-data

- 仓库：https://github.com/sjf8203/periodic-table-data
- ⭐ Stars：0（个人项目）
- License：**MIT** ✅
- 数据来源：基于 PubChemElements_all.csv 转 JSON（2023-09-25 抓取）
- 字段：atomicNumber, symbol, name, atomicMass, cPKHexColor, electronConfiguration, electronegativity, atomicRadius, ionizationEnergy, electronAffinity, oxidationStates, standardState, meltingPoint, boilingPoint, density, groupBlock, yearDiscovered（与 PubChem 列一致）
- 大小：JSON ≈ 30–40 KB
- 中文：无
- 备注：本质是「PubChem CSV → MIT JSON」的小封装，适合直接用 JSON 又想要 MIT license 的项目

### 1.4 AlexGustafsson / molecular-data

- 仓库：https://github.com/AlexGustafsson/molecular-data
- License：MIT（README 显示）
- 多格式：JSON / CSV / YAML / XML / Plist
- 字段：number, symbol, name, mass, cpkHexColor, electronConfiguration, electronNegativity, radius, ionRadius, vanDelWaalsRadius, ionizationEnergy, electronAffinity, oxidationStates[], standardState, bondingType, meltingPoint, boilingPoint, density, family, yearDiscovered
- **多语言**：附带 `elementsLocale` 文件，将元素名翻译到 ~30 种语言（含中文 `zh`）。这是少数原生支持中文的开源数据集。
- 大小：核心 elements JSON ≈ 40–60 KB；本地化文件另算
- 风险：维护活跃度一般，最近更新较旧；electronConfiguration 字段在示例里是数字（可能是简化值，需校对）

### 1.5 shangzhenyang / periodic-table（npm 包，已 archived）

- 仓库：https://github.com/shangzhenyang/periodic-table（仓库已 archived）
- ⭐ Stars：19；License：**MIT** ✅
- **特色：原生中文支持**（`name_chs` 简体 / `name_cht` 繁体），还能通过中文名查找元素与化合物
- 字段较少：`mass, mole, name, name_chs, name_cht, number, symbol`（**只有基础字段**，无密度、熔沸点、电负性等）
- 适合做「中文映射表」，不能单独承担 Detail 屏的全部字段

### 1.6 komed3 / periodic-table

- 仓库：https://github.com/komed3/periodic-table
- License：**MIT** ✅
- 提供 `elements.json`（原子/物理/化学属性）、`nuclides.json`（3000+ 核素）、`spectrum.json`（base64 谱线图）
- 字段全面，结构现代；中文不直接支持
- 适合需要「核素 + 谱线」扩展的项目

### 1.7 sweaver2112 / periodic-table-data-complete

- 仓库：https://github.com/sweaver2112/periodic-table-data-complete
- 字段最丰富（70+ 列）：`crystal_structure, magnetic_susceptibility, neutron_cross_section, lattice_constants, half_life, isotopes_*, modulus/{bulk,shear,young}, refractive_index, speed_of_sound, superconducting_point, curie_point, neel_point` 等等
- 数据非扁平（嵌套对象 + Wikipedia HTML 段落原文）
- 体积大（含 Wikipedia 第一段 HTML），不适合 Android assets 直接打包
- License：需进一步确认（README 未直接列出）
- 适合做「研究/补全字段」参考源

### 1.8 Bluegrams / periodic-table-data

- 仓库：https://github.com/Bluegrams/periodic-table-data（`Periodica.Data/Data/ElementData.csv`）
- 字段：含 `HeatCapacity, HeatOfFusion, HeatOfVaporization, AbundanceCrust, AbundanceUniverse, ThermalConductivity, Radioactive` 等扩展字段
- 主要是 .NET 生态，但 CSV 文件可独立使用

### 1.9 Kaggle 数据集

- `mexwell/periodic-table-of-elements`、`berkayalan/chemical-periodic-table-elements` 等
- 一般是 PubChem 派生 / Wikipedia 派生
- License 因数据集而异（多为 CC0 或 CC-BY），需逐一确认；维护频率一般低于 GitHub 项目

---

## 2. 对比表

| 来源 | Stars | License | 字段覆盖（vs 需求 12 项） | 中文 | 体积（JSON） | 维护 |
|---|---|---|---|---|---|---|
| **Bowserinator** | 712 | CC BY-SA 3.0 | 12/12 ✅ + summary + 图片 | ❌ | ~250 KB（含图）/~90 KB（裁剪） | 活跃（2024-03） |
| PubChem CSV | — | Public Domain | 12/12（无 summary/壳层） | ❌（社区有 cn 版） | ~30–40 KB | 官方持续更新 |
| sjf8203 | 0 | **MIT** ✅ | 同 PubChem 12/12 | ❌ | ~30–40 KB | 2023 |
| AlexGustafsson molecular-data | 中 | **MIT** ✅ | 12/12 | ✅（30 语言含 zh） | ~40–60 KB + 本地化 | 较旧 |
| shangzhenyang | 19 | **MIT** ✅ | 仅 4 项基础 | ✅（chs/cht） | 极小 | archived |
| komed3 | 中 | **MIT** ✅ | 12/12 + 核素 + 谱线 | ❌ | ~100 KB | 活跃 |
| sweaver2112 | 中 | 需确认 | 70+ 字段 | ❌ | 很大（含 HTML） | — |
| Bluegrams | 中 | 见仓库 | 扩展字段丰富 | ❌ | 中 | — |

需求 12 项字段 = 编号、符号、名称、原子量、分类、电子排布、密度、熔点、沸点、原子半径、电负性、第一电离能（+发现年份/发现者扩展项）。

---

## 3. License 兼容性总结

| 类型 | 商用 | 是否要求 ShareAlike | 备注 |
|---|---|---|---|
| MIT / Apache 2.0 (sjf8203, molecular-data, komed3, shangzhen) | ✅ | ❌ | 最适合闭源商用 App |
| CC0 / Public Domain (PubChem) | ✅ | ❌ | 需在 About 页面建议署名 PubChem 作为礼貌 |
| **CC BY-SA 3.0 (Bowserinator)** | ✅ | ⚠️ **是** | 商用允许，但若**修改并再分发数据**需以同等许可发布；纯打包到 App 内只读使用一般可接受，**必须保留出处与 Wikipedia 链接**。如担心 ShareAlike 传染性，慎选 |
| 不明 (sweaver2112, 部分 Kaggle) | ⚠️ | ⚠️ | 使用前需逐一确认 |

⚠️ Bowserinator 的 CC BY-SA 3.0 是一个**潜在合规风险**：
- 大多数 App 内部使用 + 署名做法被实践认为合规
- 但严格解读下，App 中分发的修改版数据集本身可能也要遵守 SA 条款
- 如要规避，使用 PubChem / sjf8203 / molecular-data 更稳妥

---

## 4. 数据集大小对启动期的影响估算

| 方案 | 体积 | 解析时间（中端机, Moshi/Kotlinx）|
|---|---|---|
| Bowserinator 全字段 | 250 KB | 30–60 ms |
| Bowserinator 裁剪（去 image / bohr / spectral / summary）| ~90 KB | 15–30 ms |
| PubChem CSV → JSON | 30–40 KB | <15 ms |
| molecular-data | 40–60 KB | <20 ms |

对一个 Compose 周期表 App，**< 100 KB** 都可在 IO 协程一次性 Gson/Moshi 解析完毕，对启动 P95 几乎无感。

---

## 5. 中文 / i18n 支持

只有两个候选原生带中文：

1. **shangzhenyang/periodic-table**（MIT）：`name_chs` + `name_cht`，但**只有名字**，无原子量等扩展属性
2. **AlexGustafsson/molecular-data**（MIT）：`elementsLocale.zh`，仅有元素名翻译

社区补充：
- 中国化学会 2018 版 IUPAC 中文周期表（PDF / 图片，非结构化数据，仅作为校对）：https://www.chemsoc.org.cn/a2613.html
- 豆瓣 `PubChemElements_all_cn.csv`（添加 `Cname, Pinyin` 列），license 不明
- 113/115/117/118 号元素中文官方命名：鉨(Nh)、镆(Mc)、䀅(Ts)、鿫(Og) — 由全国科技名词委员会 2017-05-09 发布

i18n 实现策略建议：
- 主 elements.json 使用 PubChem / Bowserinator 英文数据（保证字段完整性）
- 单独维护 `elements_zh.json` 仅包含 `{number, name_zh, pinyin}`，引用：molecular-data 的 zh locale + 中国化学会官方命名校对
- 在 Compose 中通过 `LocalConfiguration.locale` 决定显示英文 / 中文名

---

## 6. 推荐方案

### 首选：**Bowserinator/Periodic-Table-JSON（裁剪版） + 中文映射表**

理由：
1. 字段最全（12/12 需求字段全覆盖 + 额外的 `appearance` `summary` `cpk-hex` `shells` 等利于 UI 增强）
2. 社区采用度极高，数值经过多年校对，「事实标准」
3. 体积可接受：移除 `image`、`bohr_model_image`、`bohr_model_3d`、`spectral_img`、`summary` 后约 90 KB（如果 Detail 屏要 summary 文本，可保留 summary，约 150 KB，仍可接受）
4. 同仓库提供 `periodic-table-lookup.json` 按符号索引，方便 O(1) 查找

落地步骤：
1. 下载 `PeriodicTableJSON.json` → `app/src/main/assets/elements.json`
2. 写脚本（或人工）剔除不需要的图片字段
3. 自建 `app/src/main/assets/elements_zh.json`：`[{number:1, name_zh:"氢", pinyin:"qīng"}, ...]`，118 项小映射
4. App 启动时由 Repository 合并两份 JSON → `Element` data class
5. **合规**：在「关于」页面署名 *Data from Bowserinator/Periodic-Table-JSON, sourced from Wikipedia, CC BY-SA 3.0*，并附原仓库链接

### 备选 A：**sjf8203/periodic-table-data（PubChem-based, MIT）**

如果项目对 CC BY-SA 3.0 的 ShareAlike 条款有顾虑，选这个：
- 体积更小（~35 KB）
- MIT license，无传染性
- 字段够用（12/12，缺 summary/壳层数组）
- 缺点：仅 0 star，作者非长期维护，建议 fork 一份到自己仓库快照

### 备选 B：**PubChem CSV 直取**

最权威但需要少量预处理（CSV→JSON），适合追求「官方数据源」标签的项目；中文需自建。

### 不推荐
- **shangzhenyang**：字段过少，单独无法满足 Detail 屏
- **sweaver2112**：体积过大，含 HTML 不适合移动端
- **Kaggle 通用集**：license 杂乱，溯源困难

---

## Caveats / Not Found

- 没有找到一份「**字段完整 + 中文齐全 + MIT 商用 + 持续维护**」一站式可用的开源数据集。中文部分必然需要自建小映射文件。
- Bowserinator 的 CC BY-SA 3.0 对「打包到闭源商用 App 是否需要将整个 App 以 SA 条款释放」业界没有明确判例，主流实践是仅对**数据**保留署名 + 链接即可。如有强合规需求，请咨询法务，或直接选 PubChem/MIT 路线。
- 超重元素（Z=104–118）很多物性字段在所有数据集中都为 `null`（实验数据缺失），UI 渲染需做空值兜底（显示「—」或「未测定」）。
- molecular-data 的 `electronConfiguration` 在公开示例里是整数而非字符串「[He] 2s2 2p5」格式，使用前需要核对真实 JSON 字段。
- 113/115/117/118 中文命名（鉨/镆/䀅/鿫）含生僻字，部分 Android 默认字体可能缺字，需测试或附带 Noto Sans CJK 字体。

## External References

- Bowserinator: https://github.com/Bowserinator/Periodic-Table-JSON
- Bowserinator LICENSE: https://github.com/Bowserinator/Periodic-Table-JSON/blob/master/LICENSE.md
- Bowserinator Raw JSON: https://raw.githubusercontent.com/Bowserinator/Periodic-Table-JSON/master/PeriodicTableJSON.json
- PubChem Periodic Table: https://pubchem.ncbi.nlm.nih.gov/periodic-table/
- PubChem CSV REST: https://pubchem.ncbi.nlm.nih.gov/rest/pug/periodictable/CSV
- IUPAC 教程（PubChem 元素数据）: https://iupac.github.io/WFChemCookbook/datasources/pubchem_ptable.html
- sjf8203/periodic-table-data: https://github.com/sjf8203/periodic-table-data
- AlexGustafsson/molecular-data: https://github.com/AlexGustafsson/molecular-data
- shangzhenyang/periodic-table: https://github.com/shangzhenyang/periodic-table
- komed3/periodic-table: https://github.com/komed3/periodic-table
- sweaver2112/periodic-table-data-complete: https://github.com/sweaver2112/periodic-table-data-complete
- 中国化学会 IUPAC 中文版周期表: https://www.chemsoc.org.cn/a2613.html
- PubChemElements_all_cn.csv（豆瓣社区译版，license 不明）: https://m.douban.com/note/808951786/

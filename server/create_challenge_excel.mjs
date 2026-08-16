import fs from "node:fs/promises";
import { SpreadsheetFile, Workbook } from "@oai/artifact-tool";

const inputPath = "D:/STUDY/Dungeon/transformation-pixel-dungeon-master/server/challenge_analysis.json";
const outputDir = "D:/STUDY/test";
const outputPath = `${outputDir}/全服挑战胜率与相关性分析_含真实总胜率.xlsx`;
const previewPath = `${outputDir}/全服挑战胜率与相关性分析_含真实总胜率_预览.png`;

const analysis = JSON.parse(await fs.readFile(inputPath, "utf8"));
const cumulative = JSON.parse(await fs.readFile("D:/STUDY/Dungeon/transformation-pixel-dungeon-master/server/cumulative_win_rate_analysis.json", "utf8"));
const standard = analysis.scopes["标准可比对局"];
const full = analysis.scopes["全量排行榜记录"];
const definitions = analysis.metadata.challenge_definitions;
const labelByName = Object.fromEntries(definitions.map((item) => [item.name, item.label]));

const workbook = Workbook.create();
const overview = workbook.worksheets.add("概览");
const typeSheet = workbook.worksheets.add("挑战类型");
const countSheet = workbook.worksheets.add("挑战数量");
const pairSheet = workbook.worksheets.add("挑战相关性");
const comboSheet = workbook.worksheets.add("挑战组合");
const qualitySheet = workbook.worksheets.add("数据质量");
const cumulativeSheet = workbook.worksheets.add("真实总胜率");

const colors = {
  ink: "#241A35",
  purple: "#6D28D9",
  violet: "#8B5CF6",
  lavender: "#EDE9FE",
  mint: "#D1FAE5",
  amber: "#FEF3C7",
  rose: "#FFE4E6",
  line: "#DDD6FE",
  muted: "#5B5568",
  white: "#FFFFFF",
};

function styleTitle(sheet, range, title) {
  sheet.getRange(range).merge();
  sheet.getRange(range).values = [[title]];
  sheet.getRange(range).format = {
    fill: colors.ink,
    font: { bold: true, color: colors.white, size: 16 },
    horizontalAlignment: "left",
    verticalAlignment: "center",
  };
  sheet.getRange(range).format.rowHeight = 30;
}

function styleHeader(sheet, range) {
  sheet.getRange(range).format = {
    fill: colors.purple,
    font: { bold: true, color: colors.white },
    horizontalAlignment: "center",
    verticalAlignment: "center",
    wrapText: true,
    borders: { preset: "all", style: "thin", color: colors.line },
  };
}

function styleBody(sheet, range) {
  sheet.getRange(range).format = {
    font: { color: colors.ink },
    verticalAlignment: "center",
    borders: { insideHorizontal: { style: "thin", color: colors.line } },
  };
}

function setWidths(sheet, widths) {
  for (const [column, width] of Object.entries(widths)) {
    sheet.getRange(`${column}:${column}`).format.columnWidth = width;
  }
}

function addTable(sheet, range, name) {
  const table = sheet.tables.add(range, true, name);
  table.style = "TableStyleMedium4";
  table.showFilterButton = true;
  return table;
}

function applyRateFormatting(sheet, range) {
  sheet.getRange(range).format.numberFormat = "0.0%";
  sheet.getRange(range).conditionalFormats.add("colorScale", {
    colors: ["#FEE2E2", "#FEF3C7", "#DCFCE7"],
    thresholds: ["min", "50%", "max"],
  });
}

function applyNumberFormatting(sheet, ranges) {
  for (const range of ranges) sheet.getRange(range).format.numberFormat = "#,##0";
}

styleTitle(overview, "A1:J1", "全服挑战胜率与挑战组合分析");
overview.getRange("A2:J2").merge();
overview.getRange("A2:J2").values = [["数据来源：服务器玩家云端 global_data.rankings；主结论使用标准可比对局，排除黑名单、自定义种子、每日模式与 TEST_MODE。"]];
overview.getRange("A2:J2").format = { font: { color: colors.muted, italic: true }, wrapText: true };

overview.getRange("A4:J4").values = [["指标", "标准可比对局", "全量排行榜记录", "说明", "", "", "", "", "", ""]];
styleHeader(overview, "A4:D4");
const overviewRows = [
  ["对局数", standard.summary["对局数"], full.summary["对局数"], "全量包含特殊模式；标准口径用于可比胜率"],
  ["胜局数", standard.summary["胜局数"], full.summary["胜局数"], "排行榜 win=true"],
  ["负局数", standard.summary["负局数"], full.summary["负局数"], "对局数减胜局数"],
  ["胜率", null, null, "胜局数 / 对局数"],
  ["玩家总数", analysis.quality["玩家总数"], analysis.quality["玩家总数"], "服务器玩家记录数"],
  ["有排行榜玩家数", analysis.quality["有排行榜玩家数"], analysis.quality["有排行榜玩家数"], "至少上传过 rankings 的玩家数"],
];
overview.getRange("A5:D10").values = overviewRows;
overview.getRange("B8").formulas = [["=B6/B5"]];
overview.getRange("C8").formulas = [["=C6/C5"]];
styleBody(overview, "A5:D10");
applyNumberFormatting(overview, ["B5:C7", "B9:C10"]);
applyRateFormatting(overview, "B8:C8");

overview.getRange("A12:D12").merge();
overview.getRange("A12:D12").values = [["按玩家累计 total / won 计算的真实总胜率"]];
overview.getRange("A12:D12").format = { fill: colors.mint, font: { bold: true, color: colors.ink } };
overview.getRange("A13:D13").values = [["口径", "累计游戏次数", "累计胜利次数", "真实总胜率"]];
styleHeader(overview, "A13:D13");
const cumulativeSummaryRows = Object.values(cumulative.summaries).map((row) => [row["口径"], row["累计游戏次数"], row["累计胜利次数"], null]);
overview.getRange("A14:D15").values = cumulativeSummaryRows;
overview.getRange("D14").formulas = [["=C14/B14"]];
overview.getRange("D15").formulas = [["=C15/B15"]];
styleBody(overview, "A14:D15");
applyNumberFormatting(overview, "B14:C15");
applyRateFormatting(overview, "D14:D15");

overview.getRange("A20:D20").merge();
overview.getRange("A20:D20").values = [["分析口径与解读"]];
overview.getRange("A20:D20").format = { fill: colors.lavender, font: { bold: true, color: colors.purple } };
overview.getRange("A21:D25").merge(true);
overview.getRange("A21:D25").values = [
  ["1. 标准可比对局：排除自定义种子、每日模式和 TEST_MODE，并剔除黑名单设备；这是挑战胜率的主分析口径。"],
  ["2. 挑战类型胜率：分母是开启该挑战的有效对局数；同时提供未开启该挑战的对照胜率和百分点差。"],
  ["3. 挑战数量胜率：按一局中开启的挑战数量分组，展示该组对局数、胜局数和胜率；小样本组应谨慎解读。"],
  ["4. 相关性：Jaccard 衡量集合重叠，Lift > 1 表示共现高于独立期望，Phi > 0 表示正相关；相关性不等于因果关系。"],
  ["5. 真实总胜率：按每个玩家累计 rankings.total 与 rankings.won 求和后计算，避免对个人胜率做简单平均。"],
];
overview.getRange("A21:D25").format = { wrapText: true, font: { color: colors.ink }, rowHeight: 26 };

const standardType = standard.type_rows.map((row) => [
  labelByName[row["挑战枚举"]] || row["挑战枚举"], row["挑战枚举"], row["掩码"], row["对局数"], row["胜局数"], row["负局数"], row["胜率"], row["胜率95%下限"], row["胜率95%上限"], row["未开启对局数"], row["未开启胜率"], row["胜率差百分点"], row["相对提升倍数"], row["口径"],
]);
const fullType = full.type_rows.map((row) => [
  labelByName[row["挑战枚举"]] || row["挑战枚举"], row["挑战枚举"], row["掩码"], row["对局数"], row["胜局数"], row["负局数"], row["胜率"], row["胜率95%下限"], row["胜率95%上限"], row["未开启对局数"], row["未开启胜率"], row["胜率差百分点"], row["相对提升倍数"], row["口径"],
]);
const typeHeader = ["挑战", "挑战枚举", "掩码", "开启对局数", "胜局数", "负局数", "胜率", "95%下限", "95%上限", "未开启对局数", "未开启胜率", "胜率差(百分点)", "相对提升倍数", "口径"];
styleTitle(typeSheet, "A1:N1", "挑战类型胜率");
typeSheet.getRange("A3:N3").values = [typeHeader];
styleHeader(typeSheet, "A3:N3");
typeSheet.getRange(`A4:N${3 + standardType.length}`).values = standardType;
typeSheet.getRange(`A${5 + standardType.length}:N${4 + standardType.length * 2}`).values = fullType;
styleBody(typeSheet, `A4:N${3 + standardType.length * 2}`);
applyNumberFormatting(typeSheet, [`C4:F${3 + standardType.length * 2}`, `J4:J${3 + standardType.length * 2}`]);
applyRateFormatting(typeSheet, `G4:I${3 + standardType.length * 2}`);
typeSheet.getRange(`K4:K${3 + standardType.length * 2}`).format.numberFormat = "0.0%";
typeSheet.getRange(`L4:L${3 + standardType.length * 2}`).format.numberFormat = "0.0";
typeSheet.getRange(`M4:M${3 + standardType.length * 2}`).format.numberFormat = "0.00x";
addTable(typeSheet, `A3:N${3 + standardType.length * 2}`, "ChallengeTypeTable");
typeSheet.freezePanes.freezeRows(3);
setWidths(typeSheet, { A: 20, B: 22, C: 10, D: 12, E: 10, F: 10, G: 10, H: 11, I: 11, J: 14, K: 12, L: 14, M: 14, N: 16 });

const countRows = [...standard.count_rows, ...full.count_rows].map((row) => [row["口径"], row["开启挑战数量"], row["对局数"], row["胜局数"], row["负局数"], row["胜率"], row["占该口径比例"], row["平均挑战数"]]);
styleTitle(countSheet, "A1:H1", "按开启挑战数量统计胜率");
countSheet.getRange("A3:H3").values = [["口径", "开启挑战数量", "对局数", "胜局数", "负局数", "胜率", "占该口径比例", "平均挑战数"]];
styleHeader(countSheet, "A3:H3");
countSheet.getRange(`A4:H${3 + countRows.length}`).values = countRows;
styleBody(countSheet, `A4:H${3 + countRows.length}`);
applyNumberFormatting(countSheet, [`B4:E${3 + countRows.length}`, `H4:H${3 + countRows.length}`]);
applyRateFormatting(countSheet, `F4:G${3 + countRows.length}`);
addTable(countSheet, `A3:H${3 + countRows.length}`, "ChallengeCountTable");
countSheet.freezePanes.freezeRows(3);
setWidths(countSheet, { A: 18, B: 16, C: 10, D: 10, E: 10, F: 10, G: 14, H: 12 });

const pairRows = standard.pair_rows.map((row) => [
  labelByName[row["挑战A"]] || row["挑战A"], labelByName[row["挑战B"]] || row["挑战B"], row["共同开启次数"], row["共同开启胜局数"], row["共同开启胜率"], row["挑战A开启次数"], row["挑战B开启次数"], row["Jaccard"], row["Lift"], row["Phi"], row["口径"],
]);
styleTitle(pairSheet, "A1:K1", "挑战两两开启相关性（标准可比对局）");
pairSheet.getRange("A3:K3").values = [["挑战A", "挑战B", "共同开启次数", "共同开启胜局数", "共同开启胜率", "A开启次数", "B开启次数", "Jaccard", "Lift", "Phi", "口径"]];
styleHeader(pairSheet, "A3:K3");
pairSheet.getRange(`A4:K${3 + pairRows.length}`).values = pairRows;
styleBody(pairSheet, `A4:K${3 + pairRows.length}`);
applyNumberFormatting(pairSheet, [`C4:D${3 + pairRows.length}`, `F4:G${3 + pairRows.length}`]);
pairSheet.getRange(`E4:E${3 + pairRows.length}`).format.numberFormat = "0.0%";
pairSheet.getRange(`H4:H${3 + pairRows.length}`).format.numberFormat = "0.000";
pairSheet.getRange(`I4:J${3 + pairRows.length}`).format.numberFormat = "0.000";
pairSheet.getRange(`I4:I${3 + pairRows.length}`).conditionalFormats.add("colorScale", { colors: ["#FEE2E2", "#FEF3C7", "#DCFCE7"], thresholds: ["min", "50%", "max"] });
pairSheet.getRange(`J4:J${3 + pairRows.length}`).conditionalFormats.add("colorScale", { colors: ["#FEE2E2", "#FEF3C7", "#DCFCE7"], thresholds: ["min", "50%", "max"] });
addTable(pairSheet, `A3:K${3 + pairRows.length}`, "ChallengePairTable");
pairSheet.freezePanes.freezeRows(3);
setWidths(pairSheet, { A: 22, B: 22, C: 14, D: 14, E: 14, F: 12, G: 12, H: 11, I: 10, J: 10, K: 16 });

const comboRows = standard.combination_rows.slice(0, 80).map((row) => [row["挑战组合"], row["挑战掩码"], row["开启挑战数量"], row["对局数"], row["胜局数"], row["负局数"], row["胜率"], row["口径"]]);
styleTitle(comboSheet, "A1:H1", "挑战组合胜率（标准口径，按对局数排序）");
comboSheet.getRange("A3:H3").values = [["挑战组合", "挑战掩码", "开启挑战数量", "对局数", "胜局数", "负局数", "胜率", "口径"]];
styleHeader(comboSheet, "A3:H3");
comboSheet.getRange(`A4:H${3 + comboRows.length}`).values = comboRows;
styleBody(comboSheet, `A4:H${3 + comboRows.length}`);
applyNumberFormatting(comboSheet, [`B4:F${3 + comboRows.length}`, `C4:C${3 + comboRows.length}`]);
applyRateFormatting(comboSheet, `G4:G${3 + comboRows.length}`);
addTable(comboSheet, `A3:H${3 + comboRows.length}`, "ChallengeCombinationTable");
comboSheet.freezePanes.freezeRows(3);
setWidths(comboSheet, { A: 70, B: 14, C: 16, D: 10, E: 10, F: 10, G: 10, H: 16 });

const qualityRows = Object.entries(analysis.quality).map(([metric, value]) => [metric, value, "服务器云端排行榜副本"]);
styleTitle(qualitySheet, "A1:C1", "数据质量与统计边界");
qualitySheet.getRange("A3:C3").values = [["指标", "数值", "说明"]];
styleHeader(qualitySheet, "A3:C3");
qualitySheet.getRange(`A4:C${3 + qualityRows.length}`).values = qualityRows;
styleBody(qualitySheet, `A4:C${3 + qualityRows.length}`);
applyNumberFormatting(qualitySheet, `B4:B${3 + qualityRows.length}`);
qualitySheet.getRange(`A${5 + qualityRows.length}:C${8 + qualityRows.length}`).values = [
  ["胜率定义", "胜局数 / 对局数", analysis.metadata.definitions.win_rate],
  ["标准可比口径", "排除特殊模式", analysis.metadata.definitions.standard_scope],
  ["排行榜范围", "当前云端记录", analysis.metadata.definitions.record_scope],
  ["相关性解释", "非因果结论", analysis.metadata.definitions.pair_metrics],
];
qualitySheet.getRange(`A${5 + qualityRows.length}:C${8 + qualityRows.length}`).format = { wrapText: true, font: { color: colors.ink } };
setWidths(qualitySheet, { A: 26, B: 22, C: 100 });

// Charts on the overview sheet use helper data blocks placed to the right.
const chartTypeRows = [["挑战", "标准胜率", "全量胜率"], ...definitions.filter((item) => item.name !== "test_mode").map((item) => {
  const standardRow = standard.type_rows.find((row) => row["挑战枚举"] === item.name);
  const fullRow = full.type_rows.find((row) => row["挑战枚举"] === item.name);
  return [item.label, standardRow?.["胜率"] ?? null, fullRow?.["胜率"] ?? null];
})];
overview.getRange("L3:N19").values = chartTypeRows;
const typeChart = overview.charts.add("bar", overview.getRange("L3:N19"));
typeChart.title = "各挑战胜率对比";
typeChart.hasLegend = true;
typeChart.setPosition("F4", "J17");
typeChart.yAxis = { numberFormatCode: "0%" };

const countChartRows = [["开启挑战数量", "标准胜率"], ...standard.count_rows.map((row) => [row["开启挑战数量"], row["胜率"]])];
overview.getRange("L22:M35").values = countChartRows;
const countChart = overview.charts.add("line", overview.getRange("L22:M35"));
countChart.title = "开启挑战数量与胜率";
countChart.hasLegend = false;
countChart.setPosition("F19", "J32");
countChart.yAxis = { numberFormatCode: "0%" };

overview.showGridLines = false;
typeSheet.showGridLines = false;
countSheet.showGridLines = false;
pairSheet.showGridLines = false;
comboSheet.showGridLines = false;
qualitySheet.showGridLines = false;
cumulativeSheet.showGridLines = false;
setWidths(overview, { A: 22, B: 18, C: 18, D: 54, E: 3, F: 16, G: 16, H: 16, I: 16, J: 16, L: 20, M: 14, N: 14 });

const cumulativeSummary = Object.values(cumulative.summaries).map((row) => [row["口径"], row["玩家数"], row["累计游戏次数"], row["累计胜利次数"], row["累计失败次数"], null, row["个人胜率简单平均"], row["当前records累计胜率"]]);
styleTitle(cumulativeSheet, "A1:H1", "真实总胜率（按玩家累计游戏数加权）");
cumulativeSheet.getRange("A2:H2").merge();
cumulativeSheet.getRange("A2:H2").values = [[cumulative.metadata.definition + "；" + cumulative.metadata.scope_note]];
cumulativeSheet.getRange("A2:H2").format = { wrapText: true, font: { color: colors.muted, italic: true } };
cumulativeSheet.getRange("A4:H4").values = [["口径", "玩家数", "累计游戏次数", "累计胜利次数", "累计失败次数", "真实总胜率", "个人胜率简单平均", "当前records累计胜率"]];
styleHeader(cumulativeSheet, "A4:H4");
cumulativeSheet.getRange("A5:H6").values = cumulativeSummary;
cumulativeSheet.getRange("F5").formulas = [["=D5/C5"]];
cumulativeSheet.getRange("F6").formulas = [["=D6/C6"]];
styleBody(cumulativeSheet, "A5:H6");
applyNumberFormatting(cumulativeSheet, "B5:E6");
applyRateFormatting(cumulativeSheet, "F5:H6");

const playerRows = (cumulative.players_all || []).map((row) => [row.player_uuid, row.device_ip, row["黑名单"], row["累计游戏次数"], row["累计胜利次数"], row["累计失败次数"], null, row["排行榜当前records数"], row["排行榜当前records胜局数"]]);
cumulativeSheet.getRange("A9:I9").values = [["玩家 UUID", "设备 ID", "黑名单", "累计游戏次数", "累计胜利次数", "累计失败次数", "个人总胜率", "当前records数", "当前records胜局数"]];
styleHeader(cumulativeSheet, "A9:I9");
if (playerRows.length) {
  cumulativeSheet.getRange(`A10:I${9 + playerRows.length}`).values = playerRows;
  for (let row = 10; row < 10 + playerRows.length; row++) {
    cumulativeSheet.getRange(`G${row}`).formulas = [[`=IF(D${row}=0,"",E${row}/D${row})`]];
  }
  styleBody(cumulativeSheet, `A10:I${9 + playerRows.length}`);
  applyNumberFormatting(cumulativeSheet, [`D10:F${9 + playerRows.length}`, `H10:I${9 + playerRows.length}`]);
  applyRateFormatting(cumulativeSheet, `G10:G${9 + playerRows.length}`);
  addTable(cumulativeSheet, `A9:I${9 + playerRows.length}`, "CumulativePlayerTable");
}
cumulativeSheet.freezePanes.freezeRows(9);
setWidths(cumulativeSheet, { A: 38, B: 34, C: 10, D: 16, E: 16, F: 16, G: 14, H: 14, I: 16 });

await fs.mkdir(outputDir, { recursive: true });
const preview = await workbook.render({ sheetName: "概览", autoCrop: "all", scale: 1, format: "png" });
await fs.writeFile(previewPath, new Uint8Array(await preview.arrayBuffer()));
const xlsx = await SpreadsheetFile.exportXlsx(workbook);
await xlsx.save(outputPath);

const inspect = await workbook.inspect({ kind: "sheet,table", maxChars: 5000, tableMaxRows: 3, tableMaxCols: 8 });
console.log(JSON.stringify({ outputPath, previewPath, inspect: inspect.ndjson }, null, 2));

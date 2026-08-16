import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const path = "D:/STUDY/test/全服挑战胜率与相关性分析_含真实总胜率.xlsx";
const workbook = await SpreadsheetFile.importXlsx(await FileBlob.load(path));
const inspection = await workbook.inspect({
  kind: "workbook,sheet,table,formula",
  maxChars: 12000,
  tableMaxRows: 2,
  tableMaxCols: 14,
  options: { maxResults: 100 },
});
console.log(inspection.ndjson);
const errors = await workbook.inspect({
  kind: "match",
  searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A",
  options: { useRegex: true, maxResults: 100 },
  summary: "formula error scan",
});
console.log(errors.ndjson);

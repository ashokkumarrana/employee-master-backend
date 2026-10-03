package common.util;
import common.exception.InvalidExcelFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExcelUtil {

    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static ResponseEntity<ByteArrayResource> buildExcelDownloadResponse(byte[] fileContent, String fileName) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE)).contentLength(fileContent.length).body(new ByteArrayResource(fileContent));
    }

    public static List<LinkedHashMap<String, String>> parseAndValidateExcelRows(byte[] fileContent, List<String> expectedHeaders, int headerRowIndex) {
        if (fileContent == null || fileContent.length == 0) {
            throw new InvalidExcelFileException("Excel file cannot be empty");
        }
        List<LinkedHashMap<String, String>> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileContent))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidExcelFileException("The Excel file does not contain any worksheet.");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) {
                throw new InvalidExcelFileException("The Excel file is missing the header row.");
            }
            boolean hasExpectedHeaders = expectedHeaders != null && !expectedHeaders.isEmpty();
            int columnCount = hasExpectedHeaders ? expectedHeaders.size() : headerRow.getLastCellNum();
            List<String> columnNames = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                String expected = hasExpectedHeaders ? expectedHeaders.get(i) : getCellValue(headerRow, i, formatter);
                if (hasExpectedHeaders) {
                    String actual = getCellValue(headerRow, i, formatter);
                    if (!expected.equalsIgnoreCase(actual)) {
                        throw new InvalidExcelFileException("Invalid column at position " + (i + 1) + ". Expected: '" + expected + "', but found: '" + actual + "'.");
                    }
                }
                columnNames.add(expected);
            }
            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (isRowEmpty(row, columnCount, formatter)) {
                    continue;
                }
                LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
                rowData.put("_rowNumber", String.valueOf(r + 1));
                for (int i = 0; i < columnCount; i++) {
                    rowData.put(columnNames.get(i), getCellValue(row, i, formatter));
                }
                rows.add(rowData);
            }
        } catch (InvalidExcelFileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidExcelFileException("An error occurred while reading the Excel file: " + ex.getMessage());
        }
        return rows;
    }

    public static byte[] generateExcel(String sheetName, List<String> headers, List<String> fieldTypes, List<Map<String, String>> rows, boolean includeTypeRow) {
        return build(sheetName, headers, fieldTypes, rows, includeTypeRow, null);
    }

    public static byte[] generateWithStatusColors(String sheetName, List<String> headers, List<Map<String, String>> rows, String statusHeader) {
        return build(sheetName, headers, null, rows, false, statusHeader);
    }

    private static byte[] build(String sheetName, List<String> headers, List<String> fieldTypes, List<Map<String, String>> rows, boolean includeTypeRow, String statusHeader) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName != null && !sheetName.isBlank() ? sheetName : "Sheet1");
            int rowIndex = 0;
            if (includeTypeRow && fieldTypes != null && !fieldTypes.isEmpty()) {
                Row typeRow = sheet.createRow(rowIndex++);
                CellStyle requiredStyle = createFieldTypeStyle(workbook, "Required");
                CellStyle optionalStyle = createFieldTypeStyle(workbook, "Optional");
                for (int i = 0; i < fieldTypes.size(); i++) {
                    Cell cell = typeRow.createCell(i);
                    cell.setCellValue(fieldTypes.get(i));
                    cell.setCellStyle("Required".equalsIgnoreCase(fieldTypes.get(i)) ? requiredStyle : optionalStyle);
                }
            }
            Row headerRow = sheet.createRow(rowIndex++);
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            CellStyle plainRowStyle = createPlainStyle(workbook);
            CellStyle savedRowStyle = null;
            CellStyle savedStatusStyle = null;
            CellStyle invalidStatusStyle = null;
            CellStyle duplicateStatusStyle = null;
            if (statusHeader != null) {
                savedRowStyle = createStatusStyle(workbook, IndexedColors.LIGHT_GREEN, false);
                savedStatusStyle = createStatusStyle(workbook, IndexedColors.LIGHT_GREEN, true);
                invalidStatusStyle = createStatusStyle(workbook, IndexedColors.ROSE, true);
                duplicateStatusStyle = createStatusStyle(workbook, IndexedColors.LIGHT_ORANGE, true);
            }
            if (rows != null) {
                for (Map<String, String> rowData : rows) {
                    Row row = sheet.createRow(rowIndex++);
                    String rowStatus = statusHeader == null ? null : rowData.get(statusHeader);
                    boolean savedRow = "Saved".equalsIgnoreCase(rowStatus);
                    for (int i = 0; i < headers.size(); i++) {
                        String value = rowData.get(headers.get(i));
                        Cell cell = row.createCell(i);
                        cell.setCellValue(value == null ? "" : value);
                        if (statusHeader == null) {
                            cell.setCellStyle(plainRowStyle);
                            continue;
                        }
                        boolean statusColumn = statusHeader.equals(headers.get(i));
                        if (savedRow) {
                            cell.setCellStyle(statusColumn ? savedStatusStyle : savedRowStyle);
                        } else if (statusColumn && "Invalid".equalsIgnoreCase(rowStatus)) {
                            cell.setCellStyle(invalidStatusStyle);
                        } else if (statusColumn && "Duplicate".equalsIgnoreCase(rowStatus)) {
                            cell.setCellStyle(duplicateStatusStyle);
                        } else {
                            cell.setCellStyle(plainRowStyle);
                        }
                    }
                }
            }
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new InvalidExcelFileException("Failed to generate Excel file: " + ex.getMessage());
        }
    }

    private static String getCellValue(Row row, int cellIndex, DataFormatter formatter) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        return formatter.formatCellValue(cell).trim();
    }

    private static boolean isRowEmpty(Row row, int columnCount, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < columnCount; i++) {
            if (!getCellValue(row, i, formatter).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        return applyBaseStyle(style, font);
    }

    private static CellStyle createFieldTypeStyle(Workbook workbook, String fieldType) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor("Required".equalsIgnoreCase(fieldType) ? IndexedColors.RED.getIndex() : IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        return applyBaseStyle(style, font);
    }

    private static CellStyle createPlainStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(false);
        font.setColor(IndexedColors.BLACK.getIndex());
        return applyBaseStyle(style, font);
    }

    private static CellStyle createStatusStyle(Workbook workbook, IndexedColors color, boolean bold) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setColor(IndexedColors.BLACK.getIndex());
        return applyBaseStyle(style, font);
    }

    private static CellStyle applyBaseStyle(CellStyle style, Font font) {
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
package com.hrms.hrms.Utils;

import com.hrms.hrms.Entities.Shift;
import com.microsoft.schemas.office.visio.x2012.main.CellType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERs = { "Id", "Checkin", "Checkout", "Remark" };
    static String SHEET = "Shifts in "+Helper.getMonthForInt(new Date().getMonth());

    public static ByteArrayOutputStream exportToExcelByEmp(List<Shift> shifts,int year, int month, String sheetName) {

//        src/main/java/com/hrms/hrms/Utils/Template.xlsx"
//        src/main/java/com/hrms/hrms/Utils/Template.xlsx
        try (
                Workbook workbook = WorkbookFactory.create(OPCPackage.open("src/main/java/com/hrms/hrms/Utils/Template.xlsx"));
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {
                Sheet sheet = workbook.createSheet("123");
//            Sheet sheet = workbook.getSheetAt(0);
            workbook.setSheetName(0, sheetName);

            int numberOfDays = YearMonth.of(year, month).lengthOfMonth();
            int rowNum = 7, celNum = 0, dayIndex = 1;
            while (dayIndex <= numberOfDays) {
                Row row = sheet.createRow(rowNum);
                row.setHeight((short) 400);
                Cell dayOfMonth = row.createCell(0);
                dayOfMonth.setCellValue(dayIndex);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayIndex);
                Cell dayOfWeek = row.createCell(1);
                dayOfWeek.setCellValue(Helper.getDayName(calendar.get(Calendar.DAY_OF_WEEK)));
//                row.createCell(1).setCellValue(new SimpleDateFormat("HH:mm").format(calendar));
                if (calendar.get(Calendar.DAY_OF_WEEK) == 0) {
                    rowNum++;
                    Row weekTotalRow = sheet.createRow(rowNum);
                    weekTotalRow.createCell(0).setCellValue("Sub total");
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
                }
                rowNum++;
                dayIndex++;
            }

            workbook.write(out);
//            workbook.close();

            return out;
        } catch (Exception e) {
            throw new RuntimeException("Fail to import data to Excel file: " + e);
        }
    }

    public static ByteArrayInputStream exportToExcelAll(List<Shift> shifts) {

        String[] headers = {"Checkin", "Checkout", "Remark", "Emp. Name"};
//        String sheetName = "Shifts in "+Helper.getMonthForInt(month-1);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(SHEET);

            // Header
            Row headerRow = sheet.createRow(0);

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
            }

            int rowIdx = 1;
            for (Shift shift : shifts) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(shift.getShiftId());
                row.createCell(1).setCellValue(Helper.dateTimeFormatter.format(shift.getCheckIn()));
                row.createCell(2).setCellValue(Helper.dateTimeFormatter.format(shift.getCheckOut()));
                row.createCell(3).setCellValue(shift.getRemark());
                row.createCell(4).setCellValue(shift.getEmployee().getName());
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fail to import data to Excel file: " + e.getMessage());
        }
    }

    public static CellStyle borderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);

        return style;
    }

    public static CellStyle borderStyleWithAlignCenter(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    public static CellStyle saturdayStyle(Workbook wb) {
        Sheet sheet = wb.getSheetAt(1);
        Row row = sheet.getRow(22);
        Cell cell = row.getCell(1);
        return cell.getCellStyle();
    }

    public static CellStyle sundayStyle(Workbook wb) {
        Sheet sheet = wb.getSheetAt(1);
        Row row = sheet.getRow(23);
        Cell cell = row.getCell(1);
        return cell.getCellStyle();
    }

    public static CellStyle signStyle(Workbook wb) {
        Sheet sheet = wb.getSheetAt(1);
        Row row = sheet.getRow(17);
        Cell cell = row.getCell(0);
        CellStyle style = cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static RichTextString getRichTextStringCellValue(int rowAddress, Workbook wb) {
        Sheet sheet = wb.getSheetAt(1);
        Row row = sheet.getRow(rowAddress);
        Cell cell = row.getCell(0);
        return cell.getRichStringCellValue();
    }

    public static CellStyle subTotalStyle(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(24);
        Cell cell = row.getCell(0);
        CellStyle style = cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static CellStyle totalStyle(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(7);
        Cell cell = row.getCell(0);
        CellStyle style = cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static CellStyle oddGrandTitleStyle(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(8);
        Cell cell = row.getCell(5);
        CellStyle style = cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static CellStyle evenGrandTitleStyle(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(8);
        Cell cell = row.getCell(6);
        CellStyle style = cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static CellStyle grandTotalStyle(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(8);
        Cell cell = row.getCell(12);
        CellStyle style = cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static CellStyle statisticalStyle(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(9);
        Cell cell = row.getCell(0);
        CellStyle style = cell.getCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static CellStyle forHRStyle(Workbook workbook, int rowIndex, int cellIndex) {
        Sheet sheet = workbook.getSheetAt(1);
        Row row = sheet.getRow(rowIndex + 13);
        Cell cell = row.getCell(cellIndex);
        return cell.getCellStyle();
    }
}

package com.hrms.hrms.Utils;

import com.hrms.hrms.Entities.Shift;
import com.microsoft.schemas.office.visio.x2012.main.CellType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    public static ByteArrayInputStream exportToExcelByEmp(List<Shift> shifts,int year, int month, String sheetName) {

//        src/main/java/com/hrms/hrms/Utils/Template.xlsx"
//        src/main/java/com/hrms/hrms/Utils/Template.xlsx
        try (FileInputStream file = new FileInputStream("src/main/java/com/hrms/hrms/Utils/Template.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(file);) {
                Sheet sheet = workbook.createSheet("123");
//            Sheet sheet = workbook.getSheetAt(0);
            workbook.setSheetName(0, sheetName);

            int numberOfDays = YearMonth.of(year, month).lengthOfMonth();
            int rowNum = 7, celNum = 0, dayIndex = 1;
            while (dayIndex <= numberOfDays) {
                Row row = sheet.createRow(rowNum);
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
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
//            workbook.close();
            out.close();
            return new ByteArrayInputStream(out.toByteArray());
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
}

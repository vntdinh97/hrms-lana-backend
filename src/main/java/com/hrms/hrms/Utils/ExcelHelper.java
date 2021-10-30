package com.hrms.hrms.Utils;

import com.hrms.hrms.Entities.Shift;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERs = { "Id", "Checkin", "Checkout", "Remark" };
    static String SHEET = "Shifts in "+Helper.getMonthForInt(new Date().getMonth());

    public static ByteArrayInputStream exportToExcelByEmp(List<Shift> shifts) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet();

            // Header
            Row headerRow = sheet.createRow(0);

            for (int col = 0; col < HEADERs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs[col]);
            }

            int rowIdx = 1;
            for (Shift shift : shifts) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(shift.getShiftId());
                row.createCell(1).setCellValue(Helper.dateTimeFormatter.format(shift.getCheckIn()));
                row.createCell(2).setCellValue(Helper.dateTimeFormatter.format(shift.getCheckOut()));
                row.createCell(3).setCellValue(shift.getRemark());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fail to import data to Excel file: " + e.getMessage());
        }
    }

    public static ByteArrayInputStream exportToExcelAll(List<Shift> shifts, int month) {

        String[] headers = {"Checkin", "Checkout", "Remark", "Emp. Name"};
        String sheetName = "Shifts in "+Helper.getMonthForInt(month-1);

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

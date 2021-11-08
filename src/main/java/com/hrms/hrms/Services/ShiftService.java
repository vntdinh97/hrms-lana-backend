package com.hrms.hrms.Services;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Utils.ExcelHelper;
import com.hrms.hrms.Interfaces.ShiftInterface;
import com.hrms.hrms.Repositories.EmployeeRepository;
import com.hrms.hrms.Repositories.ShiftRepository;
import com.hrms.hrms.Utils.Helper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;

@Service
public class ShiftService implements ShiftInterface {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    ShiftRepository shiftRepository;

    @Override
    public List<Shift> addShift(ShiftDTO shift) {
        Optional<Employee> emp = employeeRepository.findById(shift.getEmpId());
        List<Shift> shifts = new ArrayList<>();
        if (emp.isPresent()) {
            Calendar checkIn = Calendar.getInstance();
            checkIn.setTime(shift.getCheckIn());
            checkIn.set(Calendar.MINUTE, 0);
            checkIn.set(Calendar.SECOND, 0);
            checkIn.set(Calendar.MILLISECOND, 0);

            Calendar checkOut = Calendar.getInstance();
            checkOut.setTime(shift.getCheckOut());
            checkOut.set(Calendar.MINUTE, 0);
            checkOut.set(Calendar.SECOND, 0);
            checkOut.set(Calendar.MILLISECOND, 0);

            //Separate into 2 shifts if it lies on 2 days
            if (checkIn.get(Calendar.DAY_OF_MONTH) != checkOut.get(Calendar.DAY_OF_MONTH)) {
                Calendar splitPoint = Calendar.getInstance();
                splitPoint.setTime(shift.getCheckOut());
                splitPoint.set(Calendar.HOUR_OF_DAY,0);
                splitPoint.set(Calendar.MINUTE,0);
                splitPoint.set(Calendar.SECOND,0);

                Date splitPointTime = splitPoint.getTime();
                Shift firstPart = new Shift(shift.getCheckIn(), splitPointTime, shift.getRemark(), emp.get());
                shiftRepository.save(firstPart);
                shifts.add(firstPart);

                Shift secondPart = new Shift(splitPointTime, shift.getCheckOut(), shift.getRemark(), emp.get());
                shiftRepository.save(secondPart);
                shifts.add(secondPart);
                return shifts;
            } else {
                Shift newShift = new Shift(shift.getCheckIn(), shift.getCheckOut(), shift.getRemark(), emp.get());
                Shift result = shiftRepository.save(newShift);
                shifts.add(result);
                return shifts;
            }


        }
        return null;
    }

    @Override
    public Shift deleteShift(long shiftId) {
        return null;
    }

    @Override
    public List<Shift> getShiftsByEmpId(long empId) {
        return shiftRepository.getShiftByEmpId(empId);
    }

    @Override
    public ByteArrayInputStream exportExcel(long empId, int year, int month) {
        List<Shift> shifts = this.getShiftsByEmpId(empId);
        Optional<Employee> emp = employeeRepository.findById(empId);
        shifts.stream().filter(x -> x.getCheckOut().getMonth() == month);
        try (
                InputStream inp = new FileInputStream("src/main/java/com/hrms/hrms/Utils/Template.xlsx");
                Workbook workbook = WorkbookFactory.create(inp);
                ByteArrayOutputStream out = new ByteArrayOutputStream();) {
//            Sheet sheet = workbook.createSheet("123");
            Sheet sheet = workbook.getSheetAt(0);
//            workbook.setSheetName(0, emp.get().getName());

            int numberOfDays = YearMonth.of(year, month).lengthOfMonth();
            int rowNum = 7, celNum = 0, dayIndex = 1;

            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm");
            //Sub total cell style
            CellStyle subTotalCellStyle = workbook.createCellStyle();
            Font subTotalFont = workbook.createFont();
            subTotalFont.setItalic(true);
            subTotalFont.setBold(true);
            subTotalFont.setFontName("Calibri");
            subTotalFont.setFontHeightInPoints((short) 10);
            subTotalCellStyle.setAlignment(HorizontalAlignment.CENTER);

            subTotalCellStyle.setFont(subTotalFont);
            while (dayIndex <= numberOfDays) {
                Row row = sheet.createRow(rowNum);

                // Day index and day name
                Cell dayOfMonth = row.createCell(0);
                dayOfMonth.setCellValue(dayIndex);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayIndex);
                Cell dayOfWeek = row.createCell(1);
                dayOfWeek.setCellValue(Helper.getDayName(calendar.get(Calendar.DAY_OF_WEEK) - 1));
//                row.createCell(1).setCellValue(new SimpleDateFormat("HH:mm").format(calendar));

                // Checkin, out and working time
                String dateAsString = formatDate.format(calendar.getTime());
                System.out.println(dateAsString);
                List<Shift> shiftInDate = this.shiftRepository.getShiftByEmpIdAndDate(empId, calendar);
                if (shiftInDate.size() == 1) {
                    Cell start = row.createCell(2);
                    start.setCellValue(formatHour.format(shiftInDate.get(0).getCheckIn()));

                    Cell stop = row.createCell(3);
                    stop.setCellValue(formatHour.format(shiftInDate.get(0).getCheckOut()));
                }
                if (shiftInDate.size() > 1) {
                    int startRowNum = rowNum;
                    for (int i = 0; i < shiftInDate.size(); i++) {
                        Cell start = row.createCell(2);
                        start.setCellValue(formatHour.format(shiftInDate.get(i).getCheckIn()));

                        Cell stop = row.createCell(3);
                        stop.setCellValue(formatHour.format(shiftInDate.get(i).getCheckOut()));
                        if (i != shiftInDate.size() - 1) {
                            rowNum++;
                            row = sheet.createRow(rowNum);
                        }
                    }
                    int stopRowNum = rowNum;
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, stopRowNum, 0, 0));
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, stopRowNum, 1, 1));
                }

                // Subtotal Row
                if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                    rowNum++;
                    Row weekTotalRow = sheet.createRow(rowNum);
                    Cell weekTotalCel = weekTotalRow.createCell(0);
                    weekTotalCel.setCellValue("Sub total");
                    weekTotalCel.setCellStyle(subTotalCellStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
                }
                rowNum++;
                dayIndex++;
            }

            workbook.write(out);
//            workbook.close();

            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to import data to Excel file: " + e);
        }
    }

    @Override
    public ByteArrayInputStream exportAllShift(int month) {
        return null;
    }
}

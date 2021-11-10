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

            if (checkIn.get(Calendar.DAY_OF_MONTH) != checkOut.get(Calendar.DAY_OF_MONTH) && checkIn.get(Calendar.DAY_OF_WEEK) == 1) {
                Calendar splitPoint = Calendar.getInstance();
                splitPoint.setTime(shift.getCheckOut());
                splitPoint.set(Calendar.HOUR_OF_DAY, 0);
                splitPoint.set(Calendar.MINUTE, 0);
                splitPoint.set(Calendar.SECOND, 0);

                Date splitPointTime = splitPoint.getTime();
                Shift firstPart = new Shift(shift.getCheckIn(), splitPointTime, shift.getRemark(), emp.get());
                shiftRepository.save(firstPart);
                shifts.add(firstPart);

                Shift secondPart = new Shift(splitPointTime, shift.getCheckOut(), shift.getRemark(), emp.get());
                shiftRepository.save(secondPart);
                shifts.add(secondPart);
            } else {
                Shift newShift = new Shift(shift.getCheckIn(), shift.getCheckOut(), shift.getRemark(), emp.get());
                Shift result = shiftRepository.save(newShift);
                shifts.add(result);
            }
            return shifts;
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
        Optional<Employee> emp = employeeRepository.findById(empId);
        try (
                InputStream inp = new FileInputStream("src/main/java/com/hrms/hrms/Utils/Template.xlsx");
                Workbook workbook = WorkbookFactory.create(inp);
                ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.getSheetAt(0);

            int numberOfDays = YearMonth.of(year, month).lengthOfMonth();
            int rowNum = 7, dayIndex = 1;

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
            String[] dayOffs = new String[]{"Annual leave", "Compensatory day"};
            String[] traveling = new String[]{"Annual leave", "Compensatory day"};
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

                List<Shift> shiftInDate = this.shiftRepository.getShiftByEmpIdAndDate(empId, calendar);

                // Checkin, out and working time
//                if (shiftInDate.size() == 1) {
//                    if (!Arrays.stream(dayOffs).anyMatch(shiftInDate.get(0).getRemark()::equals)) {
//                        Cell start = row.createCell(2);
//                        start.setCellValue(formatHour.format(shiftInDate.get(0).getCheckIn()));
//
//                        Cell stop = row.createCell(3);
//                        stop.setCellValue(formatHour.format(shiftInDate.get(0).getCheckOut()));
//
//                        //working hour
////
//
//                        // Cell 4 and 5
//
//                    }
//                }
//                if (shiftInDate.size() > 1) {
                int startRowNum = rowNum;
                for (int i = 0; i < shiftInDate.size(); i++) {
                    if (!Arrays.stream(dayOffs).anyMatch(shiftInDate.get(0).getRemark()::equals)) {
                        Cell start = row.createCell(2);
                        start.setCellValue(formatHour.format(shiftInDate.get(i).getCheckIn()));

                        Cell stop = row.createCell(3);
                        stop.setCellValue(formatHour.format(shiftInDate.get(i).getCheckOut()));

                        Calendar checkIn = Calendar.getInstance();
                        checkIn.setTime(shiftInDate.get(0).getCheckIn());
                        Calendar checkOut = Calendar.getInstance();
                        checkOut.setTime(shiftInDate.get(0).getCheckOut());
                        long workingHours = calculateWorkingHour(shiftInDate.get(0).getCheckIn(), shiftInDate.get(0).getCheckOut());

                        if (checkOut.get(Calendar.HOUR_OF_DAY) > 22 || checkOut.get(Calendar.HOUR_OF_DAY) <= 6) { // night shift
                            if (workingHours > 8 && !shiftInDate.get(i).getRemark().toLowerCase(Locale.ROOT).contains("travel")) { // OT
                                Cell nightTimeOT = row.createCell(7);
                                nightTimeOT.setCellValue(workingHours - 8);
                                Cell nightTime = row.createCell(5);
                                nightTime.setCellValue(8);
                            } else { // normal
                                Cell nightTime = row.createCell(5);
                                nightTime.setCellValue(workingHours);
                            }
                        } else { // daytime shift
                            if (workingHours > 8 && !shiftInDate.get(i).getRemark().toLowerCase(Locale.ROOT).contains("travel")) { //OT
                                Cell dayTimeOT = row.createCell(6);
                                dayTimeOT.setCellValue(workingHours - 8);
                                Cell dayTime = row.createCell(4);
                                dayTime.setCellValue(8);
                            } else {
                                Cell dayTime = row.createCell(4);
                                dayTime.setCellValue(workingHours);
                            }
                        }
                    }
                    if (i != shiftInDate.size() - 1) {
                        rowNum++;
                        row = sheet.createRow(rowNum);
                    }

                    // remark
                    Cell remark = row.createCell(13);
                    remark.setCellValue(shiftInDate.get(i).getRemark());
                }
                int stopRowNum = rowNum;

                if (startRowNum != stopRowNum) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, stopRowNum, 0, 0));
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, stopRowNum, 1, 1));
                }
//                }
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

    @Override
    public Shift editShift(long shiftId, ShiftDTO shiftDTO) {
        Optional<Shift> shiftOptional = this.shiftRepository.findById(shiftId);
        if (shiftOptional.isPresent()) {
            Shift shift = shiftOptional.get();
            shift.setCheckIn(shiftDTO.getCheckIn());
            shift.setCheckOut(shiftDTO.getCheckOut());
            shift.setRemark(shiftDTO.getRemark());
            return this.shiftRepository.save(shift);
        }
        return null;
    }

    private long calculateWorkingHour(Date checkIn, Date checkOut) {
        return Math.floorDiv((checkOut.getTime() - checkIn.getTime()), 3600000);
    }
}

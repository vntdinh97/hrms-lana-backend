package com.hrms.hrms.Services;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Entities.HolidayConfig;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Repositories.HolidayRepository;
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
    @Autowired
    HolidayRepository holidayRepository;

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
            shift.setCheckIn(checkIn.getTime());

            Calendar checkOut = Calendar.getInstance();
            checkOut.setTime(shift.getCheckOut());
            checkOut.set(Calendar.MINUTE, 0);
            checkOut.set(Calendar.SECOND, 0);
            checkOut.set(Calendar.MILLISECOND, 0);
            shift.setCheckOut(checkOut.getTime());

            //Separate into 2 shifts if it lies on Sunday night, Compensatory days, and holidays
            boolean isHoliday = holidayRepository.isHoliday(checkIn.get(Calendar.DAY_OF_MONTH), checkIn.get(Calendar.MONTH) + 1, checkIn.get(Calendar.YEAR))
                    || holidayRepository.isHoliday(checkOut.get(Calendar.DAY_OF_MONTH), checkOut.get(Calendar.MONTH) + 1, checkOut.get(Calendar.YEAR));

            if (checkIn.get(Calendar.DAY_OF_MONTH) != checkOut.get(Calendar.DAY_OF_MONTH) && (checkIn.get(Calendar.DAY_OF_WEEK) == 1 || isHoliday)) {
                Calendar splitPoint = Calendar.getInstance();
                splitPoint.setTime(shift.getCheckOut());
                splitPoint.set(Calendar.HOUR_OF_DAY, 0);
                splitPoint.set(Calendar.MINUTE, 0);
                splitPoint.set(Calendar.SECOND, 0);

                Date splitPointTime = splitPoint.getTime();
                Shift firstPart = new Shift(shift.getCheckIn(), splitPointTime, shift.getRemark(), emp.get(), false);
                shiftRepository.save(firstPart);
                shifts.add(firstPart);

                Shift secondPart = new Shift(splitPointTime, shift.getCheckOut(), shift.getRemark(), emp.get(), true);
                shiftRepository.save(secondPart);
                shifts.add(secondPart);
            } else {
                Shift newShift = new Shift(shift.getCheckIn(), shift.getCheckOut(), shift.getRemark(), emp.get(), false);
                Shift result = shiftRepository.save(newShift);
                shifts.add(result);
            }
            return shifts;
        }
        return null;
    }

    @Override
    public Shift deleteShift(long shiftId) {
        Optional<Shift> shift = this.shiftRepository.findById(shiftId);
        if (shift.isPresent()) {
            this.shiftRepository.delete(shift.get());
        }
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

            subTotalCellStyle.setFont(subTotalFont);

            int dayTotalOT = 0, weekTotalOT = 0, weekTotal = 0;
            int wh = 0, nswd = 0, otwd = 0, otnswd = 0, otdo = 0, otnsdo = 0, otph = 0, otnsph = 0, grandTotalOT = 0;
            int annualLeave = 0, compensatoryDay = 0, actualWorkingDays = 0;
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
                if (shiftInDate.size() > 0) {
                    actualWorkingDays ++;
                }
                int startRowNum = rowNum;
                for (int i = 0; i < shiftInDate.size(); i++) {

                    Shift shift = shiftInDate.get(i);
                    if (shift.getRemark().equals("Annual leave")){
                        annualLeave++;
                    } else if (shift.getRemark().equals("Compensatory day")) {
                        compensatoryDay++;
                    }
                    if (!Arrays.stream(dayOffs).anyMatch(shift.getRemark()::equals)) {
                        Cell start = row.createCell(2);
                        start.setCellValue(formatHour.format(shift.getCheckIn()));

                        Cell stop = row.createCell(3);
                        stop.setCellValue(formatHour.format(shift.getCheckOut()));

                        Calendar checkIn = Calendar.getInstance();
                        checkIn.setTime(shift.getCheckIn());
                        Calendar checkOut = Calendar.getInstance();
                        checkOut.setTime(shift.getCheckOut());
                        long workingHours = calculateWorkingHour(shift.getCheckIn(), shift.getCheckOut());

                        boolean isHoliday = holidayRepository.isHoliday(checkIn.get(Calendar.DAY_OF_MONTH), checkIn.get(Calendar.MONTH) + 1, checkIn.get(Calendar.YEAR));

                        if (checkOut.get(Calendar.HOUR_OF_DAY) > 22 || checkOut.get(Calendar.HOUR_OF_DAY) <= 6) { // night shift
                            if (workingHours > 8 && !shift.getRemark().toLowerCase(Locale.ROOT).contains("travel")) { // OT
                                Cell nightTimeOT = row.createCell(7);
                                nightTimeOT.setCellValue(workingHours - 8);
                                dayTotalOT += workingHours - 8;
                                otnswd += workingHours - 8;
                                Cell nightTime = row.createCell(5);
                                nightTime.setCellValue(8);
                                weekTotal += 8;
                                nswd += 8;
                            } else if (isHoliday) { // night holiday
                                Cell nightTimeHoliday = row.createCell(11);
                                nightTimeHoliday.setCellValue(workingHours);
                                dayTotalOT += workingHours;
                                otnsph += workingHours;
                            }
                            else if (calendar.get(Calendar.DAY_OF_WEEK) == 1 || calendar.get(Calendar.DAY_OF_WEEK) == 7) { // OT Weekend
                                if (calendar.get(Calendar.DAY_OF_WEEK) == 7) { // sat
                                    if (workingHours <= 2) {
                                        Cell nightTime = row.createCell(5);
                                        nightTime.setCellValue(workingHours);
                                        weekTotal += workingHours;
                                        nswd += workingHours;
                                    } else {
                                        Cell nightTime = row.createCell(5);
                                        nightTime.setCellValue(2);
                                        weekTotal += 2;
                                        nswd += 2;
                                        row.createCell(9).setCellValue(workingHours - 2);
                                        otnsdo += workingHours - 2;
                                        dayTotalOT += workingHours - 2;
                                    }
                                } else { //sun
                                    row.createCell(9).setCellValue(workingHours);
                                    dayTotalOT += workingHours;
                                    otnsdo += workingHours;
                                }
                            }

                            else { // normal
                                Cell nightTime = row.createCell(5);
                                if (shift.isTrans()) {
                                    nightTime = row.createCell(7);
                                    dayTotalOT += workingHours;
                                    otnswd += workingHours;
                                } else {
                                    weekTotal += workingHours;
                                    nswd += workingHours;
                                }
                                nightTime.setCellValue(workingHours);
                            }
                        } else { // daytime shift
                            if (workingHours > 8 && !shift.getRemark().toLowerCase(Locale.ROOT).contains("travel") && calendar.get(Calendar.DAY_OF_WEEK) != 1) { //OT
                                Cell dayTimeOT = row.createCell(6);
                                dayTimeOT.setCellValue(workingHours - 8);
                                dayTotalOT += workingHours - 8;
                                otwd += workingHours - 8;
                                Cell dayTime = row.createCell(4);
                                dayTime.setCellValue(8);
                                weekTotal += 8;
                                wh += 8;
                            } else if (isHoliday) {
                                Cell dayTimeHoliday = row.createCell(10);
                                dayTimeHoliday.setCellValue(workingHours);
                                dayTotalOT += workingHours;
                                otph += workingHours;
                            }
                            else if (calendar.get(Calendar.DAY_OF_WEEK) == 1) { //OT sun
                                Cell dayTime = row.createCell(8);
                                dayTime.setCellValue(workingHours);
                                dayTotalOT += workingHours;
                                otdo += workingHours;
                            }
                            else {
                                Cell dayTime = row.createCell(4);
                                dayTime.setCellValue(workingHours);
                                weekTotal += workingHours;
                                wh += workingHours;
                            }
                        }
                    }


                    Cell dayTotalOTCell = row.createCell(12);
                    if (dayTotalOT == 0) {
                        dayTotalOTCell.setCellValue("-");
                    } else {
                        dayTotalOTCell.setCellValue(dayTotalOT);
                    }
                    weekTotalOT += dayTotalOT;
                    dayTotalOT = 0;

                    // remark
                    Cell remark = row.createCell(13);
                    remark.setCellValue(shift.getRemark());

                    if (i != shiftInDate.size() - 1) {
                        rowNum++;
                        row = sheet.createRow(rowNum);
                    }
                }
                int stopRowNum = rowNum;

                if (startRowNum != stopRowNum) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, stopRowNum, 0, 0));
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, stopRowNum, 1, 1));
                }

                // End of week, create sub total
                if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                    rowNum++;
                    Row weekTotalRow = sheet.createRow(rowNum);
                    Cell subTotal = weekTotalRow.createCell(0);
                    subTotal.setCellValue("Sub total");
                    subTotal.setCellStyle(subTotalCellStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
                    Cell weekTotalHour = weekTotalRow.createCell(4);
                    weekTotalHour.setCellValue(weekTotal);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 4,5));

                    Cell weekTotalOTHour = weekTotalRow.createCell(12);
                    if (weekTotalOT == 0) {
                        weekTotalOTHour.setCellValue("-");
                    } else {
                        weekTotalOTHour.setCellValue(weekTotalOT);
                    }
                    weekTotalOT = 0; weekTotal = 0;
                }
                rowNum++;
                dayIndex++;

            }

            // Total
            Row total = sheet.createRow(rowNum);
            total.createCell(0).setCellValue("Total");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,3));
            total.createCell(4).setCellValue(wh);
            total.createCell(5).setCellValue(nswd);
            total.createCell(6).setCellValue(otwd);
            total.createCell(7).setCellValue(otnswd);
            total.createCell(8).setCellValue(otdo);
            total.createCell(9).setCellValue(otnsdo);
            total.createCell(10).setCellValue(otph);
            total.createCell(11).setCellValue(otnsph);
            grandTotalOT = otwd + otnswd + otdo + otnsdo + otph + otnsph;
            total.createCell(12).setCellValue(grandTotalOT);

            rowNum++;
            Row grandTitle = sheet.createRow(rowNum);
            grandTitle.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,3));
            grandTitle.createCell(4).setCellValue("WH");
            grandTitle.createCell(5).setCellValue("NSWD");
            grandTitle.createCell(6).setCellValue("OTWD");
            grandTitle.createCell(7).setCellValue("OTNSWD");
            grandTitle.createCell(8).setCellValue("OTDO");
            grandTitle.createCell(9).setCellValue("OTNSDO");
            grandTitle.createCell(10).setCellValue("OTPH");
            grandTitle.createCell(11).setCellValue("OTNSPH");
            grandTitle.createCell(12).setCellValue("TOTAL");

            rowNum++;
            Row annualLeaveRow = sheet.createRow(rowNum);
            annualLeaveRow.createCell(0).setCellValue("Annual Leave day/Số ngày nghỉ phép");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            annualLeaveRow.createCell(11).setCellValue(annualLeave);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));

            rowNum++;
            Row compensatoryRow = sheet.createRow(rowNum);
            compensatoryRow.createCell(0).setCellValue("Compensatory day used in month/Số ngày nghỉ bù đã sử dụng trong tháng");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            compensatoryRow.createCell(11).setCellValue(compensatoryDay);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));

            rowNum++;
            Row compensatoryBalanceRow = sheet.createRow(rowNum);
            compensatoryBalanceRow.createCell(0).setCellValue("Compensatory day balance/Số ngày nghỉ bù còn lại");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            compensatoryBalanceRow.createCell(11).setCellValue("-");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));

            rowNum++;
            Row actualWorkingDaysRow = sheet.createRow(rowNum);
            actualWorkingDaysRow.createCell(0).setCellValue("Actual working day/Số ngày làm việc thực tế");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            actualWorkingDaysRow.createCell(11).setCellValue(actualWorkingDays);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));

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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(checkIn);
        int timeCheckIn = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.setTime(checkOut);
        int timeCheckOut = calendar.get(Calendar.HOUR_OF_DAY);
        if (timeCheckIn == 8 && timeCheckOut == 17) {
            return 8;
        }
        return Math.floorDiv((checkOut.getTime() - checkIn.getTime()), 3600000);
    }
}

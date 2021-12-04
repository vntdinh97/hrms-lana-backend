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
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
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

            if (checkIn.get(Calendar.DAY_OF_MONTH) != checkOut.get(Calendar.DAY_OF_MONTH) && (checkIn.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || isHoliday)) {
                Calendar splitPoint = Calendar.getInstance();
                splitPoint.setTime(shift.getCheckOut());
                splitPoint.set(Calendar.HOUR_OF_DAY, 0);
                splitPoint.set(Calendar.MINUTE, 0);
                splitPoint.set(Calendar.SECOND, 0);

                Date splitPointTime = splitPoint.getTime();
                Shift firstPart = new Shift(shift.getCheckIn(), splitPointTime, shift.getRemark(), false, shift.isAddin(), shift.isLunchTime(), emp.get());
                shiftRepository.save(firstPart);
                shifts.add(firstPart);

                Shift secondPart = new Shift(splitPointTime, shift.getCheckOut(), shift.getRemark(), true, shift.isAddin(), shift.isLunchTime(), emp.get());
                shiftRepository.save(secondPart);
                shifts.add(secondPart);
            } else {
                Shift newShift = new Shift(shift.getCheckIn(), shift.getCheckOut(), shift.getRemark(), false, shift.isAddin(), shift.isLunchTime(),  emp.get());
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
        if (shift.isPresent()) this.shiftRepository.delete(shift.get());
        return null;
    }

    @Override
    public List<Shift> getShiftsByEmpId(long empId) {
        return shiftRepository.getShiftByEmpId(empId);
    }

    @Override
    public ByteArrayInputStream exportExcel(long empId, int year, int month) {
        Optional<Employee> emp = employeeRepository.findById(empId);
        if (!emp.isPresent()) {
            return null;
        }
        try (
                InputStream inp = new FileInputStream("src/main/java/com/hrms/hrms/Utils/Template.xlsx");
                Workbook workbook = WorkbookFactory.create(inp);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.getSheetAt(0);
            workbook.setSheetName(0, emp.get().getName());
            int numberOfDays = YearMonth.of(year, month).lengthOfMonth();
            int rowNum = 7, dayIndex = 1;

            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm");


            String[] dayOffs = new String[]{"Annual leave", "Compensatory day"};

            CellStyle wrapTextStyle = workbook.createCellStyle();
            wrapTextStyle.setWrapText(true);


            sheet.getRow(1).getCell(2).setCellValue(Helper.getMonthByShortNameForInt(month - 1));
            sheet.getRow(2).getCell(2).setCellValue(year);
            sheet.getRow(3).getCell(3).setCellValue(emp.get().getName());


            int dayTotalOT = 0, weekTotalOT = 0, weekTotal = 0;
            int wh = 0, nswd = 0, otwd = 0, otnswd = 0, otdo = 0, otnsdo = 0, otph = 0, otnsph = 0, grandTotalOT;
            int annualLeave = 0, compensatoryDay = 0, actualWorkingDays = 0;
            while (dayIndex <= numberOfDays) {
                Row row = sheet.createRow(rowNum);

                // Day index and day name

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayIndex);
                String dayName = Helper.getDayName(calendar.get(Calendar.DAY_OF_WEEK) - 1);
                Cell dayOfMonth = row.createCell(0);
                dayOfMonth.setCellValue(dayIndex);
                Cell dayOfWeek = row.createCell(1);
                dayOfWeek.setCellValue(dayName);
                for (int i = 0; i <= 15; i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) {
                        cell = row.createCell(i);
                    }
                    if (dayName.equals("Sat")) {
                        cell.setCellStyle(ExcelHelper.saturdayStyle(workbook));
                    } else if (dayName.equals("Sun")) {
                        cell.setCellStyle(ExcelHelper.sundayStyle(workbook));
                    } else if (i == 5 || i == 7 || i == 9 || i == 11) {
                        cell.setCellStyle(ExcelHelper.saturdayStyle(workbook));
                    } else {
                        cell.setCellStyle(ExcelHelper.borderStyleWithAlignCenter(workbook));
                    }
                }
                List<Shift> shiftInDate = this.shiftRepository.getShiftByEmpIdAndDate(empId, calendar);
                if (shiftInDate.size() > 0) {
                    actualWorkingDays ++;
                }

                int startRowNum = rowNum;
                for (int i = 0; i < shiftInDate.size(); i++) {
                    long addInHour = 0;
                    Shift shift = shiftInDate.get(i);
                    if (shift.getRemark().equals("Annual leave")){
                        annualLeave++;
                    } else if (shift.getRemark().equals("Compensatory day")) {
                        compensatoryDay++;
                    }
                    if (!Arrays.stream(dayOffs).anyMatch(shift.getRemark()::equals)) {
                        Cell start = row.getCell(2);
                        start.setCellValue(formatHour.format(shift.getCheckIn()));

                        Cell stop = row.getCell(3);
                        stop.setCellValue(formatHour.format(shift.getCheckOut()));

                        Calendar checkIn = Calendar.getInstance();
                        checkIn.setTime(shift.getCheckIn());
                        Calendar checkOut = Calendar.getInstance();
                        checkOut.setTime(shift.getCheckOut());
                        long workingHours = calculateWorkingHour(shift.getCheckIn(), shift.getCheckOut(), shift.isLunchTime());

                        boolean isHoliday = holidayRepository.isHoliday(checkIn.get(Calendar.DAY_OF_MONTH), checkIn.get(Calendar.MONTH) + 1, checkIn.get(Calendar.YEAR));

                        if (checkOut.get(Calendar.HOUR_OF_DAY) > 22 || checkOut.get(Calendar.HOUR_OF_DAY) <= 6) { // night shift
                            if (workingHours > 8 && !shift.getRemark().toLowerCase(Locale.ROOT).contains("travel")) { // OT
                                Cell nightTimeOT = row.getCell(7);
                                nightTimeOT.setCellValue(workingHours - 8);
                                dayTotalOT += workingHours - 8;
                                otnswd += workingHours - 8;
                                Cell nightTime = row.getCell(5);
                                nightTime.setCellValue(8);
                                weekTotal += 8;
                                nswd += 8;
                            } else if (isHoliday) { // night holiday
                                Cell nightTimeHoliday = row.getCell(11);
                                nightTimeHoliday.setCellValue(workingHours);
                                dayTotalOT += workingHours;
                                otnsph += workingHours;
                            }
                            else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) { // OT Weekend
                                if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) { // sat
                                    if (workingHours <= 2) {
                                        Cell nightTime = row.getCell(5);
                                        nightTime.setCellValue(workingHours);
                                        weekTotal += workingHours;
                                        nswd += workingHours;
                                    } else {
                                        Cell nightTime = row.getCell(5);
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
                                Cell nightTime = row.getCell(5);
                                if (shift.isTrans()) {
                                    nightTime = row.getCell(7);
                                    dayTotalOT += workingHours;
                                    otnswd += workingHours;
                                } else {
                                    if (shift.isAddin()) {
                                        addInHour = 8 - workingHours;
                                        weekTotal += 8;
                                        nswd += 8;
                                    } else {
                                        weekTotal += workingHours;
                                        nswd += workingHours;
                                    }
                                }
                                nightTime.setCellValue(workingHours);
                            }
                        } else { // daytime shift
                            if (workingHours > 8 && !shift.getRemark().toLowerCase(Locale.ROOT).contains("travel") && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { //OT
                                Cell dayTimeOT = row.getCell(6);
                                dayTimeOT.setCellValue(workingHours - 8);
                                dayTotalOT += workingHours - 8;
                                otwd += workingHours - 8;
                                Cell dayTime = row.getCell(4);
                                dayTime.setCellValue(8);
                                weekTotal += 8;
                                wh += 8;
                            } else if (isHoliday) {
                                Cell dayTimeHoliday = row.getCell(10);
                                dayTimeHoliday.setCellValue(workingHours);
                                dayTotalOT += workingHours;
                                otph += workingHours;
                            }
                            else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { //OT sun
                                Cell dayTime = row.getCell(8);
                                dayTime.setCellValue(workingHours);
                                dayTotalOT += workingHours;
                                otdo += workingHours;
                            }
                            else {
                                Cell dayTime = row.getCell(4);
                                dayTime.setCellValue(workingHours);
                                if (shift.isAddin()) {
                                    addInHour = 8 - workingHours;
                                    weekTotal += 8;
                                    wh += 8;
                                } else {
                                    weekTotal += workingHours;
                                    wh += workingHours;
                                }
                            }
                        }
                    }


                    Cell dayTotalOTCell = row.getCell(12);
                    if (dayTotalOT == 0) {
                        dayTotalOTCell.setCellValue("-");
                    } else {
                        dayTotalOTCell.setCellValue(dayTotalOT);
                    }
                    weekTotalOT += dayTotalOT;
                    dayTotalOT = 0;

                    //lunch time
                    Cell lunchTime = row.getCell(13);
                    lunchTime.setCellValue(shift.isLunchTime() ? "x" : "");

                    //add-in
                    Cell addIn = row.getCell(14);
                    if (addInHour == 0) {
                        addIn.setCellValue("-");
                    } else {
                        addIn.setCellValue(addInHour);
                    }

                    // remark
                    Cell remark = row.getCell(15);
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
                if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    rowNum++;
                    Row weekTotalRow = sheet.createRow(rowNum);
                    Cell subTotal = weekTotalRow.createCell(0);
                    subTotal.setCellValue("Sub total");
                    subTotal.setCellStyle(ExcelHelper.subTotalStyle(workbook));
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
                    subTotal.setCellStyle(ExcelHelper.subTotalStyle(workbook));
                    Cell weekTotalHour = weekTotalRow.createCell(4);
                    weekTotalHour.setCellValue(weekTotal);
                    weekTotalHour.setCellStyle(ExcelHelper.subTotalStyle(workbook));
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 4,5));
                    weekTotalHour.setCellStyle(ExcelHelper.subTotalStyle(workbook));


                    Cell weekTotalOTHour = weekTotalRow.createCell(12);
                    weekTotalOTHour.setCellStyle(ExcelHelper.subTotalStyle(workbook));
                    if (weekTotalOT == 0) {
                        weekTotalOTHour.setCellValue("-");
                    } else {
                        weekTotalOTHour.setCellValue(weekTotalOT);
                    }
                    weekTotalOT = 0; weekTotal = 0;

                    for (int i = 0; i <= 15; i++) {
                        Cell cell = weekTotalRow.getCell(i);
                        if (cell == null) {
                            cell = weekTotalRow.createCell(i);
                        }
                        cell.setCellStyle(ExcelHelper.subTotalStyle(workbook));
                    }
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
            for (int i = 0; i <= 12; i++) {
                Cell cell = total.getCell(i);
                if (cell == null) {
                    cell = total.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.totalStyle(workbook));
            }

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
            for (int i = 0; i <= 12; i++) {
                Cell cell = grandTitle.getCell(i);
                if (cell == null) {
                    cell = grandTitle.createCell(i);
                    cell.setCellStyle(ExcelHelper.borderStyle(workbook));
                } else if (i == 5 || i == 7 || i == 9 || i == 11) {
                    cell.setCellStyle(ExcelHelper.oddGrandTitleStyle(workbook));
                } else if (i == 6 || i == 8 || i == 10) {
                    cell.setCellStyle(ExcelHelper.evenGrandTitleStyle(workbook));
                } else if (i == 12) {
                    cell.setCellStyle(ExcelHelper.grandTotalStyle(workbook));
                } else {
                    cell.setCellStyle(ExcelHelper.totalStyle(workbook));
                }
            }

            rowNum++;
            Row annualLeaveRow = sheet.createRow(rowNum);
            annualLeaveRow.createCell(0).setCellValue("Annual Leave day/Số ngày nghỉ phép");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            annualLeaveRow.createCell(11).setCellValue(annualLeave);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));
            for (int i = 0; i <= 12; i++) {
                Cell cell = annualLeaveRow.getCell(i);
                if (cell == null) {
                    cell = annualLeaveRow.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.statisticalStyle(workbook));
            }

            rowNum++;
            Row compensatoryRow = sheet.createRow(rowNum);
            compensatoryRow.createCell(0).setCellValue("Compensatory day used in month/Số ngày nghỉ bù đã sử dụng trong tháng");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            compensatoryRow.createCell(11).setCellValue(compensatoryDay);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));
            for (int i = 0; i <= 12; i++) {
                Cell cell = compensatoryRow.getCell(i);
                if (cell == null) {
                    cell = compensatoryRow.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.statisticalStyle(workbook));
            }

            rowNum++;
            Row compensatoryBalanceRow = sheet.createRow(rowNum);
            compensatoryBalanceRow.createCell(0).setCellValue("Compensatory day balance/Số ngày nghỉ bù còn lại");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            compensatoryBalanceRow.createCell(11).setCellValue("-");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));
            for (int i = 0; i <= 12; i++) {
                Cell cell = compensatoryBalanceRow.getCell(i);
                if (cell == null) {
                    cell = compensatoryBalanceRow.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.statisticalStyle(workbook));
            }

            rowNum++;
            Row actualWorkingDaysRow = sheet.createRow(rowNum);
            actualWorkingDaysRow.createCell(0).setCellValue("Actual working day/Số ngày làm việc thực tế");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,10));
            actualWorkingDaysRow.createCell(11).setCellValue(actualWorkingDays);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 11,12));
            for (int i = 0; i <= 12; i++) {
                Cell cell = actualWorkingDaysRow.getCell(i);
                if (cell == null) {
                    cell = actualWorkingDaysRow.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.statisticalStyle(workbook));
            }

            rowNum++;
            Row forHRTitle = sheet.createRow(rowNum);
            forHRTitle.createCell(0).setCellValue("For HR");
            forHRTitle.createCell(1).setCellValue("Night shift (hour)");
            forHRTitle.createCell(4).setCellValue("OT (hour)");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum+2, 0,0));
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1,3));
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 4,9));

            for (int i = 0; i <= 9; i++) {
                Cell cell = forHRTitle.getCell(i);
                if (cell == null) {
                    cell = forHRTitle.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.forHRStyle(workbook, 0, i));
            }

            rowNum++;
            Row forHRSubTitle = sheet.createRow(rowNum);
            forHRSubTitle.createCell(1).setCellValue("NSWD");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1,3));
            forHRSubTitle.createCell(4).setCellValue("NSPH");
            forHRSubTitle.createCell(5).setCellValue("WD");
            forHRSubTitle.createCell(6).setCellValue("DO");
            forHRSubTitle.createCell(7).setCellValue("NS");
            forHRSubTitle.createCell(8).setCellValue("NSDO");
            forHRSubTitle.createCell(9).setCellValue("PH");

            for (int i = 0; i <= 9; i++) {
                Cell cell = forHRSubTitle.getCell(i);
                if (cell == null) {
                    cell = forHRSubTitle.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.forHRStyle(workbook, 1, i));
            }

            rowNum++;
            Row forHRValue = sheet.createRow(rowNum);
            forHRValue.createCell(1).setCellValue(nswd);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1,3));
            forHRValue.createCell(4).setCellValue(otnsph);
            forHRValue.createCell(5).setCellValue(otwd);
            forHRValue.createCell(6).setCellValue(otdo);
            forHRValue.createCell(7).setCellValue(otnswd);
            forHRValue.createCell(8).setCellValue(otnsdo);
            forHRValue.createCell(9).setCellValue(otph);

            for (int i = 0; i <= 9; i++) {
                Cell cell = forHRValue.getCell(i);
                if (cell == null) {
                    cell = forHRValue.createCell(i);
                }
                cell.setCellStyle(ExcelHelper.forHRStyle(workbook, 2, i));
            }

            rowNum++;
            sheet.createRow(rowNum);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,15));

            rowNum++;
            Row empSign = sheet.createRow(rowNum);
            empSign.setHeight((short) 1200);
            Cell empSignCell = empSign.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,15));
            empSignCell.setCellValue(ExcelHelper.getRichTextStringCellValue(17, workbook));

            rowNum++;
            Row deptHeadSign = sheet.createRow(rowNum);
            deptHeadSign.setHeight((short) 1200);
            Cell deptHeadSignCell = deptHeadSign.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,15));
            deptHeadSignCell.setCellValue(ExcelHelper.getRichTextStringCellValue(18, workbook));

            rowNum++;
            Row hrSign = sheet.createRow(rowNum);
            hrSign.setHeight((short) 1200);
            Cell hrSignCell = hrSign.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,15));
            hrSignCell.setCellValue(ExcelHelper.getRichTextStringCellValue(19, workbook));

            rowNum++;
            Row note = sheet.createRow(rowNum);
            note.setHeight((short) 1200);
            Cell noteCell = note.createCell(0);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0,15));
            Font bold = workbook.createFont();
            bold.setBold(true);
            RichTextString noteValue = ExcelHelper.getRichTextStringCellValue(20, workbook);
            noteValue.applyFont(0, 42, bold);
            noteCell.setCellValue(noteValue);

            for (int i = rowNum - 3; i <= rowNum; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j <= 15; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        cell = row.createCell(j);
                    }
                    cell.setCellStyle(ExcelHelper.signStyle(workbook));
                }
            }

            workbook.removeSheetAt(1);
            workbook.removeSheetAt(1);
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
            shift.setAddin(shiftDTO.isAddin());
            shift.setLunchTime(shiftDTO.isLunchTime());
            return this.shiftRepository.save(shift);
        }
        return null;
    }

    @Override
    public List<Shift> getShiftsByEmpIdAndMonthYear(long empId, int year, int month) {
        Optional<Employee> employeeOptional = this.employeeRepository.findById(empId);
        if (!employeeOptional.isPresent()) {
            return null;
        }
        int numberOfDays = YearMonth.of(year, month).lengthOfMonth();
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, month - 1, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(year, month - 1, numberOfDays);
        return this.shiftRepository.getShiftBetweenDates(startDate, endDate, empId);
    }


    private long calculateWorkingHour(Date checkIn, Date checkOut, boolean isLunchTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(checkIn);
        calendar.setTime(checkOut);
        long workingHour = Math.floorDiv((checkOut.getTime() - checkIn.getTime()), 3600000);
        return isLunchTime ? workingHour - 1 : workingHour;
    }
}

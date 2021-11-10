package com.hrms.hrms.Interfaces;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Shift;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ShiftInterface {
    List<Shift> addShift(ShiftDTO shift);
    Shift deleteShift(long shiftId);
    List<Shift> getShiftsByEmpId(long empId);
    ByteArrayInputStream exportExcel(long empId,int year, int month);
    ByteArrayInputStream exportAllShift(int month);
    Shift editShift(long shiftId, ShiftDTO shift);
}

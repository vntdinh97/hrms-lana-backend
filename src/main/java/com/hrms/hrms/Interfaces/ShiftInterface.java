package com.hrms.hrms.Interfaces;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Shift;

public interface ShiftInterface {
    Shift addShift(ShiftDTO shift);
    Shift deleteShift(long shiftId);
}

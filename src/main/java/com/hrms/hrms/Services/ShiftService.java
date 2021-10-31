package com.hrms.hrms.Services;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Utils.ExcelHelper;
import com.hrms.hrms.Interfaces.ShiftInterface;
import com.hrms.hrms.Repositories.EmployeeRepository;
import com.hrms.hrms.Repositories.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
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

            Calendar checkOut = Calendar.getInstance();
            checkOut.setTime(shift.getCheckOut());

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
    public ByteArrayInputStream exportExcel(long empId, int month) {
        List<Shift> shifts = this.getShiftsByEmpId(empId);
        shifts.stream().filter(x -> x.getCheckOut().getMonth() == month);
        ByteArrayInputStream in = ExcelHelper.exportToExcelByEmp(shifts, month);
        return in;
    }

    @Override
    public ByteArrayInputStream exportAllShift(int month) {
        return null;
    }
}

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
        if (emp.isPresent()) {
            Calendar checkIn = Calendar.getInstance();
            checkIn.setTime(shift.getCheckIn());

            Calendar checkOut = Calendar.getInstance();
            checkIn.setTime(shift.getCheckOut());
            List<Shift> shifts = new ArrayList<Shift>();

            //Separate into 2 shifts if it lays 2 days
            if (checkIn.get(Calendar.DAY_OF_MONTH) != checkOut.get(Calendar.DAY_OF_MONTH)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(shift.getCheckIn());
                cal.set(Calendar.HOUR_OF_DAY,23);
                cal.set(Calendar.MINUTE,59);
                cal.set(Calendar.SECOND,59);

                Date fistPartCheckOut = cal.getTime();
                Shift firstPart = new Shift(shift.getCheckIn(), fistPartCheckOut, shift.getRemark(), emp.get());
                shiftRepository.save(firstPart);
                shifts.add(firstPart);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY,23);
                cal.set(Calendar.MINUTE,59);
                cal.set(Calendar.SECOND,59);

                Date fistPartCheckOut = cal.getTime();
                Shift firstPart = new Shift(shift.getCheckIn(), fistPartCheckOut, shift.getRemark(), emp.get());
                shiftRepository.save(firstPart);
                shifts.add(firstPart);
            } else {
                Shift newShift = new Shift(shift.getCheckIn(), shift.getCheckOut(), shift.getRemark(), emp.get());
                Shift result = shiftRepository.save(newShift);
                return result;
            }

            try {

            } catch (Exception e) {
                System.out.println(e);
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
        shifts.stream().filter(x -> )
        ByteArrayInputStream in = ExcelHelper.exportToExcelByEmp(shifts);
        return in;
    }

    @Override
    public ByteArrayInputStream exportAllShift(int month) {
        return null;
    }
}

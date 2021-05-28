package com.hrms.hrms.Services;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Interfaces.ShiftInterface;
import com.hrms.hrms.Repositories.EmployeeRepository;
import com.hrms.hrms.Repositories.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShiftService implements ShiftInterface {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    ShiftRepository shiftRepository;

    @Override
    public Shift addShift(ShiftDTO shift) {
        Optional<Employee> emp = employeeRepository.findById(shift.getEmpId());
        if (emp.isPresent()) {
            Shift newShift = new Shift(shift.getCheckIn(), shift.getCheckOut(), shift.getRemark(), emp.get());
            try {
                Shift result = shiftRepository.save(newShift);
                return result;
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
}

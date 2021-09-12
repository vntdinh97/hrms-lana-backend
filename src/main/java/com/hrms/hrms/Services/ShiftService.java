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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService implements ShiftInterface {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    ShiftRepository shiftRepository;

    @Override
    public List<Shift> addShift(ShiftDTO shift) throws Exception {
        Optional<Employee> emp = employeeRepository.findById(shift.getEmpId());
        Calendar dateIn = Calendar.getInstance();
        dateIn.setTime(shift.getCheckIn());

        Calendar dateOut = Calendar.getInstance();
        dateOut.setTime(shift.getCheckOut());
        List<Shift> shiftList = new ArrayList<Shift>();
        if (!emp.isPresent()) {
            throw new Exception("Emp not existing");
        }
        if (dateIn.get(Calendar.DAY_OF_MONTH) != dateOut.get(Calendar.DAY_OF_MONTH)) { //Shift laying on 2 days
            Calendar firstDateOut = Calendar.getInstance();
            firstDateOut.setTime(shift.getCheckIn());
            firstDateOut.set(Calendar.HOUR_OF_DAY, 23);
            firstDateOut.set(Calendar.MINUTE, 59);
            firstDateOut.set(Calendar.SECOND, 59);

            Shift firstShift = new Shift(dateIn.getTime(), firstDateOut.getTime(), shift.getRemark(), emp.get());

            shiftList.add(firstShift);

            Calendar secondDateIn = Calendar.getInstance();
            secondDateIn.setTime(shift.getCheckOut());
            secondDateIn.set(Calendar.HOUR_OF_DAY, 00);
            secondDateIn.set(Calendar.MINUTE, 00);
            secondDateIn.set(Calendar.SECOND, 00);

            Shift secondShift = new Shift(secondDateIn.getTime(), dateOut.getTime(), shift.getRemark(), emp.get());

            shiftList.add(secondShift);
        }
        else {
            Shift newShift = new Shift(shift.getCheckIn(), shift.getCheckOut(), shift.getRemark(), emp.get());
            shiftList.add(newShift);
        }

        System.out.println(shiftList);

        try {
            for (Shift s: shiftList) {
                shiftRepository.save(s);
            }

            return shiftList;
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
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
    public ByteArrayInputStream exportExcel(long empId) {
        List<Shift> shifts = this.getShiftsByEmpId(empId);

        ByteArrayInputStream in = ExcelHelper.exportToExcel(shifts);
        return in;
    }
}

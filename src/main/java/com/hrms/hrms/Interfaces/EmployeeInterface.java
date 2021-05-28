package com.hrms.hrms.Interfaces;

import com.hrms.hrms.DTO.EmployeeDTO;
import com.hrms.hrms.Entities.Employee;

import java.util.List;

public interface EmployeeInterface {

    Employee addEmployee(EmployeeDTO employee);
    Employee deleteEmployee(long empId);
    List<Employee> getAll();
}

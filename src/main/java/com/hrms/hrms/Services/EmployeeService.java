package com.hrms.hrms.Services;

import com.hrms.hrms.DTO.EmployeeDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Interfaces.EmployeeInterface;
import com.hrms.hrms.Repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements EmployeeInterface {

    @Autowired
    EmployeeRepository employeeRepository;
    @Override
    public Employee addEmployee(EmployeeDTO employee) {
        Employee emp = new Employee(employee.getName());
        return employeeRepository.save(emp);
    }

    @Override
    public Employee deleteEmployee(long empId) {
        return null;
    }

    @Override
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee editEmployee(long id, EmployeeDTO employee) {
        Optional<Employee> emp = employeeRepository.findById(id);
        if (emp.isPresent()) {
            emp.get().setName(employee.getName());
            employeeRepository.save(emp.get());
            return emp.get();
        }
        return null;
    }
}

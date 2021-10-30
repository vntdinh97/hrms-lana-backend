package com.hrms.hrms.Services;

import com.hrms.hrms.DTO.EmployeeDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Enum.Role;
import com.hrms.hrms.Interfaces.EmployeeInterface;
import com.hrms.hrms.Repositories.EmployeeRepository;
import com.hrms.hrms.Repositories.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements EmployeeInterface {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    ShiftRepository shiftRepository;

    @Override
    public Employee addEmployee(EmployeeDTO employee) {
        if (employee.getRole() == null) {
            employee.setRole(Role.USER);
        }
        Employee emp = new Employee(employee.getUsername(), employee.getName(), employee.getPassword(), employee.getRole());
        return employeeRepository.save(emp);
    }

    @Override
    public Employee deleteEmployee(long empId) {
        Optional<Employee> emp = employeeRepository.findById(empId);
        if (emp.isPresent()) {
            shiftRepository.deleteByEmpId(empId);
            employeeRepository.deleteById(empId);
        }
        return emp.get();
    }

    @Override
    public List<Employee> getAll() {
        List<Employee> employeeList = employeeRepository.findAll();
        for(Employee emp : employeeList) {
            emp.setPassword(null);
        }
        return employeeList;
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

    @Override
    public Employee authenticate(EmployeeDTO employee) {
        Employee empInDatabase = this.getEmployeeByUsername(employee.getUsername());
        if (empInDatabase == null || !empInDatabase.getPassword().equals(employee.getPassword())) {
            try {
                throw new Exception("Không đúng username hoặc password");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        empInDatabase.setPassword(null);
        return empInDatabase;
    }

    @Override
    public Employee getEmployeeByUsername(String username) {
        Employee emp = employeeRepository.getEmployeeByUsername(username);
        return emp;
    }

    @Override
    public Employee getEmployeeByEmpId(long empId) {
        Optional<Employee> emp = employeeRepository.findById(empId);
        if (emp.isPresent()) {
            return emp.get();
        }
        return null;
    }
}

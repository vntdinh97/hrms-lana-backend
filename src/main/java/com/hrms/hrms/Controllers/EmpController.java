package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.EmployeeDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Interfaces.EmployeeInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/emp")
public class EmpController {

    @Autowired
    EmployeeInterface employeeService;

    @PostMapping
    public ResponseEntity<Employee> addEmployee(@RequestBody EmployeeDTO employee) {
        return new ResponseEntity<Employee>(employeeService.addEmployee(employee), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployee() {
        return new ResponseEntity<List<Employee>>(employeeService.getAll(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> editEmployee(@RequestBody EmployeeDTO employee, @PathVariable long id) {
        return new ResponseEntity<Employee>(employeeService.editEmployee(id, employee), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Employee> deleteEmployee(@PathVariable long id) {
        return new ResponseEntity<Employee>(employeeService.deleteEmployee(id), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Employee> login(@RequestBody EmployeeDTO employee) {
        return new ResponseEntity<Employee>(employeeService.authenticate(employee), HttpStatus.OK);
    }
}

package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.EmployeeDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Interfaces.EmployeeInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController()
@RequestMapping("/emp")
public class EmpController {

    @Autowired
    EmployeeInterface employeeInterface;

    @PostMapping
    public ResponseEntity<Employee> addEmployee(@RequestBody EmployeeDTO employee) {
        return new ResponseEntity<Employee>(employeeInterface.addEmployee(employee), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployee() {
        return new ResponseEntity<List<Employee>>(employeeInterface.getAll(), HttpStatus.OK);
    }
}

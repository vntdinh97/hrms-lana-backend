package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Employee;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Interfaces.EmployeeInterface;
import com.hrms.hrms.Interfaces.ShiftInterface;
import com.hrms.hrms.Utils.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shift")
public class ShiftController {

    @Autowired
    ShiftInterface shiftInterface;
    @Autowired
    EmployeeInterface employeeInterface;

    @PostMapping
    public ResponseEntity<List<Shift>> addShift(@RequestBody ShiftDTO shift) {
        return new ResponseEntity<List<Shift>>(shiftInterface.addShift(shift), HttpStatus.CREATED);
    }

    @GetMapping("/{empId}")
    public ResponseEntity<List<Shift>> getShiftsByEmpId(@PathVariable long empId) {
        return new ResponseEntity<List<Shift>>(shiftInterface.getShiftsByEmpId(empId), HttpStatus.OK);
    }

    @GetMapping("/exportExcel/{empId}/{month}")
    public ResponseEntity<Resource> exportExcel(@PathVariable long empId, @PathVariable int month) {
        Employee employee = this.employeeInterface.getEmployeeByEmpId(empId);
//        String monthName = Helper.getMonthForInt(month);
        String filename = "TS_"+(month+1)+"2021"+employee.getName()+".xlsx";
        InputStreamResource file = new InputStreamResource(shiftInterface.exportExcel(empId, month));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @GetMapping("exportAllShift/{month}")
    public ResponseEntity<Resource> exportAllShift(@PathVariable int month) {
        String filename = "TS_"+(month+1)+"2021.xlsx";
        InputStreamResource file = new InputStreamResource(shiftInterface.exportAllShift(month));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
}

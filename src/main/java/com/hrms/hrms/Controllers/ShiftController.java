package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Interfaces.ShiftInterface;
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

    @PostMapping
    public ResponseEntity<List<Shift>> addShift(@RequestBody ShiftDTO shift) throws Exception {
        return new ResponseEntity<List<Shift>>(shiftInterface.addShift(shift), HttpStatus.CREATED);
    }

    @GetMapping("/{empId}")
    public ResponseEntity<List<Shift>> getShiftsByEmpId(@PathVariable long empId) {
        return new ResponseEntity<List<Shift>>(shiftInterface.getShiftsByEmpId(empId), HttpStatus.OK);
    }

    @GetMapping("/exportExcel/{empId}")
    public ResponseEntity<Resource> exportExcel(@PathVariable long empId) {
        String filename = "shifts.xlsx";
        InputStreamResource file = new InputStreamResource(shiftInterface.exportExcel(empId));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }
}

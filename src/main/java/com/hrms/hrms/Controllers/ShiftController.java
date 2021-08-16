package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Interfaces.ShiftInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shift")
public class ShiftController {

    @Autowired
    ShiftInterface shiftInterface;

    @PostMapping
    public ResponseEntity<Shift> addShift(@RequestBody ShiftDTO shift) {
        return new ResponseEntity<Shift>(shiftInterface.addShift(shift), HttpStatus.CREATED);
    }
}

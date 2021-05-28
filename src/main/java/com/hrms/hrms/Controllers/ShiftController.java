package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Interfaces.ShiftInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/shift")
public class ShiftController {

    @Autowired
    ShiftInterface shiftInterface;

    @PostMapping
    public ResponseEntity<Shift> addShift(@RequestBody ShiftDTO shift) {
        Shift result = shiftInterface.addShift(shift);
        if (result != null) {
            return new ResponseEntity<>(shiftInterface.addShift(shift), HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}

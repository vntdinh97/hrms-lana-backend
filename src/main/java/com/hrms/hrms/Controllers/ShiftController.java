package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.ShiftDTO;
import com.hrms.hrms.Entities.Shift;
import com.hrms.hrms.Interfaces.ShiftInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/shift")
public class ShiftController {

    @Autowired
    ShiftInterface shiftInterface;

    @PostMapping
    public ResponseEntity<Shift> addShift(@RequestBody ShiftDTO shift) {
        return new ResponseEntity<Shift>(shiftInterface.addShift(shift), HttpStatus.CREATED);
    }

    @GetMapping("/{empId}")
    public ResponseEntity<List<Shift>> getShiftsByEmpId(@PathVariable long id) {
        return new ResponseEntity<List<Shift>>(shiftInterface.getShiftsByEmpId(id), HttpStatus.OK);
    }
}

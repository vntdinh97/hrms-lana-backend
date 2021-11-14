package com.hrms.hrms.Controllers;

import com.hrms.hrms.DTO.HolidayDTO;
import com.hrms.hrms.Entities.HolidayConfig;
import com.hrms.hrms.Interfaces.HolidayInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/holiday")
public class HolidayController {

    @Autowired
    HolidayInterface holidayService;

    @PostMapping
    public ResponseEntity<HolidayConfig> addHoliday(@RequestBody HolidayDTO holiday) {
        return new ResponseEntity<HolidayConfig>(holidayService.addHoliday(holiday), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HolidayConfig>> getAllHolidays() {
        return new ResponseEntity<List<HolidayConfig>>(holidayService.getAllHoliday(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HolidayConfig> editEmployee(@RequestBody HolidayDTO holiday, @PathVariable long id) {
        return new ResponseEntity<HolidayConfig>(holidayService.editHoliday(id, holiday), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HolidayConfig> deleteEmployee(@PathVariable long id) {
        return new ResponseEntity<HolidayConfig>(holidayService.deleteHoliday(id), HttpStatus.OK);
    }
}

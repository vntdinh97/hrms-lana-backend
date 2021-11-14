package com.hrms.hrms.Services;

import com.hrms.hrms.DTO.HolidayDTO;
import com.hrms.hrms.Entities.HolidayConfig;
import com.hrms.hrms.Interfaces.HolidayInterface;
import com.hrms.hrms.Repositories.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HolidayService implements HolidayInterface {
    @Autowired
    HolidayRepository holidayRepository;

    @Override
    public HolidayConfig addHoliday(HolidayDTO holiday) {
        HolidayConfig holidayConfig = new HolidayConfig(holiday.getDate(), holiday.getMonth(), holiday.getYear());
        return holidayRepository.save(holidayConfig);
    }

    @Override
    public List<HolidayConfig> getAllHoliday() {
        return this.holidayRepository.findAll();
    }

    @Override
    public HolidayConfig editHoliday(long holidayId, HolidayDTO holiday) {
        Optional<HolidayConfig> holidayConfigOptional = this.holidayRepository.findById(holidayId);
        if (holidayConfigOptional.isPresent()) {
            HolidayConfig holidayConfig = holidayConfigOptional.get();
            holidayConfig.setDate(holiday.getDate());
            holidayConfig.setMonth(holiday.getMonth());
            holidayConfig.setYear(holiday.getYear());

            return holidayRepository.save(holidayConfig);
        }
        return null;
    }

    @Override
    public HolidayConfig deleteHoliday(long holidayId) {
        Optional<HolidayConfig> holidayConfigOptional = this.holidayRepository.findById(holidayId);
        if (holidayConfigOptional.isPresent()) {
            holidayRepository.delete(holidayConfigOptional.get());
            return holidayConfigOptional.get();
        }
        return null;
    }
}

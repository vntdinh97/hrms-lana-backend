package com.hrms.hrms.Interfaces;

import com.hrms.hrms.DTO.HolidayDTO;
import com.hrms.hrms.Entities.HolidayConfig;

import java.util.List;

public interface HolidayInterface {
    HolidayConfig addHoliday(HolidayDTO holiday);
    List<HolidayConfig> getAllHoliday();
    HolidayConfig editHoliday(long holidayId, HolidayDTO holiday);
    HolidayConfig deleteHoliday(long holidayId);
}

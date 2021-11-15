package com.hrms.hrms.Repositories;

import com.hrms.hrms.Entities.HolidayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<HolidayConfig, Long> {

    @Query(value = "select exists (select * from holiday_config where date = :date and month = :month and (year = :year or year = 0))", nativeQuery = true)
    boolean isHoliday(@Param("date") int date, @Param("month") int month, @Param("year") int year);
}

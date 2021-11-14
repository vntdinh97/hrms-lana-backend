package com.hrms.hrms.Repositories;

import com.hrms.hrms.Entities.HolidayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<HolidayConfig, Long> {
}

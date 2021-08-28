package com.hrms.hrms.Repositories;

import com.hrms.hrms.Entities.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from shift where emp_id = :emp_id", nativeQuery = true)
    void deleteByEmpId(@Param("emp_id")long empId);

    @Query(value = "select * from shift where emp_id = :emp_id", nativeQuery = true)
    List<Shift> getShiftByEmpId(@Param("emp_id") long empId);
}

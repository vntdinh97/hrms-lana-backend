package com.hrms.hrms.Repositories;

import com.hrms.hrms.Entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(value = "select * from employee where username = :username", nativeQuery = true)
    Employee getEmployeeByUsername(@Param("username") String username);
}

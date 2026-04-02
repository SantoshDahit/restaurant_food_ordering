package com.restaurant.api.repository.employee;

import com.restaurant.api.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeJpaRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByCodeAndDeletedAtIsNull(String code);
    boolean existsByRestaurantCodeAndPhoneAndDeletedAtIsNull(String restaurantCode, String phone);
}

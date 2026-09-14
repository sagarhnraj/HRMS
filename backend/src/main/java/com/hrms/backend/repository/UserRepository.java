package com.hrms.backend.repository;
import com.hrms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmployee_EmployeeId(Integer employeeId);
}

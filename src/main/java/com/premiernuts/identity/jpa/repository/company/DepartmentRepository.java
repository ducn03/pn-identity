package com.premiernuts.identity.jpa.repository.company;

import com.premiernuts.identity.jpa.domain.company.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findAllByCompanyId(Long companyId);

    Optional<Department> findByCompanyIdAndCode(Long companyId, String code);
}

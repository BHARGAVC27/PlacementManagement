package com.placement.tracker.model.entity;

import com.placement.tracker.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    @Column(name = "employee_id", nullable = false, unique = true)
    private String employeeId;

    @Column(name = "department", nullable = false)
    private String department;

    public Admin() {
    }

    public Admin(Long id, String email, String passwordHash, Role role,
                 String employeeId, String department) {
        super(id, email, passwordHash, role);
        this.employeeId = employeeId;
        this.department = department;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}

package com.placement.tracker;

import com.placement.tracker.model.entity.Admin;
import com.placement.tracker.model.entity.Student;
import com.placement.tracker.model.entity.StudentProfile;
import com.placement.tracker.enums.Role;
import com.placement.tracker.repository.AdminRepository;
import com.placement.tracker.repository.StudentRepository;
import com.placement.tracker.repository.StudentProfileRepository;
import com.placement.tracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AdminRepository adminRepository,
                           StudentRepository studentRepository,
                           StudentProfileRepository studentProfileRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Create admin if not exists
        if (userRepository.findByEmail("admin@placement.com").isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail("admin@placement.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEmployeeId("EMP001");
            admin.setDepartment("Placement Cell");
            adminRepository.save(admin);
            System.out.println("✅ Admin user created: admin@placement.com / admin123");
        }

        // Create student if not exists
        if (userRepository.findByEmail("student@placement.com").isEmpty()) {
            Student student = new Student();
            student.setEmail("student@placement.com");
            student.setPasswordHash(passwordEncoder.encode("student123"));
            student.setRole(Role.STUDENT);
            student.setUsn("CS21001");
            student.setFirstName("Test");
            student.setLastName("Student");
            student.setPhone("9999999999");
            Student savedStudent = studentRepository.save(student);

            StudentProfile profile = new StudentProfile();
            profile.setCurrentCgpa(java.math.BigDecimal.valueOf(8.5));
profile.setTenthPercent(java.math.BigDecimal.valueOf(90.0));
profile.setTwelfthPercent(java.math.BigDecimal.valueOf(88.0));
            profile.setActiveBacklogs(0);
            profile.setBranch("CSE");
            profile.setResumeUrl("");
            profile.setStudent(savedStudent);
            studentProfileRepository.save(profile);
            System.out.println("✅ Student user created: student@placement.com / student123");
        }
    }
}
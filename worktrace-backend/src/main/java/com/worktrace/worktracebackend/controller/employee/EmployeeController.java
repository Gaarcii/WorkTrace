package com.worktrace.worktracebackend.controller.employee;

import com.worktrace.worktracebackend.dto.user.EmployeeRequestDto;
import com.worktrace.worktracebackend.service.auth.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        employeeService.registerEmployee(requestDto);
        return new ResponseEntity<>("Empleado creado correctamente", HttpStatus.CREATED);
    }
}

package com.worktrace.worktracebackend.controller;

import com.worktrace.worktracebackend.dto.EmployeeRequestDto;
import com.worktrace.worktracebackend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regsiter")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employee")
    public ResponseEntity<String> registerEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        employeeService.registerEmployee(requestDto);
        return new ResponseEntity<>("Empleado creado correctamente", HttpStatus.CREATED);
    }
}

package com.worktrace.worktracebackend.service.timeEntry;

import com.worktrace.worktracebackend.model.Company;
import com.worktrace.worktracebackend.model.Profile;
import com.worktrace.worktracebackend.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioYCompaniaInfo {
    private final User user;
    private final Company company;
    private final Profile profile;
}
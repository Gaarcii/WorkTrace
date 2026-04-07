package com.worktrace.worktracebackend.dto.workSite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkSiteResponseDto {
    private UUID id;
    private String name;
    private String address;
}

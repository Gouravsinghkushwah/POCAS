package com.pocas.response;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String mobile;
    private String address;
    private String accountType;
    private Double monthlyInstallment;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}

package com.pocas.request;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String name;
    private String mobile;
    private String address;
    private String accountType; // "3Y" or "5Y"
    private Double monthlyInstallment;
    private LocalDate startDate; // Admin sets start date
}

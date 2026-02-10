package com.pocas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;  // Customer Name

    @Column(nullable = false, unique = true)
    private String mobile; // Mobile number (unique per customer)

    @Column
    private String address; // Optional

    @Column(nullable = false)
    private String accountType; // "3Y" or "5Y"

    @Column(nullable = false)
    private Double monthlyInstallment; // Monthly Kist

    @Column(nullable = false)
    private LocalDate startDate; // Account start date

    @Column
    private LocalDate endDate; // System-generated based on accountType

    @Column
    private String status; // "Active", "Completed", "Closed"
}

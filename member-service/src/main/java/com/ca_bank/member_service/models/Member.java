package com.ca_bank.member_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;

@Entity
@Table(name = "members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;


    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    // Automatically set when entity is created
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Date createdAt;

    // Automatically updated when entity is modified
    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;
}


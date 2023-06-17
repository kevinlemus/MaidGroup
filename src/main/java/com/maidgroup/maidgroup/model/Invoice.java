package com.maidgroup.maidgroup.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private ArrayList<String> serviceName;
    private ArrayList<Double> price;
    private LocalDate date;
    private String firstName;
    private String LastName;
    private String clientEmail;
    private String phoneNumber;
    private double totalPrice;

}

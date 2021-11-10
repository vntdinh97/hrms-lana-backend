package com.hrms.hrms.Entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "HolidayConfig")
@Data
public class HolidayConfig {
    private static final long serialVersionUID = -1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long dayId;

    @Column(name = "date")
    private int date;

    @Column(name = "month")
    private int month;
}

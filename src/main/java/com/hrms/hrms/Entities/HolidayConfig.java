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

    @Column(name = "date", nullable = false)
    private int date;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = true)
    private int year;

    public HolidayConfig(int date, int month, int year) {
        this.date = date;
        this.month = month;
        this.year = year;
    }

    public HolidayConfig() {
    }

    public long getDayId() {
        return dayId;
    }

    public void setDayId(long dayId) {
        this.dayId = dayId;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
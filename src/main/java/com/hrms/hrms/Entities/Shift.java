package com.hrms.hrms.Entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Table(name = "Shift")
public class Shift implements Serializable {

    private static final long serialVersionUID = -2L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long shiftId;

    @Column(name = "checkin", nullable = false)
    private Date checkIn;

    @Column(name = "checkout", nullable = false)
    private Date checkOut;

    @Column(name = "remark")
    private String remark;

    @Column(name = "trans")
    private boolean trans;

    @Column(name = "is_add_in")
    private boolean isAddin;

    @Column(name = "is_lunch_time")
    private boolean isLunchTime;

    @ManyToOne
    @JoinColumn(name = "empId", nullable = false)
    private Employee employee;

    public Shift(Date checkIn, Date checkOut, String remark, boolean trans, boolean isAddin, boolean isLunchTime, Employee employee) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.remark = remark;
        this.trans = trans;
        this.isAddin = isAddin;
        this.isLunchTime = isLunchTime;
        this.employee = employee;
    }

    public boolean isTrans() {
        return trans;
    }

    public void setTrans(boolean trans) {
        this.trans = trans;
    }

    public Shift() {
    }

    public long getShiftId() {
        return shiftId;
    }

    public void setShiftId(long shiftId) {
        this.shiftId = shiftId;
    }

    public Date getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Date checkIn) {
        this.checkIn = checkIn;
    }

    public Date getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Date checkOut) {
        this.checkOut = checkOut;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Employee getEmployee() {
        return employee;
    }

    public boolean isAddin() {
        return isAddin;
    }

    public void setAddin(boolean addin) {
        isAddin = addin;
    }

    public boolean isLunchTime() {
        return isLunchTime;
    }

    public void setLunchTime(boolean lunchTime) {
        isLunchTime = lunchTime;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}

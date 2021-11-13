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

    @Column(name = "checkin")
    private Date checkIn;

    @Column(name = "checkout")
    private Date checkOut;

    @Column(name = "remark", nullable = true)
    private String remark;

    @Column(name = "trans", nullable = true)
    private boolean trans;

    @ManyToOne
    @JoinColumn(name = "empId", nullable = false)
    private Employee employee;

    public Shift(Date checkIn, Date checkOut, String remark, Employee employee, boolean trans) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.remark = remark;
        this.employee = employee;
        this.trans = trans;
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

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}

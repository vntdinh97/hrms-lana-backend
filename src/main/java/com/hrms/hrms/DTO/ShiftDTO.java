package com.hrms.hrms.DTO;

import java.util.Date;

public class ShiftDTO {

    private Date checkIn;
    private Date checkOut;
    private String remark;
    private long empId;
    private boolean isAddin;
    private boolean isLunchTime;

    public ShiftDTO() {
    }

    public ShiftDTO(Date checkIn, Date checkOut, String remark, long empId, boolean isAddin, boolean isLunchTime) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.remark = remark;
        this.empId = empId;
        this.isAddin = isAddin;
        this.isLunchTime = isLunchTime;
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

    public long getEmpId() {
        return empId;
    }

    public void setEmpId(long empId) {
        this.empId = empId;
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
}

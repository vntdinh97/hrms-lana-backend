package com.hrms.hrms.DTO;

import java.util.Date;

public class ShiftDTO {

    private Date checkIn;
    private Date checkOut;
    private String remark;
    private long empId;

    public ShiftDTO(Date checkIn, Date checkOut, String remark, long empId) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.remark = remark;
        this.empId = empId;
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
}

package com.artur_sres.multinova.dto.dashboard;

import java.math.BigDecimal;

public class DashboardSummaryResponseDTO {
    private BigDecimal sold;
    private BigDecimal received;
    private BigDecimal purchased;
    private BigDecimal billsPaid;
    private BigDecimal totalReceivable;
    private BigDecimal totalPayable;
    private BigDecimal periodBalance;
    private BigDecimal otherIncome;

    public BigDecimal getOtherIncome() {return otherIncome;}
    public void setOtherIncome(BigDecimal otherIncome) {this.otherIncome = otherIncome;}

    public BigDecimal getSold() {return sold;}
    public void setSold(BigDecimal sold) {this.sold = sold;}

    public BigDecimal getReceived() {return received;}
    public void setReceived(BigDecimal received) {this.received = received;}

    public BigDecimal getPurchased() {return purchased;}
    public void setPurchased(BigDecimal purchased) {this.purchased = purchased;}

    public BigDecimal getBillsPaid() {return billsPaid;}
    public void setBillsPaid(BigDecimal billsPaid) {this.billsPaid = billsPaid;}

    public BigDecimal getTotalReceivable() {return totalReceivable;}
    public void setTotalReceivable(BigDecimal totalReceivable) {this.totalReceivable = totalReceivable;}

    public BigDecimal getTotalPayable() {return totalPayable;}
    public void setTotalPayable(BigDecimal totalPayable) {this.totalPayable = totalPayable;}

    public BigDecimal getPeriodBalance() {return periodBalance;}
    public void setPeriodBalance(BigDecimal periodBalance) {this.periodBalance = periodBalance;}
}

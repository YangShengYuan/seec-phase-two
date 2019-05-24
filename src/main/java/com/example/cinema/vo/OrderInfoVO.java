package com.example.cinema.vo;

import java.util.List;

public class OrderInfoVO {

    List<TicketVO> ticketVOList;

    List<CouponVO> coupons;

    public List<TicketVO> getTicketVOList() {
        return ticketVOList;
    }

    public void setTicketVOList(List<TicketVO> ticketVOList) {
        this.ticketVOList = ticketVOList;
    }

    public List<CouponVO> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<CouponVO> coupons) {
        this.coupons = coupons;
    }

}

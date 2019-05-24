package com.example.cinema.blImpl.sales;

import com.example.cinema.po.Ticket;

import java.util.List;

/**
 * @author YangYiCun
 * @date 2019/5/13 11:15 AM
 */
public interface TicketServiceForBl {
    /**
     * 根据排片id搜索所有电影票，即搜索一场电影卖出去几张票
     * @param scheduleId
     * @return
     */
    List<Ticket> searchTicketsBySchedule(int scheduleId);
}

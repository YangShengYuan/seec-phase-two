package com.example.cinema.blImpl.sales;

import com.example.cinema.bl.sales.TicketService;
import com.example.cinema.blImpl.management.hall.HallServiceForBl;
import com.example.cinema.blImpl.management.schedule.ScheduleServiceForBl;
import com.example.cinema.blImpl.promotion.ActivityServiceForBl;
import com.example.cinema.blImpl.promotion.CouponServiceForBl;
import com.example.cinema.blImpl.promotion.VIPCardServiceForBl;
import com.example.cinema.data.sales.TicketMapper;
import com.example.cinema.po.Activity;
import com.example.cinema.po.AudiencePrice;
import com.example.cinema.po.Coupon;
import com.example.cinema.po.Hall;
import com.example.cinema.po.ScheduleItem;
import com.example.cinema.po.Ticket;
import com.example.cinema.po.VIPCard;
import com.example.cinema.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liying on 2019/4/16.
 */
@Service
public class TicketServiceImpl implements TicketService, TicketServiceForBl {

    @Autowired
    TicketMapper ticketMapper;
    @Autowired
    ScheduleServiceForBl scheduleService;
    @Autowired
    HallServiceForBl hallService;
    @Autowired
    CouponServiceForBl couponService;
    @Autowired
    VIPCardServiceForBl vipCardService;
    @Autowired
    ActivityServiceForBl activityService;

    @Override
    @Transactional
    public ResponseVO addTicket(TicketForm ticketForm) {
    	List<SeatForm> seats=ticketForm.getSeats();
    	List<TicketVO> tickets=new ArrayList<TicketVO>();
    	seats.stream().forEach ( seat -> {
    		int column=seat.getColumnIndex();
    		int row=seat.getRowIndex();
        	Ticket ticket=new Ticket();
        	ticket.setScheduleId(ticketForm.getScheduleId());
        	ticket.setUserId(ticketForm.getUserId());
        	ticket.setColumnIndex(column);
        	ticket.setRowIndex(row);
        	ticket.setState(0);
        	ticketMapper.insertTicket(ticket);
        	//处理VO
            int id=ticketMapper.selectLastId();
            System.out.println("new ticket Id: "+id);
            TicketVO ticketVO=new TicketVO();
            ticketVO.setId(id);
            ticketVO.setScheduleId(ticketForm.getScheduleId());
            ticketVO.setUserId(ticketForm.getUserId());
            ticketVO.setColumnIndex(column);
            ticketVO.setRowIndex(row);
            ticketVO.setState("未完成");
            tickets.add(ticketVO);
        });
    	//getCoupon and listCouponVO
        List<CouponVO> coupons=couponList2CouponVOList(couponService.selectByUserId(ticketForm.getUserId()));
    	OrderInfoVO orderInfo=new OrderInfoVO();
    	orderInfo.setTicketVOList(tickets);
    	orderInfo.setCoupons(coupons);
        System.out.println(coupons.size());
    	return ResponseVO.buildSuccess(orderInfo);//此处尚不知道返回什么东西，前端js文件也没说，因此暂定
    }

    @Override
    @Transactional
    public ResponseVO completeTicket(List<Integer> id, int couponId) {
    	AudiencePrice ap=new AudiencePrice();
        ap.setTotalPrice(0.0);//初始化一个数字否则下方第一个get报错
    	id.stream().forEach(ticketId -> {
            System.out.println(ticketId);
    		Ticket ticket=ticketMapper.selectTicketById(ticketId);
    		ScheduleItem schedule=scheduleService.getScheduleItemById(ticket.getScheduleId());
    		double ticketPrice=schedule.getFare();
    		ap.setTotalPrice(ap.getTotalPrice()+ticketPrice);
    		ap.setUserId(ticket.getUserId());
    		grantCoupons(schedule.getMovieId(),ticket.getUserId()); //送优惠券
    		ticketMapper.updateTicketState(ticketId, 1);
    	});
    	Coupon coupon=couponService.selectById(couponId);
    	if (coupon!=null&&ap.getTotalPrice()>coupon.getTargetAmount()) {
    		ap.setTotalPrice(ap.getTotalPrice()-coupon.getDiscountAmount());
    	}
    	couponService.deleteCouponUser(couponId, ap.getUserId());  //优惠券用完了要删掉那张优惠券和用户的映射关系
    	
    	
    	
    	return ResponseVO.buildSuccess(ap);//返回值待定，此处暂定为AudiencePrice对象，根据优惠活动赠送优惠券还没写，因为不知道怎么送的
    }

    @Override
    public ResponseVO getBySchedule(int scheduleId) {
        try {
            List<Ticket> tickets = ticketMapper.selectTicketsBySchedule(scheduleId);
            ScheduleItem schedule=scheduleService.getScheduleItemById(scheduleId);
            Hall hall=hallService.getHallById(schedule.getHallId());
            int[][] seats=new int[hall.getRow()][hall.getColumn()];
            tickets.stream().forEach(ticket -> {
                seats[ticket.getRowIndex()][ticket.getColumnIndex()]=1;
            });
            ScheduleWithSeatVO scheduleWithSeatVO=new ScheduleWithSeatVO();
            scheduleWithSeatVO.setScheduleItem(schedule);
            scheduleWithSeatVO.setSeats(seats);
            return ResponseVO.buildSuccess(scheduleWithSeatVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getTicketByUser(int userId) {
    	List<Ticket> ticketList=ticketMapper.selectTicketByUser(userId);
    	List<TicketVO_User> ticketVOList=new ArrayList<>();
    	for(Ticket ticket:ticketList){
    	    ScheduleItem scheduleItem=scheduleService.getScheduleItemById(ticket.getScheduleId());
    	    TicketVO_User ticketVO=new TicketVO_User();
            ticketVO.setSeat((ticket.getRowIndex()+1)+"排"+(ticket.getColumnIndex()+1)+"座");
            ticketVO.setState(ticket.getState());
    	    ticketVO.setName(scheduleItem.getMovieName());
    	    ticketVO.setHall(scheduleItem.getHallId());
    	    ticketVO.setStartTime(scheduleItem.getStartTime());
            ticketVO.setEndTime(scheduleItem.getEndTime());

            ticketVOList.add(ticketVO);
        }
        return ResponseVO.buildSuccess(ticketVOList); //返回值可能改变，根据前端需要，现在明没有找到前端调用这个方法
    }
    
    
    private void grantCoupons(int movieId, int userId) {
    	List<Activity> activities=activityService.selectActivitiesByMovie(movieId);
    	activities.stream().forEach(activity -> {
    		couponService.insertCouponUser(activity.getCoupon(), userId);
    	});
    }

    @Override
    @Transactional
    public ResponseVO completeByVIPCard(List<Integer> id, int couponId) {
    	AudiencePrice ap=new AudiencePrice();
    	ap.setTotalPrice(0.0);
    	id.stream().forEach(ticketId -> {
    		Ticket ticket=ticketMapper.selectTicketById(ticketId);
    		ScheduleItem schedule=scheduleService.getScheduleItemById(ticket.getScheduleId());
    		double ticketPrice=schedule.getFare();
    		ap.setTotalPrice(ap.getTotalPrice()+ticketPrice);
    		ap.setUserId(ticket.getUserId());
    		grantCoupons(schedule.getMovieId(),ticket.getUserId()); //送优惠券
    		ticketMapper.updateTicketState(ticketId, 1);
    	});
    	Coupon coupon=couponService.selectById(couponId);
    	if (coupon!=null&&ap.getTotalPrice()>coupon.getTargetAmount()) {
    		ap.setTotalPrice(ap.getTotalPrice()-coupon.getDiscountAmount());
    	}
    	VIPCard vipCard = vipCardService.selectCardByUserId(ap.getUserId());
    	double balance = vipCard.getBalance()-ap.getTotalPrice();
    	vipCardService.updateCard(vipCard.getId(), balance );//VIP卡扣费的任务已经完成
    	couponService.deleteCouponUser(couponId, ap.getUserId());  //优惠券用完了要删掉那张优惠券和用户的映射关系
    	
    	
        return ResponseVO.buildSuccess(); //此处返回的东西待定
    }

    @Override
    public ResponseVO cancelTicket(List<Integer> id) {
    	id.stream().forEach(ticketId -> {
    		Ticket ticket=ticketMapper.selectTicketById(ticketId);
    		if (ticket.getState()==0)
    			ticketMapper.deleteTicket(ticketId);
    	});
        return ResponseVO.buildSuccess();//返回值有可能会改
    }

    @Override
    public List<Ticket> searchTicketsBySchedule(int scheduleId){
        try{
            return ticketMapper.selectTicketsBySchedule(scheduleId);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private List<CouponVO> couponList2CouponVOList(List<Coupon> coupons){
        List<CouponVO> res=new ArrayList<CouponVO>();
        for (Coupon coupon:coupons) {
            CouponVO couponVO=new CouponVO();
            couponVO.setId(coupon.getId());
            couponVO.setName(coupon.getName());
            couponVO.setTargetAmount(coupon.getTargetAmount());
            couponVO.setDiscountAmount(coupon.getDiscountAmount());
            couponVO.setDescription(coupon.getDescription());
            couponVO.setStartTime(coupon.getStartTime());
            couponVO.setEndTime(coupon.getEndTime());
            res.add(couponVO);
        }
        return res;
    }


}

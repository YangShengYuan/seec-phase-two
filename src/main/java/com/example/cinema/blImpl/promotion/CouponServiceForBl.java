package com.example.cinema.blImpl.promotion;

import com.example.cinema.po.Coupon;

import java.util.List;

public interface CouponServiceForBl {
	
	Coupon selectById(int id);

	List<Coupon> selectByUserId(int id);

	void deleteCouponUser(int couponId,int userId);

	void insertCouponUser(Coupon coupon, int userId);
}

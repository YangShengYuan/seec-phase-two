package com.example.cinema.blImpl.promotion;

import com.example.cinema.po.VIPCard;

public interface VIPCardServiceForBl {
	VIPCard selectCardByUserId(int userId);
	void updateCard(int id,double balance);
}

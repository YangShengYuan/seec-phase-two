package com.example.cinema.blImpl.promotion;

import java.util.List;

import com.example.cinema.po.Activity;

public interface ActivityServiceForBl {
	
	List<Activity> selectActivitiesByMovie(int movieId);
}

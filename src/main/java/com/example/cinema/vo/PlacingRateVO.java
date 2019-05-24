package com.example.cinema.vo;

import java.util.Date;

/**
 * @Description 某个电影上座率VO
 * @Author
 * @Date
 */
public class PlacingRateVO {
    private Integer movieId;
    /**
     * 上座率，0~1
     */
    private double placingRate;
    private String name;

    public PlacingRateVO(Integer movieId, double placingRate, String name) {
        this.movieId = movieId;
        this.placingRate = placingRate;
        this.name = name;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public double getPlacingRate() {
        return placingRate;
    }

    public void setPlacingRate(double placingRate) {
        this.placingRate = placingRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.example.cinema.data.statistics;

import com.example.cinema.po.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author fjj
 * @date 2019/4/16 1:43 PM
 */
@Mapper
public interface StatisticsMapper {
    /**
     * 查询date日期每部电影的排片次数
     * @param date
     * @return
     */
    List<MovieScheduleTime> selectMovieScheduleTimes(@Param("date") Date date, @Param("nextDate") Date nextDate);

    /**
     * 查询所有电影的总票房（包括已经下架的，降序排列）
     * @return
     */
    List<MovieTotalBoxOffice> selectMovieTotalBoxOffice();

    /**
     * 查询某天每个客户的购票金额
     * @param date
     * @param nextDate
     * @return
     */
    List<AudiencePrice> selectAudiencePrice(@Param("date") Date date, @Param("nextDate") Date nextDate);

    /**
     * 查询最近days天内票房最高的movieNum部电影
     */
    List<MovieBoxOffice> selectPopularMovies(@Param("today") Date today, @Param("startDate") Date startDate, @Param("movieNum") int movieNum);

}

package com.example.cinema.blImpl.statistics;

import com.example.cinema.bl.statistics.StatisticsService;
import com.example.cinema.blImpl.management.hall.HallServiceForBl;
import com.example.cinema.blImpl.management.schedule.MovieServiceForBl;
import com.example.cinema.blImpl.management.schedule.ScheduleServiceForBl;
import com.example.cinema.blImpl.sales.TicketServiceForBl;
import com.example.cinema.data.management.HallMapper;
import com.example.cinema.data.statistics.StatisticsMapper;
import com.example.cinema.po.*;
import com.example.cinema.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author fjj
 * @date 2019/4/16 1:34 PM
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;
    @Autowired
    private MovieServiceForBl movieServiceForBl;
    @Autowired
    private ScheduleServiceForBl scheduleServiceForBl;
    @Autowired
    private HallServiceForBl hallServiceForBl;
    @Autowired
    private TicketServiceForBl ticketServiceForBl;

    @Override
    public ResponseVO getScheduleRateByDate(Date date) {
        try{
            Date requireDate = date;
            if(requireDate == null){
                requireDate = new Date();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            requireDate = simpleDateFormat.parse(simpleDateFormat.format(requireDate));

            Date nextDate = getNumDayAfterDate(requireDate, 1);
            return ResponseVO.buildSuccess(movieScheduleTimeList2MovieScheduleTimeVOList(statisticsMapper.selectMovieScheduleTimes(requireDate, nextDate)));

        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getTotalBoxOffice() {
        try {
            return ResponseVO.buildSuccess(movieTotalBoxOfficeList2MovieTotalBoxOfficeVOList(statisticsMapper.selectMovieTotalBoxOffice()));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getAudiencePriceSevenDays() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date startDate = getNumDayAfterDate(today, -6);
            List<AudiencePriceVO> audiencePriceVOList = new ArrayList<>();
            for(int i = 0; i < 7; i++){
                AudiencePriceVO audiencePriceVO = new AudiencePriceVO();
                Date date = getNumDayAfterDate(startDate, i);
                audiencePriceVO.setDate(date);
                List<AudiencePrice> audiencePriceList = statisticsMapper.selectAudiencePrice(date, getNumDayAfterDate(date, 1));
                double totalPrice = audiencePriceList.stream().mapToDouble(item -> item.getTotalPrice()).sum();
                audiencePriceVO.setPrice(Double.parseDouble(String.format("%.2f", audiencePriceList.size() == 0 ? 0 : totalPrice / audiencePriceList.size())));
                audiencePriceVOList.add(audiencePriceVO);
            }
            return ResponseVO.buildSuccess(audiencePriceVOList);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getMoviePlacingRateByDate(Date date) {
        //要求见接口说明
        //上座率 = 当天观影人数 / 当天放映场次 / 电影院总座位数
        //返回新的VO PlacingRateVO
        try{
            List<PlacingRateVO> placingRateVOList = new ArrayList<>();
            List<Movie> movieList = movieServiceForBl.selectAllMovie();
            for(Movie movie:movieList){
                int totalTickets = 0;//这个电影这天共卖出去几张票
                int totalSeats = 0;//这个电影这天所有排片的总座位数
                List<ScheduleItem> scheduleItemList = scheduleServiceForBl.getScheduleItemByMovieIdAndDate(movie.getId(), date);
                for(ScheduleItem scheduleItem:scheduleItemList){
                    Hall hall = hallServiceForBl.getHallById(scheduleItem.getHallId());
                    totalSeats += hall.getRow()*hall.getColumn();

                    List<Ticket> ticketList = ticketServiceForBl.searchTicketsBySchedule(scheduleItem.getId());
                    totalTickets += ticketList.size();
                }
                double placingRate = totalTickets*1.0/totalSeats;//计算上座率
                placingRateVOList.add(new PlacingRateVO(movie.getId(), placingRate, movie.getName()));
            }
            return ResponseVO.buildSuccess(placingRateVOList);
//            List<Hall> hallList = hallMapper.selectAllHall();
//            int totalSeats = 0;//电影院总座位数
//            for(Hall hall: hallList){
//                totalSeats += hall.getSeatsNumber();
//            }
//            int totalAudienceNumber = statisticsMapper.selectTotalAudienceNumber(date, getNumDayAfterDate(date,1));
//            int totalScheduleNumber = statisticsMapper.selectTotalScheduleNumber(date, getNumDayAfterDate(date,1));
//            double placingRate = totalAudienceNumber/(totalScheduleNumber*totalSeats);
//            return ResponseVO.buildSuccess(new PlacingRateVO(placingRate, date));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getPopularMovies(int days, int movieNum) {
        //要求见接口说明
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date startDate = getNumDayAfterDate(today, -(days-1));
            List<MovieBoxOffice> popularMovies = statisticsMapper.selectPopularMovies(today, startDate, movieNum);
            return ResponseVO.buildSuccess(movieBoxOfficeList2MovieBoxOfficeVOList(popularMovies));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }


    /**
     * 获得num天后的日期
     * @param oldDate
     * @param num
     * @return
     */
    Date getNumDayAfterDate(Date oldDate, int num){
        Calendar calendarTime = Calendar.getInstance();
        calendarTime.setTime(oldDate);
        calendarTime.add(Calendar.DAY_OF_YEAR, num);
        return calendarTime.getTime();
    }



    private List<MovieScheduleTimeVO> movieScheduleTimeList2MovieScheduleTimeVOList(List<MovieScheduleTime> movieScheduleTimeList){
        List<MovieScheduleTimeVO> movieScheduleTimeVOList = new ArrayList<>();
        for(MovieScheduleTime movieScheduleTime : movieScheduleTimeList){
            movieScheduleTimeVOList.add(new MovieScheduleTimeVO(movieScheduleTime));
        }
        return movieScheduleTimeVOList;
    }


    private List<MovieTotalBoxOfficeVO> movieTotalBoxOfficeList2MovieTotalBoxOfficeVOList(List<MovieTotalBoxOffice> movieTotalBoxOfficeList){
        List<MovieTotalBoxOfficeVO> movieTotalBoxOfficeVOList = new ArrayList<>();
        for(MovieTotalBoxOffice movieTotalBoxOffice : movieTotalBoxOfficeList){
            movieTotalBoxOfficeVOList.add(new MovieTotalBoxOfficeVO(movieTotalBoxOffice));
        }
        return movieTotalBoxOfficeVOList;
    }

    private List<MovieBoxOfficeVO> movieBoxOfficeList2MovieBoxOfficeVOList(List<MovieBoxOffice> movieBoxOfficeList){
        List<MovieBoxOfficeVO> movieBoxOfficeVOList = new ArrayList<>();
        for(MovieBoxOffice movieBoxOffice : movieBoxOfficeList){
            movieBoxOfficeVOList.add(new MovieBoxOfficeVO(movieBoxOffice));
        }
        return movieBoxOfficeVOList;
    }
}

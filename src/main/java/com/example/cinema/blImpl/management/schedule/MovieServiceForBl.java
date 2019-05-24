package com.example.cinema.blImpl.management.schedule;

import com.example.cinema.po.Movie;
import com.example.cinema.vo.MovieVO;

import java.util.List;

/**
 * @author fjj
 * @date 2019/4/28 12:29 AM
 */
public interface MovieServiceForBl {
    /**
     * 根据id查找电影
     * @param id
     * @return
     */
    Movie getMovieById(int id);

    /**
     * 获取所有电影
     * @return
     */
    List<Movie> selectAllMovie();
}

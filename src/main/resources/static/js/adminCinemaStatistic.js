var days = 30;
var movieNum = 5;
var placeDate = formatDate(new Date()).replace(/-/g, '/');;
$(document).ready(function() {
	
	$("#placeRate-date-input").change(function () {
        placeDate = $("#placeRate-date-input").val().replace(/-/g, '/');;
        getPlacingRate();
    });
	
	$("#placeRate-date-input").val(formatDate(new Date()));

    getScheduleRate();
    
    getBoxOffice();

    getAudiencePrice();

    getPlacingRate();

    getPolularMovie();

    $('#submit_button').click(function () {
        days = $('#days_before').val();
        movieNum = $('#movie_nums').val();
		if(days=="" )
			alert("请填写天数！")
		else if(movieNum=="")
			alert("请填写电影数量！")
        else if(isNaN(Number(days)) || isNaN(Number(movieNum))){
            alert("请正确填写数字！")
        }else {
            getPolularMovie()
        }
    });

    function getScheduleRate() {

        getRequest(
            '/statistics/scheduleRate',
            function (res) {
                var data = res.content||[];
                var tableData = data.map(function (item) {
                   return {
                       value: item.time,
                       name: item.name
                   };
                });
                var nameList = data.map(function (item) {
                    return item.name;
                });
                var option = {
                    title : {
                        text: '今日排片率',
                        subtext: new Date().toLocaleDateString(),
                        x:'center'
                    },
                    tooltip : {
                        trigger: 'item',
                        formatter: "{a} <br/>{b} : {c} ({d}%)"
                    },
                    legend: {
                        x : 'center',
                        y : 'bottom',
                        data:nameList
                    },
                    toolbox: {
                        show : true,
                        feature : {
                            mark : {show: true},
                            dataView : {show: true, readOnly: false},
                            magicType : {
                                show: true,
                                type: ['pie', 'funnel']
                            },
                            restore : {show: true},
                            saveAsImage : {show: true}
                        }
                    },
                    calculable : true,
                    series : [
                        {
                            name:'面积模式',
                            type:'pie',
                            radius : [30, 110],
                            center : ['50%', '50%'],
                            roseType : 'area',
                            data:tableData
                        }
                    ]
                };
                var scheduleRateChart = echarts.init($("#schedule-rate-container")[0]);
                scheduleRateChart.setOption(option);
            },
            function (error) {
                alert(JSON.stringify(error));
            }
        );
    }

    function getBoxOffice() {

        getRequest(
            '/statistics/boxOffice/total',
            function (res) {
                var data = res.content || [];
                var tableData = data.map(function (item) {
                    return item.boxOffice;
                });
                var nameList = data.map(function (item) {
                    return item.name;
                });
                var option = {
                    title : {
                        text: '所有电影票房',
                        subtext: '截止至'+new Date().toLocaleDateString(),
                        x:'center'
                    },
                    tooltip : {
                        trigger: 'item',
                        formatter: "{a} <br/>{b} : {c}"
                    },
                    xAxis: {
                        type: 'category',
                        data: nameList
                    },
                    yAxis: {
                        type: 'value'
                    },
                    series: [{
                        data: tableData,
                        type: 'bar',
                        name: '票房'
                    }]
                };
                var scheduleRateChart = echarts.init($("#box-office-container")[0]);
                scheduleRateChart.setOption(option);
            },
            function (error) {
                alert(JSON.stringify(error));
            });
    }

    function getAudiencePrice() {
        getRequest(
            '/statistics/audience/price',
            function (res) {
                var data = res.content || [];
                var tableData = data.map(function (item) {
                    return item.price;
                });
                var nameList = data.map(function (item) {
                    return formatDate(new Date(item.date));
                });
                var option = {
                    title : {
                        text: '每日客单价',
                        x:'center'
                    },
                    xAxis: {
                        type: 'category',
                        data: nameList
                    },
                    yAxis: {
                        type: 'value'
                    },
                    series: [{
                        data: tableData,
                        type: 'line'
                    }]
                };
                var scheduleRateChart = echarts.init($("#audience-price-container")[0]);
                scheduleRateChart.setOption(option);
            },
            function (error) {
                alert(JSON.stringify(error));
            });
    }

    function getPlacingRate() {
		getRequest(
            '/statistics/PlacingRate?date='+placeDate,
            function (res) {
                var data = res.content || [];
                var tableData = data.map(function(item){
                    return item.placingRate*100.0;
                })
                var nameList = data.map(function (item) {
                    return item.name;
                })
                var option = {
                    title: {
                        text: '上座率',
                        x: 'center'
                    },
                    tooltip : {
                        trigger: 'item',
                        formatter: "{a} <br/>{b} : {c}%"
                    },
                    xAxis: {
                        type: 'category',
                        data: nameList
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel:{
                        	formatter:function(x){
                            	return x+"%";
                            }
                        }
                    },
                    series: [{
                        data: tableData,
                        type: 'bar',
						name: '上座率',
                    }]
                };
                var scheduleRateChart = echarts.init($("#place-rate-container")[0]);
                scheduleRateChart.setOption(option);
            },
            function (error) {
                alert(JSON.stringify(error))
            }
        )
    }

    function getPolularMovie() {
        getRequest(
            '/statistics/popular/movie?days='+days+'&movieNum='+movieNum,
            function (res) {
                var data = res.content || [];
                var tableData = data.map(function (item) {
                    return item.boxOffice;
                });
                var nameList = data.map(function (item) {
                    return item.name;
                });
                var option = {
                    title : {
                        text: '过去'+days+"天内电影热榜",
                        x:'center'
                    },
                    tooltip : {
                        trigger: 'item',
                        formatter: "{a} <br/>{b} : {c}"
                    },
                    xAxis: {
                        type: 'category',
                        data: nameList
                    },
                    yAxis: {
                        type: 'value'
                    },
                    series: [{
                        data: tableData,
                        type: 'bar',
                        name: '热度'
                    }]
                };
                var popularMovieChart = echarts.init($('#popular-movie-container')[0]);
                popularMovieChart.setOption(option)
            },
            function (error) {
                alert(JSON.stringify(error))
            }
        )

    }
});
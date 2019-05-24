$(document).ready(function(){

    var movieId = parseInt(window.location.href.split('?')[1].split('&')[0].split('=')[1]);
    var userId = sessionStorage.getItem('id');
    var isLike = false;
    var movieInfo;

    getMovie();
    if(sessionStorage.getItem('role') === 'admin')
        getMovieLikeChart();

    function getMovieLikeChart() {
       getRequest(
           '/movie/' + movieId + '/like/date',
           function(res){
               var data = res.content,
                    dateArray = [],
                    numberArray = [];
               data.forEach(function (item) {
                   dateArray.push(item.likeTime);
                   numberArray.push(item.likeNum);
               });

               var myChart = echarts.init($("#like-date-chart")[0]);

               // 指定图表的配置项和数据
               var option = {
                   title: {
                       text: '想看人数变化表'
                   },
                   xAxis: {
                       type: 'category',
                       data: dateArray
                   },
                   yAxis: {
                       type: 'value'
                   },
                   series: [{
                       data: numberArray,
                       type: 'line'
                   }]
               };

               // 使用刚指定的配置项和数据显示图表。
               myChart.setOption(option);
           },
           function (error) {
               alert(error);
           }
       );
    }

    function getMovie() {
        getRequest(
            '/movie/'+movieId + '/' + userId,
            function(res){
                movieInfo = res.content;
                var data = res.content;
                isLike = data.islike;
                repaintMovieDetail(data);
            },
            function (error) {
                alert(error);
            }
        );
    }
    
    function repaintMovieDetailByAdmin(movie){
        $('#movie-img').attr('src',movie.posterUrl);
        $('#movie-name').text(movie.name);
        $('#order-movie-name').text(movie.name);
        $('#movie-description').text(movie.description);
        $('#movie-startDate').text(new Date(movie.startDate).toLocaleDateString());
        $('#movie-type').text(movie.type);
        $('#movie-country').text(movie.country);
        $('#movie-language').text(movie.language);
        $('#movie-director').text(movie.director);
        $('#movie-starring').text(movie.starring);
        $('#movie-writer').text(movie.screenWriter);
        $('#movie-length').text(movie.length);
    }

    function repaintMovieDetail(movie) {
        !isLike ? $('.icon-heart').removeClass('error-text') : $('.icon-heart').addClass('error-text');
        $('#like-btn span').text(isLike ? ' 已想看' : ' 想 看');
        $('#movie-img').attr('src',movie.posterUrl);
        $('#movie-name').text(movie.name);
        $('#order-movie-name').text(movie.name);
        $('#movie-description').text(movie.description);
        $('#movie-startDate').text(new Date(movie.startDate).toLocaleDateString());
        $('#movie-type').text(movie.type);
        $('#movie-country').text(movie.country);
        $('#movie-language').text(movie.language);
        $('#movie-director').text(movie.director);
        $('#movie-starring').text(movie.starring);
        $('#movie-writer').text(movie.screenWriter);
        $('#movie-length').text(movie.length);
    }

    // user界面才有
    $('#like-btn').click(function () {
        var url = isLike ?'/movie/'+ movieId +'/unlike?userId='+ userId :'/movie/'+ movieId +'/like?userId='+ userId;
        postRequest(
             url,
            null,
            function (res) {
                 isLike = !isLike;
                getMovie();
            },
            function (error) {
                alert(error);
            });
    });
    
    function validateMovieForm(data) {
        var isValidate = true;
        if(!data.name) {
            isValidate = false;
            $('#movie-name-input').parent('.form-group').addClass('has-error');
        }
        if(!data.posterUrl) {
            isValidate = false;
            $('#movie-img-input').parent('.form-group').addClass('has-error');
        }
        if(!data.startDate) {
            isValidate = false;
            $('#movie-date-input').parent('.form-group').addClass('has-error');
        }

        if(!isValidate){
            alert("请填写完整的电影信息");
            return isValidate;
        }else{

        }
        if(isNaN(data.length)){
            alert("电影时长为分钟数");
            isValidate = false;
            $('#movie-length-input').parent('.form-group').addClass('has-error');
        }
        return isValidate;
    }

    // admin界面才有
    $("#modify-btn").click(function (e) {
    	//alert('交给你们啦，修改时需要在对应html文件添加表单然后获取用户输入，提交给后端，别忘记对用户输入进行验证。（可参照添加电影&添加排片&修改排片）');
        $("#movie-name-input").val(movieInfo.name);
        $("#movie-date-input").val(movieInfo.startDate.slice(0,10));
        $("#movie-img-input").val(movieInfo.posterUrl);
        $("#movie-description-input").val(movieInfo.description);
        $("#movie-type-input").val(movieInfo.type);
        $("#movie-length-input").val(movieInfo.length);
        $("#movie-country-input").val(movieInfo.country);
        $("#movie-language-input").val(movieInfo.language);
        $("#movie-director-input").val(movieInfo.director);
        $("#movie-star-input").val(movieInfo.starring);
        $("#movie-writer-input").val(movieInfo.screenwriter);
    });

    $("#movie-form-btn").click(function(){//点击确认修改电影信息后
        var form = {
            id:movieInfo.id,
            name: $("#movie-name-input").val(),
            posterUrl:$("#movie-img-input").val(),
            director:$("#movie-director-input").val(),
            screenwriter:$("#movie-writer-input").val(),
            starring:$("#movie-star-input").val(),
            startDate: $("#movie-date-input").val(),
            description:$("#movie-description-input").val(),
            type:$("#movie-type-input").val(),
            length:$("#movie-length-input").val(),
            country:$("#movie-country-input").val(),
            language:$("#movie-language-input").val(),
            status:movieInfo.status,
        }

        if(!validateMovieForm(form)){
            return;
        }
        postRequest(
            '/movie/update',
            form,
            function(res){
                if(res.success){
                    $("#movieEditModal").modal('hide');
                    alert("修改成功！");
                    repaintMovieDetailByAdmin(form);
                }else{
                    alert(res.message);
                }
            },
            function(error){
                alert(JSON.stringify(error))
            }
        );
    });

    $("#delete-btn").click(function () {
        var r=confirm("确认要下架该电影吗？")
        if (r) {
            postRequest(
            	"/movie/off/batch",
                {movieIdList:[parseInt(window.location.href.split('?')[1].split('&')[0].split('=')[1])]},
                function (res) {
                    if(res.success){
                        alert("下架电影成功！");
                        window.location.href='/admin/movie/manage#';
                    } else{
                        alert(res.message);
                    }
                },
                function (error) {
                    alert(JSON.stringify(error));
                }
            );
        }
    });

});
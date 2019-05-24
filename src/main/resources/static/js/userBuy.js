$(document).ready(function () {
    getMovieList();

    function getMovieList() {
        getRequest(
            '/ticket/get/' + sessionStorage.getItem('id'),
            function (res) {
                renderTicketList(res.content);
            },
            function (error) {
                alert(error);
            });
    }

    // TODO:填空 
    function renderTicketList(list) {
        for(var i=0;i<list.length;i++){
            var item=list[i];
            var domStr="<tr>";
            domStr+="<th>"+item.name+"</th>"
            domStr+="<th>"+item.hall+"号厅"+"</th>"
            domStr+="<th>"+item.seat+"</th>"
            domStr+="<th>"+formatDateTime(new Date(item.startTime))+"</th>"
            domStr+="<th>"+formatDateTime(new Date(item.endTime))+"</th>"
            if(item.state==0){    
                domStr+="<th>"+"已失效"+"</th>"
            }else{
                domStr+="<th>"+"已完成"+"</th>"
            }
            $('.table-tr').append(domStr)
        }
    }

});


function formatDateTime(date){
    var hour = date.getHours()+'';
    var minutes = date.getMinutes()+'';
    var seconds = date.getSeconds()+'';
    hour.length===1 && (hour = '0'+hour)
    minutes.length===1 && (minutes = '0'+minutes)
    seconds.length==1 && (seconds = '0'+seconds)
    return formatDate(date)+" "+hour+":"+minutes+":"+seconds;
}
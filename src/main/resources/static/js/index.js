$(function () {

    initLoadCheck();
});

function initLoadCheck() {
    $.ajax({
        url: "/initLoadCheck",
        type: "post",
        data: {
        },
        dataType: "json",
        // async: true,
        success: function (res) {
            if (res.code === 200) {

            } else {
                alert(res.msg);
            }
        },
        error: function () {
            alert("请求超时，请刷新后重试！");
        }
    })
}
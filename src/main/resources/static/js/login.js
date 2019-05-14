$(function () {
    var ua = navigator.userAgent.toLowerCase();//获取判断用的对象
    if (ua.indexOf("micromessenger") != -1) {
        //在微信中打开
        console.log("微信");
        var wxAuthorizeHref = "https://open.weixin.qq.com/connect/oauth2/authorize";
        var appid = "wx58bcb0806f1a9c32";
        var redirect_uri = "http://score.yitengkeji.net/index.html";
        var scope = "snsapi_userinfo";      // 授权页面跳转
        // window.location = "www.baidu.com";
        window.location = wxAuthorizeHref +"?appid="+ appid +"&redirect_uri="+ urlencode(redirect_uri) +
            "&response_type=code&scope="+ scope +"#wechat_redirect";
        console.log(11);
    } else {
        // 请使用微信登录跳转
    }

    $('#sure').click(function () {
        $('#alt').html("");
        var name = $('#name').val();
        var pwd = $('#password').val();
        $.ajax({
            url: "/index/login",
            type: "post",
            data: {
                name: name,
                password: pwd
            },
            dataType: "json",
            async: true,
            success: function (res) {
                if (res.code === 200) {
                    window.location = "../index.html";
                } else {
                    $('#alt').html(res.msg);
                }
            },
            error: function () {
                alert("请求超时，请刷新后重试！");
            }
        })
    })
});
$(function () {
    var ua = navigator.userAgent.toLowerCase();//获取判断用的对象
    if (ua.indexOf("micromessenger") != -1) {
        window.location = "../index.html";
    }
});
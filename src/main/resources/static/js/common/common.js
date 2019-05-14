function formatTime (sec) {
    if (sec == null) return "--";
    var date = new Date(sec);
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var minute = date.getMinutes();
    var seconds = date.getSeconds();

    var time = [hour, minute, seconds].map(formatNumber).join(":");
    var date = [year, month, day].map(formatNumber).join("-");

    return date + " " + time;
}

function formatNumber(n) {
    n = n.toString();
    return n[1] ? n : "0" + n;
}

function checkMobile(phone){
    if((/^1[3|4|5|6|7|8|9][0-9]\d{4,8}$/.test(phone))){
        return true;
    } else {
        return false;
    }
}

function isEmpty(par) {
    if (par === undefined || par == null || par === "") {
        return true;
    } else {
        return false;
    }
}

function checkIDCard(idNo) {
    var regIdNo = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
    if(regIdNo.test(idNo)){
        return true;
    } else {
        return false;
    }
}

function urlencode (str) {
    str = (str + '').toString();
    return encodeURIComponent(str).replace(/!/g, '%21').replace(/'/g, '%27').replace(/\(/g, '%28').
    replace(/\)/g, '%29').replace(/\*/g, '%2A').replace(/%20/g, '+');
}

function getCodeMsg(code) {
    switch (code) {
        case 10003: return "redirect_uri域名与后台配置不一致";
        case 10004: return "此公众号被封禁";
        case 10005: return "此公众号并没有这些scope的权限";
        case 10006: return "必须关注此测试号";
        case 10009: return "操作太频繁了，请稍后重试";
        case 10010: return "scope不能为空";
        case 10011: return "redirect_uri不能为空";
        case 10012: return "appid不能为空";
        case 10013: return "state不能为空";
        case 10015: return "公众号未授权第三方平台，请检查授权状态";
        case 10016: return "不支持微信开放平台的Appid，请使用公众号Appid";
        default:
            return "";
    }
}
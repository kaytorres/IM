//var COMETDSUCCESS = false;
var getUUidUrl = url("getUUid");
var getQRImgUrl = url("getQRImg?uuid=");
var getPollUrl = url("getPoll?uuid=");
var loginUrl = url("login?uuid=");
var confirmConnUrl = url("confirmConn");
var _oResetTimeout;
var _oConfirmTimeout;
var _pollCount = 0;
var CONNECTFLAG = false;
var CONNECTFAILCOUNT = 0;
var pageUUID = null;

function AppendTxt()
{
	//browserDetect();
	$('#Global_1').html(GlobalConfig.text_scan_hint);
	$('#Global_2').html(GlobalConfig.text_scan_success);
	$('#Global_3').html(GlobalConfig.text_confirm_hint);
	$('#Global_4').html(GlobalConfig.text_bottom_line);
	$('#Global_5').html(GlobalConfig.text_presence_chat);
	$('#Global_6').html(GlobalConfig.text_presence_chat);
	$('#Global_7').html(GlobalConfig.text_presence_away);
	$('#Global_8').html(GlobalConfig.text_presence_dnd);
	$('#Global_9').html(GlobalConfig.text_presence_offline);
	$('#Global_10').html(GlobalConfig.text_friends_subtitle);
	$('#Global_11').html(GlobalConfig.text_addsbook_subtitle);
	$('#Global_12').html(GlobalConfig.text_emj_default);
	$('#Global_13').html(GlobalConfig.text_del_return);
	$('#Global_14').attr("title",GlobalConfig.text_add_chatmem);
	$('#Global_15').attr("title",GlobalConfig.text_del_chatmem);
	$('#chatListSearchTxt').attr("placeholder",GlobalConfig.text_publish_placeholder);
	$('#statusUpdateBtn').html(GlobalConfig.text_publish_btn);
	$('#listOperatorContent_top_item_message').attr("title",GlobalConfig.text_chat_title);
	$('#listOperatorContent_top_item_addressbook').attr("title",GlobalConfig.text_addsbook_title);
	$('#listContentNoMsg').html(GlobalConfig.text_nomsg);
	$('#adbSearchTxt').attr("placeholder",GlobalConfig.text_search_placeholder);
	$('#searchNoResult').html(GlobalConfig.text_nosearch);
	$('#sendEmojiIcon').attr("title",GlobalConfig.text_emj_btn);
	$('#sendImgIcon').attr("title",GlobalConfig.text_img_btn);
	$('#chat-img-upload').attr("value",GlobalConfig.text_file_hint);
	$('#chatPanelSend').html(GlobalConfig.text_send_btn);
	
}

$(document).ready(function(e) {
//	alert(getUUidUrl)
	
	AppendTxt();
	
	
	$.ajax({
        type: "GET",
        url: getUUidUrl,
        
        dataType: "json",
        cache: false,
        success : function(result){
		
            clearTimeout(_oResetTimeout);
            clearTimeout(_oConfirmTimeout);
            _oResetTimeout = setTimeout(function(){
                location.reload();
            }, 5 * 60 *1000);//5 mins
            if(result.status=="2")
    		{
            	pageUUID = result.uuid;
    			login(result.smackjid,result.smackname,result.addsbook,result.avatar,result.xcardsort);
    		}
            else
            {
            	_loadQRImg(result.UUID);
            }
        },
        error:function(result){
        	location.reload();
        }
    });
	
	if($("img.guide").length > 0) {
		var _nTimer = 0,
			_oGuide$ = $(".guide"),
			_oGuideTrigger$ = $("#guideTrigger, #tipTrigger"),
			_oMask$ = $(".mask");

			function _back() {
				_nTimer = setTimeout(function() {
				_oMask$.stop().animate({opacity:0}, function(){$(".mask").hide()});
				_oGuide$.stop().animate({marginLeft:"-120px",opacity:0}, "400", "swing",function(){
					_oGuide$.hide();
				});
			}, 100);
		}

		/*guide*/
		_oGuide$.css({"left":"50%", "opacity":0});
		_oGuideTrigger$.css({"backgroundColor":"white", "opacity":"0"});
		_oGuideTrigger$.mouseover(function(){
			clearTimeout(_nTimer);
			_oMask$.show().stop().animate({"opacity":0.2});
			_oGuide$.css("display", "block").stop().animate({marginLeft:"+168px", opacity:1}, 900, "swing", function() {
				_oGuide$.animate({marginLeft:"+153px"}, 300);
			});
		}).mouseout(_back);

		_oGuide$.mouseover(function(){
			clearTimeout(_nTimer);
		}).mouseout(_back);
	}
}); 

function _loadQRImg(uuid) {
	var _oLoginQrCodeImg = document.getElementById("loginQrCode");
    _oLoginQrCodeImg.src = getQRImgUrl+uuid;
    pageUUID = uuid;
    _poll(uuid);
 
}

function _poll(uuid) {
	$.ajax({
		type: "GET",
		url: getPollUrl+uuid+"&tip=1",
		dataType: "text",
		timeout: 10*1000,
		success: function(data, textStatus, jqXHR) 
		{
		var result =   jQuery.parseJSON(data);
		 if(result.status=="1")				//已扫描，待确认
		 {
			 clearTimeout(_oResetTimeout);
			 if(_pollCount == 0)
			 {
				 _oConfirmTimeout = setTimeout(function(){
	                location.reload();
				 }, 5 * 60 *1000);//5 mins
				 _pollCount  = _pollCount + 1;
			 }
			 $('.normlDesc').hide();
			 $('.successMsg').show();
			 _confirm(uuid);
		 }
		 else if (result.status=="4")		//过期
		 {
			 location.reload();
		 }
		 else
		 {
			 _poll(uuid);
		 }
		},
		error: function(jqXHR, textStatus, errorThrown) {
			if (textStatus == 'timeout') {
				_poll(uuid);
			} else {
			
			}
		}
		});
}

function _confirm(uuid){
	$.ajax({
		type: "GET",
		url: getPollUrl+uuid+"&tip=0",
		dataType: "text",
		timeout: 10*1000,
		success: function(data, textStatus, jqXHR) 
		{
		var result =   jQuery.parseJSON(data);
		if(result.status=="2")				//已确认，登录
		{
			 login(result.smackjid,result.smackname,result.addsbook,result.avatar,result.xcardsort);
		}
		else if(result.status=="4")			//过期
		{
			 location.reload();
		}
		else
		{
			_confirm(uuid);
		}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			if (textStatus == 'timeout') {
				_confirm(uuid);
			} else {
			}
		}
		});
}

function _confirmConn()
{
	
	$.ajax({
		type: "GET",
		url: confirmConnUrl + '?uuid=' +pageUUID,
		dataType: "text",
		timeout: 5*1000,
		success: function(data, textStatus, jqXHR) {
		var result =   jQuery.parseJSON(data);
		if(result.status=="connect")
		{
			 //login(result.smackjid,result.smackname);
			setTimeout(_confirmConn, 5*1000);
		}
		else if (result.status=="disconnect")
		{
			//_confirm(uuid);
			//转回二维码页面
			CONNECTFLAG = false;
			location.reload();
		}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			if (textStatus == 'timeout') {
				//_confirm(uuid);
				CONNECTFAILCOUNT = CONNECTFAILCOUNT+1
				if(CONNECTFAILCOUNT==5)
				{
					location.reload();
				}
				else
				{
					setTimeout(_confirmConn, 5*1000);
				}
			} else {
			
			}
		}
		});
	
}


function login(jid,name,addsbookjson,avatar,xcardsort){
	smackJid = jid;
	smackName = name;
	addsbook = jQuery.parseJSON(addsbookjson);
	xcardSortJson = jQuery.parseJSON(xcardsort);
	if((avatar != null)&&(avatar.length>0))
	{
		avatarBASE64 = avatar;
		$('#avatar-img').attr("src",avatarBASE64);
		$('.chatMemBoxImg').attr("src",avatarBASE64);
		
	}
	
	clearTimeout(_oResetTimeout);
	clearTimeout(_oConfirmTimeout);
	//$("body").html("登录成功,smackJid = "+result.smackjid);
	
	//if (!COMETDSUCCESS) {
	//	setTimeout(_toLogon, 1000)
	//}else{
		$("#login_container").css("display","none");
		$("#container").css("display","block");
		//alert(smackName)
		$("#nickName")[0].innerHTML = smackName;
		displayAddsbook();
		loginSuccess();
		XMPPLOGON = true;
		CONNECTFLAG = true;
		$('#logonbtn').click();
		_confirmConn();
	//}
}

function _toLogon(){
	//if (!COMETDSUCCESS) {
	//	setTimeout(_toLogon, 1000)
	//}else{
		$("#login_container").css("display","none");
		$("#container").css("display","block");
		//alert(smackName)
		$("#nickName")[0].innerHTML = smackName;
		loginSuccess();
		XMPPLOGON = true;
		CONNECTFLAG = true;
		$('#logonbtn').click();
		_confirmConn();
	//}
}


function loadScriptDocWrite (url) {
	    document.write('<scr' + 'ipt src="' + url + '" type="text/javascript"></scr' + 'ipt>');
}

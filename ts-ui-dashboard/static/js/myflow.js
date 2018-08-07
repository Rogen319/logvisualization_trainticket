$("#refresh_my_order_list_button_three").click(function(){
    if(getCookie("loginId").length < 1 || getCookie("loginToken").length < 1){
        alert("Please Login");
    }
    queryMyOrderThree();
});

$("#refresh_my_consign_list_button3").click(function(){
    if(getCookie("loginId").length < 1 || getCookie("loginToken").length < 1){
        alert("Please Login");
    }
    queryMyConsign();
});

function queryMyConsign() {
    var accountid = getCookie("loginId");
    $("#my_consigns_result3").html("");
    $.ajax({
        type: "get",
        url: "/consign/findByAccountId/" + accountid,
        dataType: "json",
        xhrFields: {
            withCredentials: true
        },
        beforeSend: function (xhr) {
            xhr.setRequestHeader("request-type", "Consign");
        },
        success: function(result){
            var size = result.length;
            for(var i = 0; i < size;i++){
                var order = result[i];
                var fromString = getStationNameById(order['from']);
                var toString  = getStationNameById(order['to']);
                $("#my_consigns_result3").append(
                    "<div class='panel panel-default'>" +
                    "<div class='panel-heading'>" +
                    "<h4 class='panel-title'>" +
                    "<label>" +
                    // "<a data-toggle='collapse' href='#collapse" + i + "'>" +
                    "From:" + fromString + "    ----->    To:" + toString +
                    // "</a>" +
                    "</label>" +
                    "</h4>" +
                    "</div>" +
                    "<div>" +
                    "<div id='collapse" + i + "' class='panel-collapse collapse in'>" +
                    "<div class='panel-body'>" +
                    "<form role='form' class='form-horizontal'>" +
                    "<div class='div form-group'>" +
                    "<label class='col-sm-2 control-label'>Consign ID: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_consign_list_id'>" + order["id"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='div form-group'>" +
                    "<label class='col-sm-2 control-label'>From: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_consign_list_from'>" + fromString + "</label>" +
                    "<label class='control-label noshow_component my_consign_list_from_id'>" + order["from"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='div form-group'>" +
                    "<label class='col-sm-2 control-label'>To: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_consign_list_to'>" + toString + "</label>" +
                    "<label class='control-label noshow_component my_consign_list_to_id'>" + order["to"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Handle Date: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_consign_list_handle_date'>" + convertNumberToDateTimeString(order["handleDate"]) + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Target Date: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_consign_list_target_date'>" + convertNumberToDateTimeString(order["targetDate"]) + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Consignee: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label'>" + order["consignee"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Phone: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label'>" + order["phone"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Price: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_consign_list_price'>" + order["price"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Weight: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label'>" + order["weight"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "</form>" +
                    "</div>" +
                    "</div>" +
                    "</div>"
                );
            }
            addListenerToOrderCancel();
            addListenerToOrderChange();
            addListenerToPayOrderButton();
            addListenerToOrderConsign();
        }
    });
}

function queryMyOrderThree(){
    var myOrdersQueryInfo = new Object();
    myOrdersQueryInfo.enableStateQuery = false;
    myOrdersQueryInfo.enableTravelDateQuery = false;
    myOrdersQueryInfo.enableBoughtDateQuery = false;
    myOrdersQueryInfo.travelDateStart = null;
    myOrdersQueryInfo.travelDateEnd = null;
    myOrdersQueryInfo.boughtDateStart = null;
    myOrdersQueryInfo.boughtDateEnd = null;
    var myOrdersQueryData = JSON.stringify(myOrdersQueryInfo);
    $("#my_orders_result_three").html("");
    queryForMyOrderThree("/order/query",myOrdersQueryData);
    queryForMyOrderThree("/orderOther/query",myOrdersQueryData);
}

function queryForMyOrderThree(path,data){
    $.ajax({
        type: "post",
        url: path,
        contentType: "application/json",
        dataType: "json",
        data:data,
        xhrFields: {
            withCredentials: true
        },
        beforeSend: function (xhr) {
            xhr.setRequestHeader("request-type", "QueryOrder");
        },
        success: function(result){
            var size = result.length;
            for(var i = 0; i < size;i++){
                var order = result[i];
                var fromString = getStationNameById(order['from']);
                var toString  = getStationNameById(order['to']);
                $("#my_orders_result_three").append(
                    "<div class='panel panel-default'>" +
                    "<div class='panel-heading'>" +
                    "<h4 class='panel-title'>" +
                    "<label>" +
                    // "<a data-toggle='collapse' href='#collapse" + i + "'>" +
                    "From:" + fromString + "    ----->    To:" + toString +
                    // "</a>" +
                    "</label>" +
                    "</h4>" +
                    "</div>" +
                    "<div>" +
                    "<div id='collapse" + i + "' class='panel-collapse collapse in'>" +
                    "<div class='panel-body'>" +
                    "<form role='form' class='form-horizontal'>" +
                    "<div class='div form-group'>" +
                    "<label class='col-sm-2 control-label'>Order ID: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_id'>" + order["id"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='div form-group'>" +
                    "<label class='col-sm-2 control-label'>From: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_from'>" + fromString + "</label>" +
                    "<label class='control-label noshow_component my_order_list_from_id'>" + order["from"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='div form-group'>" +
                    "<label class='col-sm-2 control-label'>To: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_to'>" + toString + "</label>" +
                    "<label class='control-label noshow_component my_order_list_to_id'>" + order["to"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Bought Date: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_bought_date'>" + convertNumberToDateTimeString(order["boughtDate"]) + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Trip Id: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_train_number'>" + order["trainNumber"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Seat Number: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_seat'>" + order["seatNumber"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Status: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='noshow_component my_order_list_status'>" + order["status"] + "</label>" +
                    "<label class='control-label'>" + convertNumberToOrderStatus(order["status"]) + "</label>" +
                    addPayButtonOrNot(order["status"]) +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Price: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_price'>" + order["price"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Name: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label my_order_list_contactname'>" + order["contactsName"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Document Type: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label'>" + convertNumberToDocumentType(order["documentType"]) + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>DocumentNumber: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='control-label'>" + order["contactsDocumentNumber"] + "</label>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Operation: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='order_id control-label noshow_component' >" + order["id"] + "</label>" +
                    "<label class='order_id control-label noshow_component my_order_list_accountId' >" + order["accountId"] + "</label>" +
                    addCancelAandRebookButtonOrNot(order) +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Consign: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='order_id control-label noshow_component' >" + order["id"] + "</label>" +
                    "<label class='order_id control-label noshow_component my_order_list_accountId' >" + order["accountId"] + "</label>" +
                    addConsignButtonOrNot(order) +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<label class='col-sm-2 control-label'>Voucher: </label>" +
                    "<div class='col-sm-10'>" +
                    "<label class='order_id control-label noshow_component' >" + order["id"] + "</label>" +
                    "<label class='order_id control-label noshow_component my_order_list_accountId' >" + order["accountId"] + "</label>" +
                    addVoucherButtonOrNot(order) +
                    "</div>" +
                    "</div>" +
                    "</form>" +
                    "</div>" +
                    "</div>" +
                    "</div>"
                );
            }
            addListenerToOrderCancel();
            addListenerToOrderChange();
            addListenerToPayOrderButton();
            addListenerToOrderConsign();
            addListenerToVoucherPrint();
        }
    });
}

function addConsignButtonOrNot(order){
    var str = "";
    if(order["status"] == 0 || order['status'] == 1 || order["status"] == 2){
        str += "<button type='button' class='ticket_consign_btn btn btn-primary'>" + "Consign" + "</button>";
    }
    else{
        str = "Not operable";
    }
    return str;
}

function addVoucherButtonOrNot(order){
    var str = "";
    if(order["status"] == 6){
        str = "<button type='button' class='voucher_print_btn btn btn-primary'>" + "Print Voucher" + "</button>";
    }
    else{
        str = "Not operable";
    }
    return str;
}

function addListenerToOrderConsign(){
    var consignButtonSet = $(".ticket_consign_btn");
    for(var i = 0;i < consignButtonSet.length;i++) {
        consignButtonSet[i].onclick = function () {
            var consignInfo = new Object();
            consignInfo.accountId = getCookie("loginId");
            var date = new Date();
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var currentdate = year + seperator1 + month + seperator1 + strDate;
            consignInfo.handleDate = currentdate;
            consignInfo.targetDate = $(this).parents("form").find(".my_order_list_bought_date").text();
            consignInfo.from = $(this).parents("form").find(".my_order_list_from_id").text();
            consignInfo.to = $(this).parents("form").find(".my_order_list_to_id").text();
            var consignee = prompt("Please input the name of consignee:");
            if(consignee != null){
                var phone = prompt("Please input the phone of consignee:");
                if(phone != null){
                    var weight = prompt("Please input the weight of the consigned thing: ");
                    if(weight != null){
                        consignInfo.consignee = consignee;
                        consignInfo.phone = phone;
                        consignInfo.weight = weight;
                        consignInfo.isWithin = false;
                        var data = JSON.stringify(consignInfo);
                        $.ajax({
                            type: "post",
                            url: "/consign/insertConsign",
                            contentType: "application/json",
                            dataType: "json",
                            data: data,
                            xhrFields: {
                                withCredentials: true
                            },
                            beforeSend: function (xhr) {
                                xhr.setRequestHeader("request-type", "Consign");
                            },
                            success: function(result){
                                if(result["status"] == true){
                                    alert(result["message"]);
                                }else{
                                    alert(result["message"]);
                                }
                            }
                        });
                    }
                    else{
                        alert("You have canceled!");
                    }
                }else{
                    alert("You have canceled!");
                }
            } else{
                alert("You have canceled!");
            }
        }
    }
}

function addListenerToVoucherPrint() {
    var voucherButtonSet = $(".voucher_print_btn");
    for (var i = 0; i < voucherButtonSet.length; i++) {
        voucherButtonSet[i].onclick = function () {
            var url = "voucher.html?orderId=" + escape($(this).parents("form").find(".my_order_list_id").text());
            // url += "&from=" + escape($(this).parents("form").find(".my_order_list_from").text());
            // url += "&to=" + escape($(this).parents("form").find(".my_order_list_to").text());
            // url += "&bought_date=" + escape($(this).parents("form").find(".my_order_list_bought_date").text());
            // url += "&price=" + escape($(this).parents("form").find(".my_order_list_price").text());
            // url += "&passenger=" + escape($(this).parents("form").find(".my_order_list_contactname").text());
            // url += "&seat=" + escape($(this).parents("form").find(".my_order_list_seat").text());
            url += "&train_number=" + escape($(this).parents("form").find(".my_order_list_train_number").text());
            location.href = url;
        }
    }
}
package com.BE.QuitCare.enums;

public enum MessageTypeStatus
{
    NOTIFICATION1,// Cảnh báo nếu số điếu tăng
    NOTIFICATION2,// khen thưởng nếu số điếu giảm
    NOTIFICATION3,// nếu tick một triệu chứng liên tiếp 3 ngày thì thông báo đặt lịch khám với Coach để biết chính xác tin trạng sức khỏe
    NOTIFICATION4 //nếu tick hơn 2  triệu chứng liên tiếp 3 ngày
}

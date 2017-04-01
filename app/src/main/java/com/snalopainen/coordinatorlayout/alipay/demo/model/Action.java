package com.snalopainen.coordinatorlayout.alipay.demo.model;


import com.snalopainen.coordinatorlayout.alipay.demo.R;

/**
 * Created by snajdan on 2017/30/3.
 * 声明：以下数据结构一一对应后台返回数据，不代表本人设计思路。
 */

public class Action implements BaseModel {
    public String TypeName = "收银台";
    public int TypeId = R.drawable.icon_button_cashier;
    public String TypeImg;
//    public ArrayList<SubType> TypeList;
//    public ArrayList<HotKeyword> HotKeyword;

    /* 非服务器返回数据，本地定义：首页"更多类型" */
//    private boolean more;
//
//    public static Action createMoreType(String typeId) {
//        Action more = new Action();
//        more.more = true;
//        more.TypeId = typeId;
//        more.TypeName = "更多";
//        more.TypeImg=null;
//        return more;
//    }
//
//    public boolean isMore() {
//        return more;
//    }
//
//    public static class SubType implements BaseModel {
//        public String SubTypeImg;
//        public String SubTypeName;
//        public String SubTypeId;
//        public ArrayList<ThirdType> SubTypeList;
//    }
//
//    public static class ThirdType implements BaseModel {
//        public String ThirdTypeId;
//        public String ThirdTypeName;
//        public String ThirdTypePic;
//    }
//
//    public static class HotKeyword implements BaseModel {
//        public String keyword;
//    }
}

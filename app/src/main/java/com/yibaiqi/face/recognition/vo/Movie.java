package com.yibaiqi.face.recognition.vo;

import java.util.List;

/**
 * Created by @author xiaofu on 2019/1/22.
 */
public class Movie {

    /**
     * msg : success
     * retCode : 200
     * result : [{"cur":1747.74,"days":5,"name":"叶问3","sum":53467.76}]
     */

    private String msg;
    private String retCode;
    private List<ResultBean> result;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * cur : 1747.74
         * days : 5
         * name : 叶问3
         * sum : 53467.76
         */

        private double cur;
        private int days;
        private String name;
        private double sum;

        public double getCur() {
            return cur;
        }

        public void setCur(double cur) {
            this.cur = cur;
        }

        public int getDays() {
            return days;
        }

        public void setDays(int days) {
            this.days = days;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getSum() {
            return sum;
        }

        public void setSum(double sum) {
            this.sum = sum;
        }
    }
}

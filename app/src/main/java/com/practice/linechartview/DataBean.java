package com.practice.linechartview;

import java.io.Serializable;

/**
 * @author 黎亮亮
 * @Date 2021/9/10
 * @describe
 */
public class DataBean implements Serializable {

    private String date;
    private String price;
    private int xCut;
    private int yCut;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getxCut() {
        return xCut;
    }

    public void setxCut(int xCut) {
        this.xCut = xCut;
    }

    public int getyCut() {
        return yCut;
    }

    public void setyCut(int yCut) {
        this.yCut = yCut;
    }
}

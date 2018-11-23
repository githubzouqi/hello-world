package com.example.pc2.carmapproject.interfaces;

/**
 * 下线货架后，给mq发消息移除地图上的货架
 * 接口方法用来获取移除货架的id
 */
public interface onPodRemoveListener {

    void removePod(int podCodeID);

}

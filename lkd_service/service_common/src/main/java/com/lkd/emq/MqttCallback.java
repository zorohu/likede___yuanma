package com.lkd.emq;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MqttCallback implements MqttCallbackExtended {
    //需要订阅的topic配置
    @Value("${mqtt.consumer.consumerTopics}")
    private List<String> consumerTopics;

    @Autowired
    private MqttService mqttService;

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("emq error.",throwable);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info( "topic:"+topic+"  message:"+ new String(message.getPayload())   );
        //处理消息
        mqttService.processMessage(topic, message);
        //处理成功后确认消息
        mqttClient.messageArrivedComplete(message.getId(),message.getQos());
    }

    /**
     *功能描述  消息发送成功后，回调该方法，判断是否发送成功。
     * @author liYuan
     * @date 2022/8/3 15:17
      * @param iMqttDeliveryToken
     * @return void
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("deliveryComplete---------" + iMqttDeliveryToken.isComplete());
    }


    @Override
    public void connectComplete(boolean b, String s) {
        //和EMQ连接成功后根据配置自动订阅topic
        if(consumerTopics != null && consumerTopics.size() > 0){
            consumerTopics.forEach(t->{
                try {
                        log.info(">>>>>>>>>>>>>>subscribe topic:"+t);
                        mqttClient.subscribe(t, 2);
                    } catch (MqttException e) {
                        log.error("emq connect error", e);
                    }
            });
        }
    }

    private MqttClient mqttClient;
    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }
}
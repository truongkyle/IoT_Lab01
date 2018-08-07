package com.example.ritodoji.ledcontrolmqtt;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.IOException;

public class MqttControl implements MqttCallback{
    private static final String username = "omnsnfxm";
    private static final String password = "2g50mT2Xl3Cx";
    private static final String serveruri = "tcp://m14.cloudmqtt.com:13457";
    private static final String clientId = "raspberry";
    private static final String toPic = "smarthome/led/control";
    private static final String topicTemp ="smarthome/temp";
    private static final String TAG = MqttControl.class.getSimpleName();
    private static final  int QOs = 1;
    public MqttClient client ;
    private MqttInterface mqttInterface;
    private String pay ;
    private Context context ;
    private Gpio ledpin;

    public interface MqttInterface{
        void getMessage(String payload);
    }
    public  MqttControl(Context context, MqttInterface mqttControl, Gpio ledpinz) throws MqttException {
        this.mqttInterface = mqttControl;
        this.context = context;
        this.ledpin = ledpinz;
        subcribeToTopic();
    }
    public void subcribeToTopic() throws MqttException {
        client = new MqttClient(serveruri,clientId,new MemoryPersistence());
        client.connect(connectOptionchoice());
        client.subscribe(toPic,0);
        client.subscribe(topicTemp,1);
        client.setCallback(this);
        Log.d(TAG,"nice connected ~");
        Toast.makeText(context,"MQTT onSuccess !", Toast.LENGTH_LONG).show();
    }

    public static MqttConnectOptions connectOptionchoice(){
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setCleanSession(false);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setKeepAliveInterval(30);
        return connectOptions;
    }

    public void close() throws MqttException {
        client.disconnect();
        client.close();
        Log.d(TAG,"client  disconnected ! ");
    }

    public void sendmessage(String data, String topic) throws MqttException {
        if(!client.isConnected()){
           subcribeToTopic();
        }
        MqttMessage message = new MqttMessage(data.getBytes());
        message.setQos(QOs);
        client.publish(topic,message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        try {
            Log.d(TAG,"Reconnecting ~ ................(-______-)#......");
            client.connect(MqttControl.connectOptionchoice());
            Log.d(TAG,"Connected success ! ");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws IOException, MqttException {
        String payload = new String(message.getPayload());
        switch (payload) {
            case "LED_1_ON":
                Log.d(TAG, "LED 1 ON");
                ledpin.setValue(true);
                sendmessage("1_ON","smarthome/led/state");
                break;
            case "LED_1_OFF":
                Log.d(TAG, "LED OFF");
                ledpin.setValue(false);
                sendmessage("1_OFF","smarthome/led/state");
                break;
            case "LED_2_ON":
                break;
            case "LED_2_OFF":
                break;
            case "PLAY_MUSIC":
                break;



        }
        Log.d(TAG, "done mess");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}

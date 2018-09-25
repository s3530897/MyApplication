package com.example.aomeng.pepperdemo;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.net.http.Headers;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.ConversationStatus;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.EngageHuman;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.EncodedImageHandle;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;
import com.aldebaran.qi.sdk.object.touch.TouchState;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private ConversationStatus conversationStatus;
    private Animation animation;
    private TouchSensor headTouchSensor;
    private HumanAwareness humanAwareness;
    private Chat chat;
    private Button button;
    private Future<Void> engageHumanFuture;
    TextView textView;
    // An image view used to show the picture.
    private ImageView pictureView;
    // The QiContext provided by the QiSDK.
    private QiContext qiContext;
    // TimestampedImage future.
    Future<TimestampedImageHandle> timestampedImageHandleFuture;
    Future<Void> chatFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QiSDK.register(this, this);
        button = (Button) findViewById(R.id.button);
        pictureView = findViewById(R.id.picture_view);
        textView=(TextView)findViewById(R.id.textview);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeTouPicture();

            }
        });


    }

    //定义Handler对象
    private Handler handler =new Handler(){
        @Override
//当有消息发送出来的时候就执行Handler的这个方法
        public void handleMessage(Message msg){
            super.handleMessage(msg);
//处理UI
        }
    };

    public void takePicture() {
        // Check that the Activity owns the focus.
        Future<TakePicture> takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync();
        Future<TimestampedImageHandle> timestampedImageHandleFuture = takePictureFuture.andThenCompose(new Function<TakePicture, Future<TimestampedImageHandle>>() {
            @Override
            public Future<TimestampedImageHandle> execute(TakePicture takePicture) {
                return takePicture.async().run();
            }
        });
        timestampedImageHandleFuture.andThenConsume(new Consumer<TimestampedImageHandle>() {
            @Override
            public void consume(TimestampedImageHandle timestampedImageHandle) throws Throwable {
                //Consume take picture action when it's ready
                //Log.i(TAG, "Picture taken");
                // get picture
                EncodedImageHandle encodedImageHandle = timestampedImageHandle.getImage();

                EncodedImage encodedImage = encodedImageHandle.getValue();
                //Log.i(TAG, "PICTURE RECEIVED!");

                // get the byte buffer and cast it to byte array
                ByteBuffer buffer = encodedImage.getData();
                buffer.rewind();
                final int pictureBufferSize = buffer.remaining();
                final byte[] pictureArray = new byte[pictureBufferSize];
                buffer.get(pictureArray);
                String s = new String(Base64.encodeBase64(pictureArray));
                Map map=post("https://cv.icarbonx.com/api/food_cls_qlt",new BasicNameValuePair("image_base64", s));
                Map nutmap=post("https://cv.icarbonx.com/api/food_nutrition",new BasicNameValuePair("image_base64", s));
                String finalresults="";
                Iterator entries = nutmap.entrySet().iterator();
                Log.i("TTTagprint","测试19234");
                boolean flag2=true;
                while (entries.hasNext()&&flag2==true) {
                    flag2=false;
                    Map.Entry entry = (Map.Entry) entries.next();
                    Map value = (Map)entry.getValue();
                    Log.i("TTTagprint",value.toString());

                    //
                    boolean flag=true;
                    Iterator rrrentries = value.entrySet().iterator();
                    while (rrrentries.hasNext()&&flag==true) {
                        flag=false;
                        Map.Entry rrrentry = (Map.Entry) rrrentries.next();
                        value=(Map)rrrentry.getValue();
                        finalresults+="脂肪"+((Map)value.get("脂肪")).get("value").toString()+((Map)value.get("脂肪")).get("unit").toString()+"\n";
                        finalresults+="能量"+((Map)value.get("能量")).get("value").toString()+((Map)value.get("能量")).get("unit").toString()+"\n";
                        finalresults+="碳水化合物"+((Map)value.get("碳水化合物")).get("value").toString()+((Map)value.get("碳水化合物")).get("unit").toString()+"\n";
                        finalresults+="胆固醇"+((Map)value.get("胆固醇")).get("value").toString()+((Map)value.get("胆固醇")).get("unit").toString()+"\n";
                        finalresults+="水分"+((Map)value.get("水分")).get("value").toString()+((Map)value.get("水分")).get("unit").toString()+"\n";
                        finalresults+="蛋白质"+((Map)value.get("蛋白质")).get("value").toString()+((Map)value.get("蛋白质")).get("unit").toString()+"\n";
                        /*
                        Iterator nutentries = value.entrySet().iterator();
                        while (nutentries.hasNext()) {
                            Map.Entry nutentry = (Map.Entry) nutentries.next();
                            Log.i("TTTagprint",nutentry.getKey().toString());
                            Log.i("TTTagprint",nutentry.getValue().toString());
                            String key = nutentry.getKey().toString();
                            Log.i("TTTagprint",nutentry.getKey().toString());
                            finalresults+= key+":";
                            Map nutvalue = (Map) nutentry.getValue();
                            finalresults+= nutvalue.get("value").toString()+" "+nutvalue.get("unit").toString()+"\n";
                        }*/
                    }
                }
                //Log.i(TAG, "PICTURE RECEIVED! (" + pictureBufferSize + " Bytes)");
                // display picture
                String name= map.get("name").toString();
                String[] names= name.split("$");
                names=name.split("@");
                name=names[1];
                String kilo=names[0];
                name+="\n"+finalresults;
                final String foodname=name;
                final Bitmap pictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize);
                List list;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pictureView.setImageBitmap(pictureBitmap);
                        textView.setText(foodname);
                    }
                });

            }
        });
        if (qiContext == null) {
            return;
        }
    }
        // Disable the button.
        //button.setEnabled(false);

    public void takeRecogPicture() {
        // Check that the Activity owns the focus.
        Future<TakePicture> takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync();
        Future<TimestampedImageHandle> timestampedImageHandleFuture = takePictureFuture.andThenCompose(new Function<TakePicture, Future<TimestampedImageHandle>>() {
            @Override
            public Future<TimestampedImageHandle> execute(TakePicture takePicture) {
                return takePicture.async().run();
            }
        });
        timestampedImageHandleFuture.andThenConsume(new Consumer<TimestampedImageHandle>() {
            @Override
            public void consume(TimestampedImageHandle timestampedImageHandle) throws Throwable {
                //Consume take picture action when it's ready
                //Log.i(TAG, "Picture taken");
                // get picture
                EncodedImageHandle encodedImageHandle = timestampedImageHandle.getImage();

                EncodedImage encodedImage = encodedImageHandle.getValue();
                //Log.i(TAG, "PICTURE RECEIVED!");

                // get the byte buffer and cast it to byte array
                ByteBuffer buffer = encodedImage.getData();
                buffer.rewind();
                final int pictureBufferSize = buffer.remaining();
                final byte[] pictureArray = new byte[pictureBufferSize];
                buffer.get(pictureArray);
                String s = new String(Base64.encodeBase64(pictureArray));
                //Map map=post("https://cv.icarbonx.com/api/food_cls_qlt",new BasicNameValuePair("image_base64", s));
                Map nutmap=post("http://139.199.67.41:8058/api/face_re",new BasicNameValuePair("image_base64", s),new BasicNameValuePair("maxdist","0.92"));
                String finalresults="";
                Iterator entries = nutmap.entrySet().iterator();
                Log.i("TTTagprint","测试19234");
                //Log.i(TAG, "PICTURE RECEIVED! (" + pictureBufferSize + " Bytes)");
                // display picture
                //String name= map.get("name").toString();
                final String humanname=nutmap.get("message").toString();
                final Bitmap pictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize);
                List list;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pictureView.setImageBitmap(pictureBitmap);
                        //textView.setText(humanname);
                    }
                });
                if(humanname.equals("can not find face")||humanname.equals("unknow")) {
                    String textToSay = "欢迎光临，好久不见,拍拍我的头我能帮你识别各种信息";
                    final Say say = SayBuilder.with(qiContext)
                            .withText(textToSay)
                            .build();
                    say.async().run();
                }else{
                    String textToSay = "欢迎光临"+humanname+"，好久不见,拍拍我的头我能帮你识别各种信息";
                    final Say say = SayBuilder.with(qiContext)
                            .withText(textToSay)
                            .build();
                    say.async().run();
                }



            }
        });
        if (qiContext == null) {
        }
    }

    public void takeTouPicture() {
        // Check that the Activity owns the focus.
        Future<TakePicture> takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync();
        Future<TimestampedImageHandle> timestampedImageHandleFuture = takePictureFuture.andThenCompose(new Function<TakePicture, Future<TimestampedImageHandle>>() {
            @Override
            public Future<TimestampedImageHandle> execute(TakePicture takePicture) {
                return takePicture.async().run();
            }
        });
        timestampedImageHandleFuture.andThenConsume(new Consumer<TimestampedImageHandle>() {
            @Override
            public void consume(TimestampedImageHandle timestampedImageHandle) throws Throwable {
                //Consume take picture action when it's ready
                //Log.i(TAG, "Picture taken");
                // get picture
                EncodedImageHandle encodedImageHandle = timestampedImageHandle.getImage();

                EncodedImage encodedImage = encodedImageHandle.getValue();
                //Log.i(TAG, "PICTURE RECEIVED!");

                // get the byte buffer and cast it to byte array
                ByteBuffer buffer = encodedImage.getData();
                buffer.rewind();
                final int pictureBufferSize = buffer.remaining();
                final byte[] pictureArray = new byte[pictureBufferSize];
                buffer.get(pictureArray);
                String s = new String(Base64.encodeBase64(pictureArray));
                //Map map=post("https://cv.icarbonx.com/api/food_cls_qlt",new BasicNameValuePair("image_base64", s));
                Map nutmap=post("http://139.199.67.41:8054/api/tonguecls",new BasicNameValuePair("image_base64", s));
                String finalresults="";
                Iterator entries = nutmap.entrySet().iterator();
                Log.i("TTTagprint","测试19234");
                //Log.i(TAG, "PICTURE RECEIVED! (" + pictureBufferSize + " Bytes)");
                // display picture
                //String name= map.get("name").toString();
                Float aFloat=Float.valueOf(nutmap.get("message").toString());
                final String humanname=nutmap.get("message").toString();
                final Bitmap pictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize);
                String textToS ="你的"+nutmap.get("sz").toString()+nutmap.get("ts").toString()+","+nutmap.get("reason").toString()+nutmap.get("symptom").toString();
                final String textToSay=textToS;
                if(aFloat<0)
                    return;
                List list;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pictureView.setImageBitmap(pictureBitmap);

                    }
                });
                final Say say = SayBuilder.with(qiContext)
                        .withText(textToSay)
                        .build();
                say.async().run();
                //textView.setText(humanname);
            }
        });
        if (qiContext == null) {
        }
    }

        public void takeHumanPicture() {
            // Check that the Activity owns the focus.
            Future<TakePicture> takePictureFuture = TakePictureBuilder.with(qiContext).buildAsync();
            Future<TimestampedImageHandle> timestampedImageHandleFuture = takePictureFuture.andThenCompose(new Function<TakePicture, Future<TimestampedImageHandle>>() {
                @Override
                public Future<TimestampedImageHandle> execute(TakePicture takePicture) {
                    return takePicture.async().run();
                }
            });
            timestampedImageHandleFuture.andThenConsume(new Consumer<TimestampedImageHandle>() {
                @Override
                public void consume(TimestampedImageHandle timestampedImageHandle) throws Throwable {
                    //Consume take picture action when it's ready
                    //Log.i(TAG, "Picture taken");
                    // get picture
                    EncodedImageHandle encodedImageHandle = timestampedImageHandle.getImage();

                    EncodedImage encodedImage = encodedImageHandle.getValue();
                    //Log.i(TAG, "PICTURE RECEIVED!");

                    // get the byte buffer and cast it to byte array
                    ByteBuffer buffer = encodedImage.getData();
                    buffer.rewind();
                    final int pictureBufferSize = buffer.remaining();
                    final byte[] pictureArray = new byte[pictureBufferSize];
                    buffer.get(pictureArray);

                    final Bitmap pictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize);
                    List list;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pictureView.setImageBitmap(pictureBitmap);
                        }
                    });

                }
            });
            if (qiContext == null) {
                return;
            }
        }


    @Override
    protected void onDestroy(){
        QiSDK.unregister(this,this);
        super.onDestroy();
    }
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        humanAwareness = qiContext.getHumanAwareness();

        humanAwareness.addOnHumansAroundChangedListener(new HumanAwareness.OnHumansAroundChangedListener() {
            @Override
            public void onHumansAroundChanged(List<Human> humans) {
                Log.i("HumanTag", "HumansAround Changed");
                if (humans.size() == 0) return;
                // If Pepper is not already engaged
                if (engageHumanFuture == null || engageHumanFuture.isDone()){
                    Log.i("HumanTag", "start engaging human");
                    startEngagingHuman(getClosestHuman(humans));
                }
            }
        });
        // Get the Touch service from the QiContext.
        Touch touch = qiContext.getTouch();
        headTouchSensor = touch.getSensor("Head/Touch");
        // Add onStateChanged listener.
        final Say say3 = SayBuilder.with(qiContext)
                .withText("哎呀,你拿的食物好不健康啊，快扔掉")
                .build();
        final String message = "happy!!!!";
        headTouchSensor.addOnStateChangedListener(new TouchSensor.OnStateChangedListener() {
            @Override
            public void onStateChanged(TouchState touchState) {
                takePicture();
                say3.async().run();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(message);
                    }
                });
                chatFuture = chat.async().run();

                chatFuture.thenConsume(new Consumer<Future<Void>>() {
                    @Override
                    public void consume(Future<Void> future) throws Throwable {
                        if (future.hasError()) {
                        }
                    }
                });
            }
        });


        Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.greetings) // Set the topic resource.
                .build(); // Build the topic.

        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chat.addOnStartedListener(new Chat.OnStartedListener() {
            @Override
            public void onStarted() {

            }
        });
        chat.addOnHeardListener(new Chat.OnHeardListener() {
            @Override
            public void onHeard(Phrase heardPhrase) {
                if(heardPhrase!=null)
                    Log.i("TTTag", heardPhrase.getText());
                    String s=heardPhrase.getText();
                    new MyThread(s).start();
            }
        });

        //重要

        /*
        Say say = SayBuilder.with(qiContext).withText("你好我是数字生命管家").build();
        Topic topic = TopicBuilder.with(qiContext).withResource(R.raw.raw).build();
        animation =  AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.show_head_a001) // Set the animation resource.
                .build();
        Animate animate=AnimateBuilder.with(qiContext).withAnimation(animation).build();
        conversationStatus = qiContext.getConversation().status(qiContext.getRobotContext());
        conversationStatus.addOnHeardListener(new ConversationStatus.OnHeardListener() {
            @Override
            public void onHeard(Phrase heardPhrase) {
                Log.i("myTag", heardPhrase.getText());
            }
        });

        Chat chat= ChatBuilder.with(qiContext).

                build();

        chat.addOnStartedListener(new Chat.OnStartedListener() {
            @Override
            public void onStarted() {
                System.console().printf("开始了么？");
            }
        });
        chat.addOnNoReplyFoundForListener(new Chat.OnNoReplyFoundForListener() {
            @Override
            public void onNoReplyFoundFor(Phrase input) {
                System.console().printf("没有发现");
            }
        });
       // Chatbot chatbot= QiChatbotBuilder.with(qiContext).withTopic()
    //    chat.run();
        say.async().run();
        animate.run();*/
    }

    @Override
    public void onRobotFocusLost() {
        humanAwareness.removeAllOnEngagedHumanChangedListeners();
        this.qiContext = null;
        if (chat != null) {
            chat.removeAllOnStartedListeners();
            chat.removeAllOnHeardListeners();
        }
        /*
        if (conversationStatus != null) {
            conversationStatus.removeAllOnHeardListeners();
        }*/
    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }


    public Map post(String url,BasicNameValuePair basicNameValuePair) {
      // 创建HttpPost对象
        Map docType = new HashMap();
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        // 创建传递参数集合
        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        paramsList.add(basicNameValuePair);
      // 设置字符集
      try {
          post.setEntity(new UrlEncodedFormEntity(paramsList, HTTP.UTF_8));
          Log.i("TTTag","测试一");
          HttpResponse response = client.execute(post);
            Log.i("TTTag","测试一.5");
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          Log.i("TTTag","测试二");
          HttpEntity httpEntity=response.getEntity();
          String strResult = EntityUtils.toString(httpEntity,"utf-8");
          //转MAP
          JsonObject returnData = new JsonParser().parse(strResult).getAsJsonObject();
          Log.i("TTTagMap",returnData.toString());
          Gson gson = new Gson();
          docType= gson.fromJson(strResult,Map.class);

          Log.i("TTTag",docType.toString());
          //JSONObject jo = JSONObject.fromObject(strResult);
          //Log.i("TTTag",jo.toString());
      }
          Log.i("TTTag","测试三");
    } catch (UnsupportedEncodingException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
    } catch (ClientProtocolException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
    } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
    }
        return docType;
    }

    //post3
    public Map post(String url,BasicNameValuePair basicNameValuePair,BasicNameValuePair basicNameValuePair2) {
        // 创建HttpPost对象
        Map docType = new HashMap();
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        // 创建传递参数集合
        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        paramsList.add(basicNameValuePair);
        paramsList.add(basicNameValuePair2);
        // 设置字符集
        try {
            post.setEntity(new UrlEncodedFormEntity(paramsList, HTTP.UTF_8));
            Log.i("TTTag","测试一");
            HttpResponse response = client.execute(post);
            Log.i("TTTag","测试一.5");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.i("TTTag","测试二");
                HttpEntity httpEntity=response.getEntity();
                String strResult = EntityUtils.toString(httpEntity,"utf-8");
                //转MAP
                JsonObject returnData = new JsonParser().parse(strResult).getAsJsonObject();
                Log.i("TTTagMap",returnData.toString());
                Gson gson = new Gson();
                docType= gson.fromJson(strResult,Map.class);

                Log.i("TTTag",docType.toString());
                //JSONObject jo = JSONObject.fromObject(strResult);
                //Log.i("TTTag",jo.toString());
            }
            Log.i("TTTag","测试三");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return docType;
    }

    //post2

    public Map post(String url,BasicNameValuePair basicNameValuePair,Boolean b) {
        // 创建HttpPost对象
        Map docType = new HashMap();
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        // 创建传递参数集合
        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        String sssss="{'question':'主食'}";
        // 设置字符集
        try {
            post.setEntity(new StringEntity(sssss, HTTP.UTF_8));
            post.setHeader("Content-Type","application/json");
            post.setHeader("Authorization","EndpointKey f0f30e69-8bf2-4beb-ad04-a63ef9c05cb0");

            Log.i("TTTagLLL","测试一");
            HttpResponse response = client.execute(post);
            Log.i("TTTagLLL","测试一.5");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.i("TTTagLLL","测试二");
                HttpEntity httpEntity=response.getEntity();
                String strResult = EntityUtils.toString(httpEntity,"utf-8");
                //转MAP
                JsonObject returnData = new JsonParser().parse(strResult).getAsJsonObject();
                Log.i("TTTagMapLLL",returnData.toString());
                Gson gson = new Gson();
                docType= gson.fromJson(strResult,Map.class);
                List<Map> docTypes=(List<Map>) docType.get("answers");
                docType=docTypes.get(0);
                Log.i("TTTagLLL",docType.get("answer").toString());
                //JSONObject jo = JSONObject.fromObject(strResult);
                //Log.i("TTTag",jo.toString());
            }
            Log.i("TTTagLLLMain",docType.get("answer").toString());
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return docType;
    }
    public class MyThread extends Thread{
        private String ss;
        public MyThread(String s){
            ss=s;
        }
        public void run(){
        //你要执行的方法
            String s;
            Log.i("TTTagReturn",ss);
            if(ss.equals("<...>")||ss==null)
                s="我并没有听见你在说什么";
            else{
                if(ss.equals("你觉得最近我瘦了么")||ss.equals("那我选_ 马克")||ss.equals("那我选_ 佩佩")||ss.equals("我为什么瘦不下来")||ss.equals("有什么办法么")||ss.equals("给我推荐一个教练吧")){
                    if(ss.equals("那我选_ 马克")||ss.equals("那我选_ 佩佩")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("支付提示")
                                        .setMessage("请确认支付500元")
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //do nothing - it will close on its own
                                            }
                                        })
                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .show();
                            }
                        });
                    }else {
                        ;
                    }
                }else {
                    ;
                    /*
                    Map map2=post("http://118.89.14.44:50056/nlp/api/v2.0/health_qa",new BasicNameValuePair("msg", ss));
                    Map map=post("https://diabete.azurewebsites.net/qnamaker/knowledgebases/44af971e-5765-46eb-848f-0ce76f1d35c2/generateAnswer",new BasicNameValuePair("question", ss),true);
                s=map.get("answer").toString();
                final String aaa=s;
                runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(aaa);
                }
            });
               } */}
            }

// Run the action synchronously.
            //say.async().run();

        //执行完毕后给handler发送一个空消息
            handler.sendEmptyMessage(0);
        }


    }

    private void startEngagingHuman(Human human) {
        EngageHuman engageHuman = EngageHumanBuilder.with(qiContext)
                .withHuman(human)
                .build();

        engageHuman.addOnHumanIsEngagedListener(new EngageHuman.OnHumanIsEngagedListener() {
            @Override
            public void onHumanIsEngaged() {
                Log.i("HumanTag", "Human is engaged");
                takeRecogPicture();
                /*
                String textToSay = "欢迎光临，好久不见,拍拍我的头我能帮你识别各种信息";
                final Say say = SayBuilder.with(qiContext)
                        .withText(textToSay)
                        .build();
                        */
                Animation anim = AnimationBuilder.with(qiContext).withResources(R.raw.show_head_a001).build();
                final Animate animate = AnimateBuilder.with(qiContext).withAnimation(anim).build();

                //say.async().run();
                animate.async().run();
            }
        });
        engageHuman.addOnStartedListener(new EngageHuman.OnStartedListener() {
            @Override
            public void onStarted() {
                Log.i("HumanTag", "EngageHuman Started");
            }
        });

        engageHumanFuture = engageHuman.async().run();

        engageHumanFuture.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> voidFuture) throws Throwable {
                if (voidFuture.isSuccess()) {
                    Log.i("HumanTag", "The human is gone");
                    String textToSay2 = "嘤嘤嘤";
                    final Say say2 = SayBuilder.with(qiContext)
                            .withText(textToSay2)
                            .build();
                    say2.async().run();
                }
                else if (voidFuture.isCancelled()) {
                    Log.i("HumanTag", "engageHuman was canceled");
                }
                else if (voidFuture.hasError()) {
                    Log.e("HumanTag", "Could not engage human ", voidFuture.getError() );
                }
            }
        });
    }
    private double getDistance(Frame robotFrame, Human human) {
        // Get the human head frame.
        Frame humanFrame = human.getHeadFrame();
        // Retrieve the translation between the robot and the human.
        Vector3 translation = humanFrame.computeTransform(robotFrame).getTransform().getTranslation();
        // Get the translation coordinates.
        double x = translation.getX();
        double y = translation.getY();
        // Compute and return the distance.
        return Math.sqrt(x*x + y*y);
    }

    private Human getClosestHuman(List<Human> humans) {
        // Get the robot frame.
        final Frame robotFrame = qiContext.getActuation().robotFrame();

        // Compare humans using the distance.
        Comparator<Human> comparator = new Comparator<Human>() {
            @Override
            public int compare(Human human1, Human human2) {
                return Double.compare(getDistance(robotFrame, human1), getDistance(robotFrame, human2));
            }
        };
        // Return the closest human.
        return Collections.min(humans, comparator);
    }
}

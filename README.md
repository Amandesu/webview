# 实现简单的`Jsbridge`
什么是`Jsbridge`? `JsBridge`并不是`Android`或者`H5`直接就有的`API`.它是一种通过上面五种通信方式(遵循某种协议)来实现的一个双向通信桥. 下面来实现一个简单的 `JsBridge`

1. 每次调用原生方法都会生成一个对应`callBack`唯一`id`.客户端回调会告诉返回对应的`id`来执行对应的`callback`
2. `callNative`是`H5`调用**Android原生**的方法,是通过`JavascriptInterface`注入一个`nativeBridge`来实现的.
3. `receiveNative`是**Android原生**调用`H5`的方法,是通过`webView.loadUrl("javascript:window.Bridge.receiveNative("")`来实现.
> 你能调用我，我能调用你，然后通过某个协议(`id`).一个双工通信就实现了 

**`H5`代码如下**
```
<body>
    <div>我是webview</div>
    <div id="cs">测试JavascriptInterface</div>
</body>
<script>
    let cid = 0;
    let callbacks = {};
    window.Bridge = {
        // 获取用户的登录信息
        getUserInfo(data = {}){
            window.Bridge.callNative("getUserInfo", data)
        },
        // 获取网络情况
        getNetInfo(data = {}){
            window.Bridge.callNative("getNetInfo", data)
        },
        callNative: function(bridgeName, data) {
            cid++;
            if (data.onSuccess) {
                callbacks[cid] = data.onSuccess;
            }
            nativeBridge.postMessage(JSON.stringify({
                cid,
                bridgeName: bridgeName,
                data: data.params || {}
            }));
         },
        receiveNative: function(msg) {
            callbacks[msg.cid] && callbacks[msg.cid](msg);
            if (msg.bridgeName != "getNetInfo") {
                Reflect.deleteProperty(callbacks, msg.cid);
            }
        }
    }
    document.getElementById("cs").onclick = function(){
        window.Bridge.getUserInfo({
            params:{},
            onSuccess(res){
                alert(res.name)
            }
        })
    }
</script>
```

**`Android`代码如下**
```
final WebView webView = (WebView)findViewById(R.id.h5_shop);
final Handler handler = new Handler(){
    public void handleMessage(Message msg) {
        if (msg.what == 1) {
            webView.loadUrl("javascript:window.Bridge.receiveNative("+resObj.toString()+")");
        }
    }
};
webView.getSettings().setJavaScriptEnabled(true);
class JsInterface {
    @JavascriptInterface
    public void postMessage(String a) throws JSONException{
        JSONObject jsonObj = new JSONObject(a);
        String bridgeName = jsonObj.getString("bridgeName");
        if ( bridgeName.equals("getUserInfo") ){
            resObj.put("cid", jsonObj.get("cid"));
            resObj.put("name", "leiwuyi");
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        } 

    }
}
webView.addJavascriptInterface(new JsInterface(), "nativeBridge");
webView.loadUrl("file:///android_asset/shop.html");
```

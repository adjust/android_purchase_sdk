## 概要

こちらはadjust™のAndroid Purchase SDKです。adjust™について詳しくは[adjust.com]をご覧ください。

## 目次

* [基本的な連携方法](#basic-integration)
    * [SDKダウンロード](#sdk-get)
    * [SDKモジュールのインポート](#sdk-import)
    * [プロジェクトへのSDKライブラリの追加](#sdk-add)
    * [アプリへのSDKの連携](#sdk-integrate)
    * [Adjust Purchaseログ](#sdk-logging)
* [課金の検証](#verify-purchases)
    * [検証リクエスト](#verification-request)
    * [検証のレスポンス](#verification-response)
    * [検証された課金のトラッキング](#track-purchases)
* [利用例](#best-practices)
* [ライセンス](#license)

## 基本的な連携方法

adjustのPurchase SDKをご利用になるには、アプリに**まず不正防止ツールを有効化してください**。
詳しくはこちらの公式[不正防止ガイド][fraud-prevention]をご覧ください。

Androidプロジェクトへのadjust Purchase SDKの連携方法を説明します。
ここではAndroidの開発にAndroid Studioが使われていること、Android APIレベル9 (Gingerbread)以降をターゲットとしていることを仮定します。

### <a id="sdk-get"></a>SDKダウンロード

[リリースページ][releases]から最新バージョンをダウンロードしてください。ダウンロードしたアーカイブを任意のディレクトリに展開してください。

### <a id="sdk-import"></a>SDKモジュールのインポート

Android Studioメニューから`File → New → Import Module...`と進めてください。

![][import_module]

`Source directory`欄で、[前のステップ](#sdk-get)で展開したフォルダを探してください。
`./android_purchase_sdk/AdjustPurchase/adjust_purchase`フォルダを選択してください。
モジュール名`:adjust_purchase`が表示されていることをご確認ください。

![][select_module]

続いて`adjust_purchase`モジュールをAndroid Studioプロジェクトにインポートする必要があります。

![][imported_module]

### <a id="sdk-add"></a>Add the SDK library to your project

Open the `build.gradle` file of your app and find the `dependencies` block. Add
the following line:

```
compile project(":adjust_purchase")
```

![][gradle_adjust_purchase]

### <a id="sdk-integrate"></a>プロジェクトへのSDKライブラリの追加

`Application`クラス内に`onCreate`メソッドがあればそこに、なければこれを作成し、adjust Purchase SDK初期化の以下のコードを追加してください。

```java
import com.adjust.sdk.purchase.ADJPConfig;
import com.adjust.sdk.purchase.ADJPConstants;
import com.adjust.sdk.purchase.AdjustPurchase;

public class GlobalApplication extends Application {
@Override
public void onCreate() {
super.onCreate();

String yourAppToken = "{YourAppToken}";
String environment = ADJPConstants.ENVIRONMENT_SANDBOX;

ADJPConfig config = new ADJPConfig(yourAppToken, environment);

AdjustPurchase.init(config);
}
}
```

![][application_config]

`{YourAppToken}`にアプリのトークンを記入してください。トークンは[dashboard]で確認できます。

`environment`に以下のどちらかを設定してください。これはテスト用アプリか本番用アプリかによって異なります。

```java
String environment = ADJPConstants.ENVIRONMENT_SANDBOX;
String environment = ADJPConstants.ENVIRONMENT_PRODUCTION;
```

**重要** この値はアプリのテスト中のみ`ADJPConstants.ENVIRONMENT_SANDBOX`に設定してください。
アプリを提出する前に`ADJPConstants.ENVIRONMENT_PRODUCTION`になっていることを必ず確認してください。
再度開発やテストをする際は`ADJPConstants.ENVIRONMENT_SANDBOX`に戻してください。

この変数は実際のトラフィックとテスト端末からのテストのトラフィックを区別するために利用されます。
正しく計測するために、この値の設定には常に注意してください。

### <a id="sdk-logging"></a>Adjust Purchaseログ

`ADJPConfig`インスタンスの`setLogLevel:`に設定するパラメータを変更することによって記録するログのレベルを調節できます。
パラメータは以下の種類があります。

```java
config.setLogLevel(ADJPLogLevel.VERBOSE);   // すべてのログを有効にする
config.setLogLevel(ADJPLogLevel.DEBUG);     // より詳細なログを記録する
config.setLogLevel(ADJPLogLevel.INFO);      // デフォルト
config.setLogLevel(ADJPLogLevel.WARN);      // infoのログを無効にする
config.setLogLevel(ADJPLogLevel.ERROR);     // warningsを無効にする
config.setLogLevel(ADJPLogLevel.ASSERT);    // errorsも無効にする
```

## <a id="verify-purchases"></a>課金の検証

### <a id="verification-request"></a>検証リクエスト

アプリ内課金を検証するには、`AdjustPurchase`インスタンスで`verifyPurchase`メソッドをコールしてください。
課金が正常に行われてからこのメソッドをコールしてください。

以下に一例を示します。これはお使いのIAP APIによって異なります。

```java
public class MainActivity extends Activity implements OnADJPVerificationFinished {
static IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
public void onConsumeFinished(Purchase purchase, IabResult result) {
if (result.isSuccess()) {
// Do your stuff.

AdjustPurchase.verifyPurchase(purchase.getSku(), purchase.getToken(), 
purchase.getDeveloperPayload(), mCurrentActivity);
} else  {
// Do your else stuff.
}
}
};

// ...

public void onVerificationFinished(ADJPVerificationInfo info) {
// ...
}
}
```

検証リクエストに使われるadjust Purchase SDKのメソッドには以下のパラメータを渡す必要があります。

```
itemSku           // ユニークオーダーID (SKU)
itemToken         // 課金を識別するトークン
developerPayload  // オーダーに関する補足情報を含む開発者指定の文字列
callback          // 検証のレスポンスを処理するコールバックメソッド
```

### <a id="verification-response"></a>検証のレスポンス

The response to purchase verification is represented with 
an `ADJPVerificationInfo` object and it contains following information:
上記のコードに示したように、このメソッドの最後のパラメータに`OnADJPVerificationFinished`プロトコルを実装したオブジェクトを渡す必要があります。
`onVerificationFinished`メソッドをオーバーライドしてこれを行ってください。
このメソッドはレスポンス受信後にadjust Purchase SDKによってコールされます。
課金検証へのレスポンスは`ADJPVerificationInfo`オブジェクトで表され、以下の情報を含みます。

```java
info.getVerificationState()     // 課金検証のステータス
info.getStatusCode()            // バックエンドでのレスポンスステータスコードを示す整数値
info.getMessage()               // 課金検証のステータスに関するメッセージ
```

課金検証のステータスは以下のいずれかの値を持ち得ます。

```
ADJPVerificationState.ADJPVerificationStatePassed       - 課金検証が成功
ADJPVerificationState.ADJPVerificationStateFailed       - 課金検証が失敗
ADJPVerificationState.ADJPVerificationStateUnknown      - 課金検証のステータスが不明
ADJPVerificationState.ADJPVerificationStateNotVerified  - 課金が検証されなかった
```

* 課金がAppleのサーバーで正しく検証された場合、`ADJPVerificationStatePassed`はステータスコード`200`を返します。
* Appleのサーバーが課金を無効として識別した場合、`ADJPVerificationStateFailed`はステータスコード`406`を返します。
* Appleのサーバーが課金の検証リクエストに対して返答を返さなかった場合、`ADJPVerificationStateUnknown`はステータスコード`204`を返します。
これは課金の正当性についてadjustはAppleから何の情報も得ていないことを示します。課金の状態には関係ありません。有効、無効のどちらも有り得ます。
何らかの状況で正しいステータスの送信を阻まれた場合にもこれは起こりえます。これらのエラーに関する詳細は`ADJPVerificationInfo`オブジェクトの
`getMessage()`欄でご確認いただけます。
* `ADJPVerificationStateNotVerified`が受信されたら、それは`verifyPurchase`メソッドへのコールが不正なパラメータで行われたことを意味します。

### <a id="track-purchases"></a>検証された課金のトラッキング

課金が正しく検証された後、公式adjust SDKを使ってそれをトラッキングしダッシュボード上で収益として反映させることができます。

上記の例を用いて以下に例を挙げます。

```java
@Override
public void onVerificationFinished(ADJPVerificationInfo info) {
if (info.getVerificationState() == ADJPVerificationState.ADJPVerificationStatePassed) {
AdjustEvent event = new AdjustEvent("{YourEventToken}");
event.setRevenue(0.01, "EUR");

Adjust.trackEvent(event);
}
}
```

## <a id="best-practices"></a>利用例

`ADJPVerificationStatePassed`もしくは`ADJPVerificationStateFailed`を受け取れば、この決定はAppleのサーバーによって行われ、
課金の収益としてトラッキングすべきか信用できるものであるという事が保証されます。
`ADJPVerificationStateUnknown`が受信された場合、この課金に対しての挙動を決めることができます。

統計的な目的の場合、adjustダッシュボード上でそれぞれの状況に対して明確なイベントをひとつ用意することが有効かもしれません。
この方法だと、課金のうちいくつが有効、無効、または検証不可能で不明ステータスとして帰ってきたかが分かります。
ご希望であれば検証されなかった課金に対してもトラッキングすることができます。

これを行う場合、レスポンスを処理するメソッドは一例として以下のようになります。

```java
@Override
public void onVerificationFinished(ADJPVerificationInfo info) {
if (info.getVerificationState() == ADJPVerificationState.ADJPVerificationStatePassed) {
AdjustEvent event = new AdjustEvent("{RevenueEventPassedToken}");
event.setRevenue(0.01, "EUR");

Adjust.trackEvent(event);
} else if (info.getVerificationState() == ADJPVerificationState.ADJPVerificationStateFailed) {
AdjustEvent event = new AdjustEvent("{RevenueEventFailedToken}");
Adjust.trackEvent(event);
} else if (info.getVerificationState() == ADJPVerificationState.ADJPVerificationStateUnknown) {
AdjustEvent event = new AdjustEvent("{RevenueEventUnknownToken}");
Adjust.trackEvent(event);
} else {
AdjustEvent event = new AdjustEvent("{RevenueEventNotVerifiedToken}");
Adjust.trackEvent(event);
}
}
```

課金検証は販売されたアイテムの納入に対して承認または拒否することが目的ではありません。課金検証はレポートされるトランザクションデータを
実際のトランザクションデータと合わせることが目的です。

[dashboard]:                http://adjust.com
[adjust.com]:               http://adjust.com

[maven]:                    http://maven.org
[releases]:                 https://github.com/adjust/android_purchase_sdk/releases
[fraud-prevention]:         https://docs.adjust.com/en/fraud-prevention/

[import_module]:            https://raw.github.com/adjust/sdks/master/Resources/android_purchase/import_module.png
[select_module]:            https://raw.github.com/adjust/sdks/master/Resources/android_purchase/select_module.png
[imported_module]:          https://raw.github.com/adjust/sdks/master/Resources/android_purchase/imported_module.png
[application_config]:       https://raw.github.com/adjust/sdks/master/Resources/android_purchase/application_config.png
[gradle_adjust_purchase]:   https://raw.github.com/adjust/sdks/master/Resources/android_purchase/gradle_adjust_purchase.png

## <a id="license"></a>ライセンス

adjust purchase SDKはMITライセンスを適用しています。

Copyright (c) 2016 adjust GmbH,
http://www.adjust.com

以下に定める条件に従い、本ソフトウェアおよび関連文書のファイル（以下「ソフトウェア」）の複製を取得するすべての人に対し、
ソフトウェアを無制限に扱うことを無償で許可します。これには、ソフトウェアの複製を使用、複写、変更、結合、掲載、頒布、サブライセンス、
および/または販売する権利、およびソフトウェアを提供する相手に同じことを許可する権利も無制限に含まれます。

上記の著作権表示および本許諾表示を、ソフトウェアのすべての複製または重要な部分に記載するものとします。

ソフトウェアは「現状のまま」で、明示であるか暗黙であるかを問わず、何らの保証もなく提供されます。
ここでいう保証とは、商品性、特定の目的への適合性、および権利非侵害についての保証も含みますが、それに限定されるものではありません。 
作者または著作権者は、契約行為、不法行為、またはそれ以外であろうと、ソフトウェアに起因または関連し、
あるいはソフトウェアの使用またはその他の扱いによって生じる一切の請求、損害、その他の義務について何らの責任も負わないものとします。

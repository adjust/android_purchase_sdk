## 摘要

这是 adjust™ 的安卓收入验证SDK。您可以访问[adjust.com]了解更多有关 adjust™ 的信息。


## 目录

* [基本集成](#basic-integration)
   * [获取SDK](#sdk-get)
   * [导入SDK模块](#sdk-import)
   * [添加SDK库至您的项目](#sdk-add)
   * [集成SDK至您的应用](#sdk-integrate)
   * [Adjust购买日志](#sdk-logging)
* [收入验证](#verify-purchases)
   * [发出验证请求](#verification-request)
   * [处理验证响应](#verification-response)
   * [跟踪已验证收入](#track-purchases)
* [最佳实践](#best-practices)
* [许可协议](#license)

## 基本集成

您必须 **首先为您的应用启用防作弊** ，以使用adjust收入验证SDK。您可以在我们的官方[防作弊指南][fraud-prevention]文档中找到相关说明。

以下是将adjust收入验证SDK集成至安卓项目的基本步骤。我们假定您将Android Studio用于安卓开发，并使用安卓API level 9 (Gingerbread)及以上版本。

### <a id="sdk-get"></a>获取SDK

请从我们的[发布专页][releases]中下载最新版本，并将文档解压至您选择的文件夹中。

### <a id="sdk-import"></a>导入SDK模块

请在Android Studio菜单中选择 `File → New → Import Module...` 。

![][import_module]

在 `Source directory` （源目录）中找到您在[上步骤](#sdk-get)中解压的文件夹。选择文件夹 `./android_purchase_sdk/AdjustPurchase/adjust_purchase` 。请在完成前确保模块名称显示为 `:adjust_purchase` 。

![][select_module]

之后 `adjust_purchase` 模块应已导入至您的Android Studio项目中。

![][imported_module]

### <a id="sdk-add"></a>添加SDK库至您的项目

打开您应用中的 `build.gradle` 文件并找到 `dependencies` 程序块。请添加如下代码行：

```
compile project(":adjust_purchase")
```

![][gradle_adjust_purchase]

### <a id="sdk-integrate"></a>集成SDK至您的应用

在您的 `Application` 类中找到或者创建 `onCreate` 方法，然后添加如下代码以初始化adjust收入验证SDK:

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

使用您的应用识别码替换 `{YourAppToken}` 。您可以在[控制面板]找到该识别码。

鉴于您的应用是用于测试还是产品开发，您必须将 `environment` （环境模式）设为以下值之一：

```java
String environment = ADJPConstants.ENVIRONMENT_SANDBOX;
String environment = ADJPConstants.ENVIRONMENT_PRODUCTION;
```

**重要:** 仅当您或其他人测试您的应用时，该值应设为 `ADJPConstants.ENVIRONMENT_SANDBOX` 。在您发布应用之前，请确保将环境改设为 `ADJPConstants.ENVIRONMENT_PRODUCTION` 。再次研发和测试时，请将其设回为 `ADJPConstants.ENVIRONMENT_SANDBOX` 。

我们按照设置的环境来区分真实流量和来自测试设备的测试流量，所以正确使用环境参数是非常重要的！

### <a id="sdk-logging"></a>Adjust 购买日志

您可以增加或减少在测试中看到的日志数量，方法是用以下参数之一来调用 `ADJPConfig` 实例上的 `setLogLevel` ：

```java
config.setLogLevel(ADJPLogLevel.VERBOSE);   // Enable all logging.
config.setLogLevel(ADJPLogLevel.DEBUG);     // Enable more logging.
config.setLogLevel(ADJPLogLevel.INFO);      // The default.
config.setLogLevel(ADJPLogLevel.WARN);      // Disable info logging.
config.setLogLevel(ADJPLogLevel.ERROR);     // Disable warnings as well.
config.setLogLevel(ADJPLogLevel.ASSERT);    // Disable errors as well.
```

## <a id="verify-purchases"></a>收入验证

### <a id="verification-request"></a>发出验证请求

为了验证您的应用内购买，您需要调用 `AdjustPurchase` 实例上的 `verifyPurchase` 方法。请确保在购买成功后调用此方法。

以下为操作示例 （取决于您正在使用的IAP API）：

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
您需要传递以下参数，以调用adjust收入验证SDK的方法来发出验证请求：

```
itemSku           // Unique order ID (SKU)
itemToken         // A token that uniquely identifies a purchase
developerPayload  // A developer-specified string that contains supplemental information about an order
callback          // Callback method which will process the verification response
```

### <a id="verification-response"></a>处理验证响应

如以上代码所述，在该方法的最后一个参数中，您应该传递一个对象来实现 `OnADJPVerificationFinished` 协议。因此，您需要改写名为 `onVerificationFinished` 的方法。该方法将在响应到达时被我们的收入验证SDK调用。收入验证响应表示为 `ADJPVerificationInfo` 对象，并包含以下信息：

```java
info.getVerificationState()     // State of purchase verification.
info.getStatusCode()            // Integer which displays backend response status code.
info.getMessage()               // Message describing purchase verification state.
```

验证状态将为以下值之一：

```
ADJPVerificationState.ADJPVerificationStatePassed       - Purchase verification successful.
ADJPVerificationState.ADJPVerificationStateFailed       - Purchase verification failed.
ADJPVerificationState.ADJPVerificationStateUnknown      - Purchase verification state unknown.
ADJPVerificationState.ADJPVerificationStateNotVerified  - Purchase was not verified.
```

* 如果Google服务器成功验证购买，将会报告 `ADJPVerificationStatePassed` 连带状态码 `200` 。
* 如果Google服务器将购买视为无效，则会报告 `ADJPVerificationStateFailed` 连带状态码 `406` 。
* 如果Google服务器并未对我们的收入验证请求给予回复，将会报告 `ADJPVerificationStateUnknown` 连带状态码 `204` 。此种情况意味着我们无法从Google服务器中获取有关收入有效性的任何信息。这和购买本身并无联系，购买可能是有效或者无效的。报告此种状态的原因也有可能是发生了阻止我们报告收入验证正确状态的情况。您可以通过调用 `ADJPVerificationInfo` 对象上的 `getMessage()` 方法获取关于此故障的更多详细信息。
* 如果报告了 `ADJPVerificationStateNotVerified` ， 则表示使用了无效参数调用 `verifyPurchase` 方法。

### <a id="track-purchases"></a>跟踪已验证收入

成功验证收入后，您可以使用我们的adjust SDK来跟踪收入并在您的控制面板中查看收入。

使用以上示例，您可以如下设置跟踪已验证收入：

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

## <a id="best-practices"></a>最佳实践

一旦报告 `ADJPVerificationStatePassed` 或者 `ADJPVerificationStateFailed` ，您可以信赖来自Google服务器的反馈并由其决定是否跟踪您的购买收入。如果报告的是 `ADJPVerificationStateUnknown` ，您需要决定对此购买采取的下一步动作。

为了统计的需要，我们建议您在adjust控制面板中对每一个场景设置一个定义事件。这样便于您更好地了解您的收入中有多少被标记为已通过验证，多少被标记为未通过验证，以及多少是无法被验证并返回未知状态。如果需要的话，您还可以跟踪未经验证的收入。

您可以通过以下方法来处理响应：

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

收入验证功能不是用来批准/拒绝已售货物的交付，而是用来比较报告交易数据和实际交易数据的一致性。

[dashboard]:                http://adjust.com
[adjust.com]:               http://adjust.com

[maven]:                    http://maven.org
[releases]:                 https://github.com/adjust/android_purchase_sdk/releases
[fraud-prevention]:         https://docs.adjust.com/zh/fraud-prevention/

[import_module]:            https://raw.github.com/adjust/sdks/master/Resources/android_purchase/import_module.png
[select_module]:            https://raw.github.com/adjust/sdks/master/Resources/android_purchase/select_module.png
[imported_module]:          https://raw.github.com/adjust/sdks/master/Resources/android_purchase/imported_module.png
[application_config]:       https://raw.github.com/adjust/sdks/master/Resources/android_purchase/application_config.png
[gradle_adjust_purchase]:   https://raw.github.com/adjust/sdks/master/Resources/android_purchase/gradle_adjust_purchase.png

## <a id="license"></a>许可协议

The adjust purchase SDK is licensed under the MIT License.

Copyright (c) 2016 adjust GmbH,
http://www.adjust.com

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

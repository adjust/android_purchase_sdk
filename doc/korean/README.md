## 요약

이 문서는 adjust™의 Android 구매 SDK를 설명하는 요약 정보입니다. adjust™에 대한 자세한 내용은 [adjust.com]에서 확인할 수 있습니다.

## 차례

* [기본 연동](#basic-integration)
   * [SDK 다운로드](#sdk-get)
   * [SDK 모듈 가져오기](#sdk-import)
   * [프로젝트에 SDK 라이브러리 추가](#sdk-add)
   * [SDK를 앱에 연동](#sdk-integrate)
   * [결제 로그 조절](#sdk-logging)
* [결제 검증](#verify-purchases)
   * [검증 요청](#verification-request)
   * [검증 응답 처리](#verification-response)
   * [검증된 결제 추적](#track-purchases)
* [모범 사례](#best-practices)
* [라이선스](#license)

## 기본 통합

adjust 구매 SDK를 사용하려면 앱에 대해 **먼저 사기 예방 기능을 활성화**해야 합니다. 자세한 방법은 저희의 공식 문서인 [사기 예방 가이드][fraud-prevention]에서 확인할 수 있습니다.

여기에서는 adjust 구매 SDK를 Android 프로젝트에 연동하기 위한 기본 절차만 설명합니다. 모든 설명은 Android Studio를 사용하여 Android 앱을 개발하고 있고 Android API 레벨 9(진저브레드) 이상의 기기를 대상으로 한다는 가정을 바탕으로 합니다.

### <a id="sdk-get"></a>SDK 다운로드

저희의 [릴리스 페이지][releases]에서 최신 버전을 다운로드하세요. 원하는 폴더에 압축 파일을 푸세요.

### <a id="sdk-import"></a>SDK 모듈 가져오기

Android Studio 메뉴에서 `File → New → Import Module...`을 선택하세요.

![][import_module]

`Source directory` 필드에서 [이전 단계](#sdk-get)에서 압축 파일을 푼 폴더를 찾으세요. `./android_purchase_sdk/AdjustPurchase/adjust_purchase` 폴더를 선택하세요. 모듈 이름 `:adjust_purchase`가 나타나는지 확인한 후 완료하세요.

![][select_module]

그러면 `adjust_purchase` 모듈을 Android Studio 프로젝트로 가져옵니다.

![][imported_module]

### <a id="sdk-add"></a>프로젝트에 SDK 라이브러리 추가

앱의 `build.gradle` 파일을 열고 `dependencies` 블록을 찾으세요. 다음 줄을 추가하세요.

```
compile project(":adjust_purchase")
```

![][gradle_adjust_purchase]

### <a id="sdk-integrate"></a>SDK를 앱에 연동

`Application` 클래스에서 `onCreate` 메서드를 찾거나 만든 후 다음 코드를 추가하여 adjust 구매 SDK를 초기화하세요.

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

`{YourAppToken}`을 앱 토큰으로 교체하세요. 앱 토큰은 [대시보드]에서 찾을 수 있습니다.

앱을 테스트용으로 제작하는지 출시 목적으로 제작하는지에 따라 다음 값 중 하나로 `environment`를 설정해야 합니다.
    
```java
String environment = ADJPConstants.ENVIRONMENT_SANDBOX;
String environment = ADJPConstants.ENVIRONMENT_PRODUCTION;
```
    
**중요:** 앱을 테스트할 때에만 이 값을 `ADJPConstants.ENVIRONMENT_SANDBOX`로 설정해야 합니다. 앱을 출시할 때에는 반드시 이 값을 `ADJPConstants.ENVIRONMENT_PRODUCTION`으로 설정해야 합니다. 앱을 개발 중이거나 테스트할 때에는 이 값을 다시 `ADJPConstants.ENVIRONMENT_SANDBOX`로 설정하세요.

저희는 이 환경 값을 사용하여 테스트 기기로부터 전달되는 실제 트래픽과 테스트 트래픽을 구분합니다. 항상 이 값을 목적에 맞게 설정하는 것이 중요합니다!

### <a id="sdk-logging"></a>결제 로그 조절

`ADJPConfig` 인스턴스에서 다음 매개변수 중 하나와 함께 `setLogLevel`을 호출하여 테스트에서 표시되는 로그의 양을 늘리거나 줄일 수 있습니다.

```java
config.setLogLevel(ADJPLogLevel.VERBOSE);   // 모든 로그를 활성화합니다.
config.setLogLevel(ADJPLogLevel.DEBUG);     // 더 많은 로그를 활성화합니다.
config.setLogLevel(ADJPLogLevel.INFO);      // 기본값입니다.
config.setLogLevel(ADJPLogLevel.WARN);      // 정보 로그를 비활성화합니다.
config.setLogLevel(ADJPLogLevel.ERROR);     // 경고도 비활성화합니다.
config.setLogLevel(ADJPLogLevel.ASSERT);    // 오류도 비활성화합니다.
```

## <a id="verify-purchases"></a>결제 검증

### <a id="verification-request"></a>검증 요청

앱에서 이루어진 결제를 검증하려면 `AdjustPurchase` 인스턴스에서 `verifyPurchase` 메서드를 호출해야 합니다. 결제가 성공적으로 완료된 후 이 메서드가 호출되도록 하세요.

다음은 이 작업을 수행하기 위한 한 가지 예시로, 사용 중인 IAP API에 따라 다를 수 있습니다.

```java
public class MainActivity extends Activity implements OnADJPVerificationFinished {
    static IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                // 작업을 수행합니다.

                AdjustPurchase.verifyPurchase(purchase.getSku(), purchase.getToken(), 
                    purchase.getDeveloperPayload(), mCurrentActivity);
            } else  {
                // 다른 작업을 수행합니다.
            }
        }
    };
    
    // ...
    
    public void onVerificationFinished(ADJPVerificationInfo info) {
        // ...
    }
}
```

검증 요청에 사용되는 adjust 구매 SDK의 메서드에서는 다음 매개변수를 전달해야 합니다.

```
itemSku           // 고유 주문 ID(SKU)
itemToken         // 결제를 고유하게 식별하는 토큰
developerPayload  // 개발자가 지정한 문자열로 주문에 대한 부가 정보를 담고 있음
callback          // 검증 응답을 처리하는 콜백 메서드
```

### <a id="verification-response"></a>검증 응답 처리

위 코드에서 설명한 대로 이 메서드의 마지막 매개변수에서 `OnADJPVerificationFinished` 프로토콜을 구현하는 개체를 전달해야 합니다. 이 작업을 수행하려면 `onVerificationFinished` 메서드를 재정의해야 합니다. 이 메서드는 응답이 도착하면 저희의 구매 SDK에서 호출됩니다. 결제 검증에 대한 응답은 `ADJPVerificationInfo` 개체로 표시되고 다음 정보를 포함합니다.

```java
info.getVerificationState()     // 결제 검증 상태입니다.
info.getStatusCode()            // 백엔드 응답 상태 코드를 표시하는 정수입니다.
info.getMessage()               // 결제 검증 상태를 설명하는 메시지입니다.
```

검증 상태는 다음 값 중 하나를 가질 수 있습니다.

```
ADJPVerificationState.ADJPVerificationStatePassed       - Purchase verification successful.
ADJPVerificationState.ADJPVerificationStateFailed       - Purchase verification failed.
ADJPVerificationState.ADJPVerificationStateUnknown      - Purchase verification state unknown.
ADJPVerificationState.ADJPVerificationStateNotVerified  - Purchase was not verified.
```

* Google 서버에서 결제가 성공적으로 검증되면 `ADJPVerificationStatePassed`가 `200` 상태 코드와 함께 보고됩니다.
* Google 서버에서 결제가 유효하지 않다고 인식하면 `ADJPVerificationStateFailed`가 `406` 상태 코드와 함께 보고됩니다.
* Google 서버가 결제 검증 요청에 대해 아무런 응답도 하지 않으면 `ADJPVerificationStateUnknown`이 `204` 상태 코드와 함께 보고됩니다. 이 상황은 저희가 Google 서버로부터 결제의 유효성에 관해 아무런 정보도 받지 못했음을 의미합니다. 이 상태는 결제 자체에 대해 아무런 정보를 제공하지 않으며, 결제가 유효할 수도 있고 유효하지 않을 수도 있습니다. 또한 기타 다른 상황으로 인해 저희가 결제 검증에 대한 정확한 상태를 보고받지 못했을 때에도 이 상태가 보고됩니다. 이러한 오류에 대한 자세한 내용은 `ADJPVerificationInfo` 개체에서 `getMessage()` 메서드를 호출하여 확인할 수 있습니다.
* `ADJPVerificationStateNotVerified`가 보고되면, `verifyPurchase` 메서드에 대한 콜에 유효하지 않은 매개변수가 사용되었음을 의미합니다.

### <a id="track-purchases"></a>검증된 결제 추적

결제가 성공적으로 검증되면 저희의 공식 adjust SDK를 사용하여 결제를 추적하고 대시보드에서 수익을 파악할 수 있습니다.

위의 예를 사용하여 다음과 같이 이 작업을 수행할 수 있습니다.

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

## <a id="best-practices"></a>모범 사례

`ADJPVerificationStatePassed` 또는 `ADJPVerificationStateFailed`가 보고되면 이러한 결정이 Google 서버에서 이루어졌으므로 안심하고 구매 수익을 추적하거나 추적하지 않을 수 있습니다. `ADJPVerificationStateUnknown`이 보고되면 이러한 구매에 대해 수행할 작업을 결정할 수 있습니다.

통계 목적으로 adjust 대시보드에서 이러한 각 시나리오에 대해 단일 이벤트를 지정하는 것이 좋습니다. 이렇게 하면 얼마나 많은 수의 결제가 검증되었는지, 검증에 실패했는지, 검증할 수 없어 검증 상태를 알 수 없는지 한눈에 살펴볼 수 있습니다. 원하는 경우 검증되지 않은 결제도 추적할 수 있습니다.

이렇게 하려면 응답 처리를 위한 메서드를 다음과 같이 설정할 수 있습니다.

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

결제 검증은 판매된 상품의 배달을 승인하거나 거절하는 용도로는 사용되지 않습니다. 결제 검증은 보고된 트랜잭션 데이터와 실제 트랜잭션 데이터가 일치하는지 확인하는 작업입니다.

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

## <a id="license"></a>라이선스

adjust 구매 SDK는 MIT 라이선스에 의거하여 사용이 허가됩니다.

Copyright (c) 2016 adjust GmbH,
http://www.adjust.com

이 소프트웨어와 부속 문서 파일(이하 ‘소프트웨어’)을 소유하고 있는 사람은 무료로 소프트웨어의 복사본을 사용, 복사, 수정, 병합, 발행, 배포, 2차 라이선스 허가, 판매할 수 있는 권한을 부여받았으며, 소프트웨어를 제공받는 사람에게도 그러한 권한을 부여할 수 있는 권한이 있습니다. 단, 반드시 아래의 조건을 충족해야 합니다.

소프트웨어의 모든 복사본이나 소프트웨어의 중요 부분에 상기 저작권 고지와 이 권한 고지를 포함해야 합니다.

소프트웨어는 상품성, 특정 목적에의 적합성, 저작권 비침해에 대한 보증을 비롯하여 어떠한 종류의 묵시적 또는 명시적 보증 없이 '있는 그대로' 제공됩니다. 소프트웨어 제작자 또는 저작권자는 어떠한 경우에도 계약 이행, 계약 위반, 기타 상황에서 소프트웨어, 소프트웨어 사용, 소프트웨어에서의 거래와 관련하여 발생하는 배상 청구, 손해, 또는 기타 책임 문제에 대해 책임을 지지 않습니다.

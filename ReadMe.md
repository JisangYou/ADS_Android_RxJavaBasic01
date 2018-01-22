# ADS04 Android

## 수업 내용

- RxJava 및 Observable, lambda 기본 학습

## Code Review

### MainActivity

```Java
public class MainActivity extends AppCompatActivity {
    RecyclerView recycler;
    CustomAdapter adapter;

    // 데이터 저장 변수
    List<String> months = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recycler = findViewById(R.id.recycler);
        adapter = new CustomAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // 데이터 - 인터넷에서 순차적으로 가져오는것
        String data[] = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        // 1. 발행자 생성 operator from
        Observable<String> observableFrom = Observable.fromArray(data);


        // 2. subscirbe 에제코드
//        observableFrom.subscribe(new Consumer<String>() {    // onNext 데이터가 있으면 호출된다
//            @Override
//            public void accept(String s) throws Exception {
//                months.add(s);
//            }
//        }, new Consumer<Throwable>() {    // onError 가 호출된다.
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//
//            }
//        }, new Action() { // onComplete 이 호출된다.
//            @Override
//            public void run() throws Exception {
//                adapter.setDataAndRefresh(months);
//            }
//        });


        /**
         * --> subscribe를 타고 올라가보면 해당 메소드가 정의되어 있다.
         * 해당 메소드처럼 차례대로 onNext, onError, onComplete를 인자로 받는다.
         *
         *  @CheckReturnValue
         * @SchedulerSupport(SchedulerSupport.NONE)
         * public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
         * Action onComplete)
         * {return subscribe(onNext, onError, onComplete, Functions.emptyConsumer());}
         *
         */

        //  구독자
        // 1. from
        // 배열 또는 Iterable의 요소를 순서대로 이벤트로 발생시키는 Observable
        observableFrom.subscribe(
                str -> months.add(str), //onNext
                throwble -> {}, // onError
                () -> adapter.setDataAndRefresh(months)); // onComplete



        // 2. just
        // 누군가가 구독하게 되면, 해당 이벤트를 1번 발생시키는 것.
        Observable<String> observableJust = Observable.just("JAN", "FEB", "MAR");
        observableJust.subscribe(str -> months.add(str));

        //3. defer
        // subscribe하는 순간 특정 function을 실행하고 리턴받는 Observable의 이벤트를 전달.
        Observable<String> observableDefer = Observable.defer(new Callable<ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> call() throws Exception {
                return Observable.just("JAN", "FEB", "MAR");
            }
        });
        observableDefer.subscribe(str -> months.add(str));
    }
}
```
- Adapter 세팅하는 코드는 생략


## 보충설명

- RxJava를 사용하려면 Observer패턴에 대한 이해가 필요

### Observer 패턴
>> 옵서버 패턴(observer pattern)은 객체의 상태 변화를 관찰하는 관찰자들, 즉 옵저버들의 목록을 객체에 등록하여 상태 변화가 있을 때마다 메서드 등을 통해 객체가 직접 목록의 각 옵저버에게 통지하도록 하는 디자인 패턴이다. 주로 분산 이벤트 핸들링 시스템을 구현하는 데 사용된다. 발행/구독 모델로 알려져 있기도 하다

![옵저버패턴](http://upload.wikimedia.org/wikipedia/commons/8/8d/Observer.svg)

- Subject 는 이벤트를 발생시키는 주체 
- RxJava 에서는 Observable(또는 Subject)라는 이름으로 표현 
- Subject 에서 발생되는 이벤트들은 그 Subject 에 관심있다고 등록한 Observer 들에게 전달한다.
- Observer 는 RxJava 에서는 Subscriber 라는 이름으로 표현이 된다.

- 때에 따라서 Observable 도 Subscriber 의 역할을 할 수 있다. 
- 일단 다음과 같이 이해

>> Observable : 이벤트를 발생시킨다.
>> Subscriber : 발생된 이벤트를 받아 처리한다.

### Observable과 Subscriber

>> 데이터의 강을 만드는 Observable, 강에서 데이터를 하나씩 건지는 Subscriber

- Observable은 데이터를 제공하는 생산자로 세가지 유형의 행동을 함

1. onNext - 새로운 데이터를 전달
2. onCompleted - 스트림의 종료
3. onError - 에러 신호를 전달

- Subscriber입장에서는 Observable로 부터 onNext,onCompleted,onError를 전파받음.

- 예제코드 

```java
Observable<String> simpleObservable =
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("Hello RxAndroid !!");
                        subscriber.onCompleted();
                    }
                });

        simpleObservable
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "complete!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String text) {
                        ((TextView) findViewById(R.id.textView)).setText(text);
                    }
                });
```

### Observable를 만드는 다양한 방법

- create : Observable 에 구독하게되면, 어떤 형태로 이벤트가 발생되고 전파되는지를 정의하는 인터페이스
- from : 배열 또는 Iterable의 요소를 순서대로 이벤트로 발생시키는 Observalbe
- just : 누군가가 구독하게 되면, 해당 이벤트를 1번 발생시키는 것
- defer : 구독하는 순간 특정 function을 실행하고 리턴받는 Observable의 이벤트 전달
- interval : 주기적으로 이벤트 전파
- timer : 특정 시간만큼 지나서 이벤트 발생
- range : 시작점과 반복횟수를 지정하면 n, n+1, n+2와 같이 반복하여 이벤트를 발생시킴.

### 출처

- 츌처 : http://gaemi.github.io/android/2015/05/20/RxJava-with-Android-1-RxJava-%EC%82%AC%EC%9A%A9%ED%95%B4%EB%B3%B4%EA%B8%B0.html
- 출처 : https://academy.realm.io/kr/posts/rxandroid/

## TODO

- Rx를 사용할떄와 사용하지 않을 때 어떤점이 좋고, 불편한지 연구해보기
- lambda 적응이 필요
- observerPattern 공부

## Retrospect

- 언제, 왜 사용하는지를 먼저 파악해서 공부해야 할 것 같다. 
- 현재 조사하고 공부했던 내용들이 아직까지는 조각조각 떨어진 느낌이 들기에, 공부를 조금 더 한다음 추후에 readme업데이트를 해야 할 것 같다.

## Output
- 생략
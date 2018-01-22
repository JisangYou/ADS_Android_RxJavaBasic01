package com.project.rxbasic01;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


import io.reactivex.Observable;
import io.reactivex.ObservableSource;

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

class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.Holder> {
    List<String> data = new ArrayList<>();

    public void setDataAndRefresh(List<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.text1.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView text1;

        public Holder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
        }
    }
}
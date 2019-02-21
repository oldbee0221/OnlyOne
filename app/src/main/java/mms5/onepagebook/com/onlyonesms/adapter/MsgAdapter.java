package mms5.onepagebook.com.onlyonesms.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.CardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;

public abstract class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Msg> mItems;
    public boolean mIsLoading = false;

    public MsgAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_msg, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Msg item = mItems.get(position);

        holder.mTvDate.setText(getDate(item.lastUpdateTime));
        holder.mTvMessage1.setText(item.message1);
        holder.mTvMessage2.setText(item.message2);

        holder.mLayoutAdoption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAdoption(item);
            }
        });

        holder.mLayoutDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDel(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public int add(List<Msg> arrayList) {
        mItems.clear();

        if(arrayList == null) return 0;

        int size = arrayList.size();
        for(int i=0; i<size; i++) {
            mItems.add(arrayList.get(i));
        }

        notifyItemInserted(mItems.size());

        return size;
    }

    public void removeAll(){
        mItems.clear();
    }

    public void remove(Msg item) {
        int size = mItems.size();
        for(int i=0; i<size; i++) {
            if(mItems.get(i).lastUpdateTime == item.lastUpdateTime) {
                mItems.remove(i);
                break;
            }
        }

        notifyDataSetChanged();
    }

    private String getDate(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        StringBuffer sb = new StringBuffer();
        sb.append(cal.get(Calendar.YEAR)).append("-");

        int month = cal.get(Calendar.MONTH) + 1;
        if(month > 10) sb.append(month).append("-");
        else sb.append("0").append(month).append("-");

        int day = cal.get(Calendar.DAY_OF_MONTH);
        if(day > 10) sb.append(day).append(" ");
        else sb.append("0").append(day).append(" ");

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour > 10) sb.append(hour).append(":");
        else sb.append("0").append(hour).append(":");

        int min = cal.get(Calendar.MINUTE);
        if(min > 10) sb.append(min).append(":");
        else sb.append("0").append(min).append(":");

        int mil = cal.get(Calendar.MILLISECOND);
        if(mil > 10) sb.append(mil);
        else sb.append("0").append(mil);

        return sb.toString();
    }

    public abstract void load();
    public abstract void onDel(Msg item);
    public abstract void onAdoption(Msg use);

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTvMessage1;
        public TextView mTvMessage2;
        public TextView mTvDate;
        public LinearLayout mLayoutAdoption;
        public LinearLayout mLayoutDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = itemView.findViewById(R.id.cv_base);
            mTvMessage1 = itemView.findViewById(R.id.tv_message1);
            mTvMessage2 = itemView.findViewById(R.id.tv_message2);
            mTvDate = itemView.findViewById(R.id.tv_date);
            mLayoutAdoption = itemView.findViewById(R.id.ll_adoption);
            mLayoutDelete = itemView.findViewById(R.id.ll_delete);
        }
    }
}


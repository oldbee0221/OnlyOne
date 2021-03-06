package mms5.onepagebook.com.onlyonesms.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.base.GlideApp;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2019-07-12.
 */
public abstract class CallMsgAdapter extends RecyclerView.Adapter<CallMsgAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<CallMsg> mItems;
    private boolean mIsFromMsg;
    private long mRegDate;
    private int mSeletedPos = -1;

    public CallMsgAdapter(Context context, boolean mode, long regdate) {
        mContext = context;
        mItems = new ArrayList<>();
        mIsFromMsg = mode;
        mRegDate = regdate;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_callmsg, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final CallMsg item = mItems.get(position);

        if (mIsFromMsg) {
            holder.mLayoutSend.setVisibility(View.VISIBLE);
            holder.mLayoutDelete.setVisibility(View.GONE);
        } else {
            holder.mLayoutSend.setVisibility(View.GONE);
            holder.mLayoutDelete.setVisibility(View.VISIBLE);
        }

        holder.mTvDate.setText(getDate(item.regdate));
        holder.mTvCategory.setText(item.category);
        holder.mTvTitle.setText(item.title);

        if(item.contents.length() > 30) {
            if(mSeletedPos == position) {
                holder.mTvMessage.setText(item.contents);
            } else {
                holder.mTvMessage.setText(item.contents.substring(0, 30) + "...");
            }
        } else {
            holder.mTvMessage.setText(item.contents);
        }

        if(!TextUtils.isEmpty((item.imgpath))) {
            GlideApp.with(mContext)
                    .load(new File(item.imgpath))
                    .into(holder.mIvPhoto);
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onContents(position);
            }
        });

        holder.mLayoutUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpdate(item);
            }
        });

        holder.mLayoutDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDel(item);
            }
        });

        holder.mLayoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSend(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public int add(List<CallMsg> arrayList) {
        mItems.clear();

        if (arrayList == null) return 0;

        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            mItems.add(arrayList.get(i));
        }

        notifyItemInserted(mItems.size());

        return size;
    }

    public void removeAll() {
        mItems.clear();
    }

    public void remove(CallMsg item) {
        int size = mItems.size();
        for (int i = 0; i < size; i++) {
            if (mItems.get(i).regdate == item.regdate) {
                mItems.remove(i);
                break;
            }
        }

        if(item.regdate == mRegDate) {
            Utils.PutSharedPreference(mContext, Constants.PREF_CB_AUTO_MSG, 0);
        }

        notifyDataSetChanged();
    }

    public void selected(int pos) {
        if(mSeletedPos == -1) {
            mSeletedPos = pos;
        } else {
            mSeletedPos = -1;
        }

        notifyDataSetChanged();
    }

    private String getDate(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        StringBuffer sb = new StringBuffer();
        sb.append(cal.get(Calendar.YEAR)).append("-");

        int month = cal.get(Calendar.MONTH) + 1;
        if (month >= 10) sb.append(month).append("-");
        else sb.append("0").append(month).append("-");

        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (day >= 10) sb.append(day).append(" ");
        else sb.append("0").append(day).append(" ");

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour >= 10) sb.append(hour).append(":");
        else sb.append("0").append(hour).append(":");

        int min = cal.get(Calendar.MINUTE);
        if (min >= 10) sb.append(min).append(":");
        else sb.append("0").append(min).append(":");

        int mil = cal.get(Calendar.SECOND);
        if (mil >= 10) sb.append(mil);
        else sb.append("0").append(mil);

        return sb.toString();
    }

    public abstract void load();
    public abstract void onDel(CallMsg item);
    public abstract void onSend(CallMsg use);
    public abstract void onUpdate(CallMsg item);
    public abstract void onContents(int pos);

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTvCategory;
        public TextView mTvTitle;
        public TextView mTvMessage;
        public TextView mTvDate;
        public ImageView mIvPhoto;
        public LinearLayout mLayoutUpdate;
        public LinearLayout mLayoutDelete;
        public LinearLayout mLayoutSend;

        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = itemView.findViewById(R.id.cv_base);
            mTvCategory = itemView.findViewById(R.id.tv_category);
            mTvTitle = itemView.findViewById(R.id.tv_title);
            mTvMessage = itemView.findViewById(R.id.tv_msg);
            mTvDate = itemView.findViewById(R.id.tv_date);
            mIvPhoto = itemView.findViewById(R.id.iv_photo);
            mLayoutUpdate = itemView.findViewById(R.id.ll_update);
            mLayoutDelete = itemView.findViewById(R.id.ll_delete);
            mLayoutSend = itemView.findViewById(R.id.ll_send);
        }
    }
}


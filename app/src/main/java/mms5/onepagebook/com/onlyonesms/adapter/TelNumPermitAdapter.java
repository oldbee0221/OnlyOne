package mms5.onepagebook.com.onlyonesms.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.db.entity.TelNumPermit;

public abstract class TelNumPermitAdapter extends RecyclerView.Adapter<TelNumPermitAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<TelNumPermit> mItems;

    public TelNumPermitAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_telnum, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TelNumPermit item = mItems.get(position);

        holder.mTvName.setText(item.name);
        holder.mTvNum.setText(item.num);

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

    public int add(List<TelNumPermit> arrayList) {
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

    public void remove(TelNumPermit item) {
        int size = mItems.size();
        for (int i = 0; i < size; i++) {
            if (mItems.get(i).tid == item.tid) {
                mItems.remove(i);
                break;
            }
        }

        notifyDataSetChanged();
    }

    public abstract void load();

    public abstract void onDel(TelNumPermit item);

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTvName;
        public TextView mTvNum;
        public LinearLayout mLayoutDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = itemView.findViewById(R.id.cv_base);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvNum = itemView.findViewById(R.id.tv_telnum);
            mLayoutDelete = itemView.findViewById(R.id.ll_delete);
        }
    }
}
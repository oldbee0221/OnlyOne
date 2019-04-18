package mms5.onepagebook.com.onlyonesms.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.base.GlideApp;
import mms5.onepagebook.com.onlyonesms.db.entity.ImageBox;

public abstract class ImgAdapter extends RecyclerView.Adapter<ImgAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<ImageBox> mItems;

    public ImgAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_img, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ImageBox item = mItems.get(position);

        Bitmap bm = BitmapFactory.decodeFile(item.imgPath);
        GlideApp.with(mContext)
                .load(bm)
                .into(holder.mIvPhoto);

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

    public int add(List<ImageBox> arrayList) {
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

    public void remove(ImageBox item) {
        int size = mItems.size();
        for (int i = 0; i < size; i++) {
            if (mItems.get(i).iid == item.iid) {
                mItems.remove(i);
                break;
            }
        }

        notifyDataSetChanged();
    }

    public abstract void load();

    public abstract void onDel(ImageBox item);

    public abstract void onAdoption(ImageBox use);

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public ImageView mIvPhoto;
        public LinearLayout mLayoutAdoption;
        public LinearLayout mLayoutDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = itemView.findViewById(R.id.cv_base);
            mIvPhoto = itemView.findViewById(R.id.iv_photo);
            mLayoutAdoption = itemView.findViewById(R.id.ll_adoption);
            mLayoutDelete = itemView.findViewById(R.id.ll_delete);
        }
    }
}



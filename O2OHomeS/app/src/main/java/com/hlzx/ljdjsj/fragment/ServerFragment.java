package com.hlzx.ljdjsj.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.activity.ExpressServeActivity;

/**
 * Created by alan on 2015/12/8.
 */
public class ServerFragment extends Fragment implements View.OnClickListener {

    private GridView mServerGrid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_server, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mServerGrid = (GridView) getView().findViewById(R.id.serve_gv);
        mServerGrid.setAdapter(new ServerAdapter());
        mServerGrid.setOnItemClickListener(itemClickListener);

    }

    @Override
    public void onClick(View v) {

    }

    AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             startActivity(new Intent(getActivity(), ExpressServeActivity.class));
        }
    };

    public class ServerAdapter extends BaseAdapter {
        public ServerAdapter() {
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder=new ViewHolder();
                convertView=LayoutInflater.from(getActivity()).inflate(R.layout.item_serve,null);
                holder.img_iv=(ImageView)convertView.findViewById(R.id.img_iv);
                holder.name_tv=(TextView)convertView.findViewById(R.id.serve_name_tv);
                convertView.setTag(holder);
            } else {
               holder=(ViewHolder)convertView.getTag();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView img_iv;
            TextView name_tv;
        }

    }

}

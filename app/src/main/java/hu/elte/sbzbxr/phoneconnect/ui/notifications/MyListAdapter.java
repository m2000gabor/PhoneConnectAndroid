package hu.elte.sbzbxr.phoneconnect.ui.notifications;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.HashMap;
import java.util.List;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.model.notification.NotificationPair;

public class MyListAdapter extends BaseAdapter {
    private final Activity activity;
    private final List<NotificationPair> list;
    private final HashMap<CharSequence,Integer> map; //appName, index in list

    public MyListAdapter(Activity activity, List<NotificationPair> list) {
        this.activity = activity;
        this.list=list;
        this.map = new HashMap<>();
        for(int i=0;i<list.size();i++){
            map.put(list.get(i).app,i);
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.list_item, container, false);
            }

            CheckBox checkBox = ((CheckBox) convertView.findViewById(R.id.notificationSingleAppCheckbox));
            NotificationPair pair = (NotificationPair) getItem(position);
            checkBox.setText(pair.app);
            checkBox.setChecked(pair.enabled);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Integer i = map.get(buttonView.getText());
                    if(i!=null) list.get(i).enabled=isChecked;
                }
            });
            return convertView;
        }
}

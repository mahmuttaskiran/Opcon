package com.opcon.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.opcon.R;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.fragments.NotifierFragment;
import com.opcon.ui.views.NotifierView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 25/10/2016.
 */

public class NotifierPermissionRationaleAlertDialog implements DialogInterface.OnClickListener {

    private AlertDialog.Builder builder;

    private DialogInterface.OnClickListener listener;

    public NotifierPermissionRationaleAlertDialog(Context context,
                                                  int id,
                                                  DialogInterface.OnClickListener listener)
    {
        this.listener = listener;

        this.builder = new AlertDialog.Builder(context);
        ArrayAdapter<String> adapter = getAdapter(context, getNotifierPermissionsDescriptions(context, id));

        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.not_now, this);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_notifier_permision, new RelativeLayout(context));

        ListView permissionDescsList = (ListView) view.findViewById(R.id.listView);
        NotifierView notifierView = (NotifierView) view.findViewById(R.id.notifierView);
        notifierView.fullView();

        Notifier notifierViewComponent = NotifierProvider.Utils.get(context, id);
        notifierView.with(notifierViewComponent);
        notifierView.hideRightSide();
        permissionDescsList.setAdapter(adapter);
        builder.setView(view);

        notifierView.hideNote();

    }

    public void show() {
         builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.dismiss();
        } else if (which == DialogInterface.BUTTON_POSITIVE) {
            dialog.dismiss();
        }
        if (this.listener != null) {
            this.listener.onClick(null, which);
        }
    }

    private ArrayAdapter<String> getAdapter(Context context, List<String> items) {
        return new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items);
    }

    private List<String> getNotifierPermissionsDescriptions(Context context, int id) {
        List<String> list = new ArrayList<>();
        String[] permissions = NotifierFragment.getNotifierPermissions(context, id);
        PackageManager pm = context.getPackageManager();

        for (String permission : permissions) {
            try {
                PermissionInfo mInfo =
                        pm.getPermissionInfo(permission, PackageManager.GET_META_DATA);
                String desc = mInfo.loadLabel(pm).toString();
                if (!list.contains(desc))
                    list.add(desc);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

}

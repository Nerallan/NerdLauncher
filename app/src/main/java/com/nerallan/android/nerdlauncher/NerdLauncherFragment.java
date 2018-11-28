package com.nerallan.android.nerdlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Nerallan on 11/22/2018.
 */

public class NerdLauncherFragment extends Fragment {
    private static final String TAG = "NerdLauncherFragment";

    private RecyclerView mRecyclerView;

    public static NerdLauncherFragment newInstance(){
        return new NerdLauncherFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_nerd_launcher_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setupAdapter();
        return view;
    }

    // create an instance of RecyclerView.Adapter and assign it to the RecyclerView object
    private void setupAdapter(){
        // Intent i = new Intent(Intent.ACTION_SEND);
        // i = Intent.createChooser(i, getString(R.string.send_report));
        // startActivity(i)

        // The MAIN / LAUNCHER intent filter may or may not match the implicit MAIN / LAUNCHER intent sent via startActivity (...)
        // The startActivity (Intent) call does not mean “Start an activity corresponding to this implicit intent.”
        // It means “Start the DEFAULT activity corresponding to this implicit intent”.

        // When you send an implicit intent using startActivity (...) (or startActivityForResult (...)),
        // the OS invisibly includes in intent the Intent category CATEGORY_DEFAULT.
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // intent filters of MAIN / LAUNCHER may not include CATEGORY_DEFAULT, the reliability of their compliance with implicit intents is not guaranteed.
        // Therefore, we use the intent for a direct request from the PackageManager for information about activities with filter MAIN / LAUNCHER.
        PackageManager pm = getActivity().getPackageManager();
        // Activity labels along with other metadata are contained in ResolveInfo objects returned by the PackageManager.
        // these activities are launcher activities, so the label should be the name of the application.
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);
        Collections.sort(activities, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                // sorting ResolveInfo objects returned by the PackageManager in alphabetical order of
                // the labels obtained by the ResolveInfo.loadLabel (...) method.
                PackageManager pm = getActivity().getPackageManager();
                return String.CASE_INSENSITIVE_ORDER.compare(
                    a.loadLabel(pm).toString(),
                    b.loadLabel(pm).toString());
            }
        });
        Log.i(TAG, "Found " + activities.size() + " activities.");
        mRecyclerView.setAdapter(new ActivityAdapter(activities));
    }


    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ResolveInfo mResolveInfo;
        private TextView mNameTextView;

        public ActivityHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView;
            mNameTextView.setOnClickListener(this);
        }

        public void bindActivity(ResolveInfo pResolveInfo){
            mResolveInfo = pResolveInfo;
            PackageManager pm = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(pm).toString();
            mNameTextView.setText(appName);
        }

        @Override
        public void onClick(View v) {
            // To create an explicit intent, you must extract the package name and the name of the activity class from ResolveInfo.
            // This data can be obtained from the ResolveInfo part with the name ActivityInfo.
            ActivityInfo activityInfo = mResolveInfo.activityInfo;
            Intent i = new Intent(Intent.ACTION_MAIN)
                    // 2nd argument - class name
                    .setClassName(activityInfo.applicationInfo.packageName, activityInfo.name);
            startActivity(i);
        }
    }


    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {
        private final List<ResolveInfo> mActivities;

        public ActivityAdapter(List<ResolveInfo> pActivities) {
            mActivities = pActivities;
        }


        @NonNull
        @Override
        public ActivityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ActivityHolder holder, int position) {
            ResolveInfo resolveInfo = mActivities.get(position);
            holder.bindActivity(resolveInfo);
        }

        @Override
        public int getItemCount() {
            return mActivities.size();
        }
    }
}

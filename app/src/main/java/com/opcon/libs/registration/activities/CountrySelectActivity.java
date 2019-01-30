package com.opcon.libs.registration.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.opcon.R;
import com.opcon.libs.registration.utils.CountryUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import timber.log.Timber;


public class CountrySelectActivity extends AppCompatActivity {

    public static final String NAME = "n";
    public  static final String DIAL_CODE = "dc";

    @BindView(R.id.edittext)
    EditText mSearchBox;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.activity_country_select)
    RelativeLayout   mLLRoot;

    @BindView(R.id.fastscroll)
    FastScroller mFastScroller;

    MenuItem miClose, miSearch;

    LinearLayoutManager mLayoutManager;
    CountryAdapter mAdapter;
    List<CountryUtils.Country> mCountries;

    CountryAdapter.CountrySelectListener mSelectListener = new CountryAdapter.CountrySelectListener() {
        @Override
        public void onCountrySelect(CountryUtils.Country country) {
            Intent i = new Intent();
            i.putExtra(NAME, country.getName());
            i.putExtra(DIAL_CODE, country.getDialCode());
            setResult(RESULT_OK, i);
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null);
        mCountries.clear();
        mCountries = null;
        mAdapter = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_select);
        ButterKnife.bind(this);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(14);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Drawable ic = getResources().getDrawable(R.drawable.ic_keyboard_backspace_material_grey);
            getSupportActionBar().setHomeAsUpIndicator(ic);
        }



        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(5);
            getSupportActionBar().setTitle("");
        }

        mLayoutManager = new LinearLayoutManager(getApplicationContext(),
            LinearLayoutManager.VERTICAL, false);
        mAdapter = new CountryAdapter(mSelectListener, (mCountries = CountryUtils.getCountries(getApplicationContext())));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mFastScroller.setRecyclerView(mRecyclerView);

        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mSearchBox.getText().toString().isEmpty()) {
                    mAdapter.setCountries(mCountries);
                    miClose.setVisible(false);
                    miSearch.setVisible(true);
                } else {
                    mAdapter.setCountries(filterCountries(mSearchBox.getText().toString()));
                    miClose.setVisible(true);
                    miSearch.setVisible(false);
                }

            }

            private List<CountryUtils.Country> filterCountries(String s) {
                List<CountryUtils.Country> filtered = new ArrayList<>();
                for (CountryUtils.Country country: mCountries) {
                    if (country.getName().toLowerCase().contains(s.toLowerCase())) {
                        filtered.add(country);
                    }
                }
                return filtered;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simiple_search, menu);
        miClose =  menu.findItem(R.id.menu_main_close);
        miSearch = menu.findItem(R.id.menu_main_search);

        Drawable icon = miClose.getIcon();
        if (icon !=null) {
            icon.setColorFilter(getResources().getColor(R.color.materialGrey), PorterDuff.Mode.MULTIPLY);
        }

        Drawable icon1 = miSearch.getIcon();
        if (icon1 != null) {
            icon1.setColorFilter(getResources().getColor(R.color.materialGrey), PorterDuff.Mode.MULTIPLY
            );
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_close:
                mSearchBox.setText("");
                break;
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.Holder>
    implements SectionTitleProvider{

        @Override
        public String getSectionTitle(int position) {
            Timber.e(String.valueOf(mCountries.get(position).getName().charAt(0)));
            return String.valueOf(mCountries.get(position).getName().charAt(0));
        }

        interface CountrySelectListener {
            void onCountrySelect(CountryUtils.Country country);
        }

        CountrySelectListener mSelectListener;
        List<CountryUtils.Country> mCountries;

        CountryAdapter(CountrySelectListener mSelectListener, List<CountryUtils.Country> countries) {
            this.mSelectListener = mSelectListener;
            this.mCountries = countries;
        }

        @Override
        public void onBindViewHolder(CountryAdapter.Holder holder, int position) {
            holder.with(mCountries.get(position));
        }

        @Override
        public int getItemCount() {
            return mCountries.size();
        }

        void setCountries(List<CountryUtils.Country> countries) {
            mCountries = countries;
            notifyDataSetChanged();
        }

        @Override
        public CountryAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_country, parent, false), this);
        }

        static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView mName, mDialCode;
            LinearLayout mRoot;
            CountryAdapter mAdapter;
            Holder(View itemView, CountryAdapter mSelectListener) {
                super(itemView);
                mAdapter = mSelectListener;
                mName = (TextView) itemView.findViewById(R.id.rowcountry_name);
                mDialCode = (TextView) itemView.findViewById(R.id.rowcountry_dialcode);
                mRoot = (LinearLayout) itemView.findViewById(R.id.rowcountry_llRoot);
                mRoot.setOnClickListener(this);
            }
            public void with(CountryUtils.Country c) {
                this.mName.setText(c.getName());
                this.mDialCode.setText(String.valueOf(c.getDialCode()));
            }

            @Override
            public void onClick(View v) {
                if (mAdapter.mSelectListener != null && getAdapterPosition() != -1)  {
                    mAdapter.mSelectListener.onCountrySelect(mAdapter.mCountries.get(getAdapterPosition()));
                }
            }
        }
    }

}

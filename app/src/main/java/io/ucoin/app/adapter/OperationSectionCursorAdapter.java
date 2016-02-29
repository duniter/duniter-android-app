package io.ucoin.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.ucoin.app.R;
import io.ucoin.app.enumeration.DayOfWeek;
import io.ucoin.app.enumeration.Month;
import io.ucoin.app.enumeration.TxDirection;
import io.ucoin.app.enumeration.TxState;
import io.ucoin.app.Format;
import io.ucoin.app.sqlite.SQLiteView;

public class OperationSectionCursorAdapter extends CursorAdapter {

    private Context mContext;
    private Cursor mCursor;
    private int dayOfWeekIndex;
    private int dayIndex;
    private int hourIndex;
    private int timeAmountThenIndex;
    private int relAmountThenIndex;
    private int directionIndex;
    private int qtAmountIndex;
    private int commentIndex;
    private int stateIndex;

    private BigInteger mUd;
    private int delay;


    private HashMap<Integer, String> mSectionPosition;

    public OperationSectionCursorAdapter(Context context, Cursor c, int flags, BigInteger mUd, int delay) {
        super(context, c, flags);
        mContext = context;
        mCursor = c;
        this.mUd = mUd;
        this.delay = delay;
        mSectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (mSectionPosition.containsKey(position)) {
            v = newSectionView(mContext, parent);
            bindSectionView(v, mContext, mSectionPosition.get(position));
        } else {
            int sectionBeforePosition = 0;
            for (Integer sectionPosition : mSectionPosition.keySet()) {
                if (position > sectionPosition) {
                    sectionBeforePosition++;
                }
            }
            if (!mCursor.moveToPosition(position - sectionBeforePosition)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            v = newView(mContext, mCursor, parent);
            bindView(v, mContext, mCursor);
        }
        return v;
    }

    public View newSectionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.list_item_section_separator, parent, false);
    }

    public void bindSectionView(View v, Context context, String section) {
        ((TextView) v.findViewById(R.id.section_name)).setText(section);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_item_tx, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.day = (TextView) view.findViewById(R.id.day);
        holder.hour = (TextView) view.findViewById(R.id.hour);
        holder.amount = (TextView) view.findViewById(R.id.amount);
        holder.defaultAmount = (TextView) view.findViewById(R.id.second_amount);
        holder.comment = (TextView) view.findViewById(R.id.comment);

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();
        String d = cursor.getString(dayOfWeekIndex);
        if (d == null) d = Integer.toString(DayOfWeek.UNKNOWN.ordinal());

        String dayOfWeek = DayOfWeek.fromInt(Integer.parseInt(d),false).toString(context);

        holder.day.setText(dayOfWeek + " " + cursor.getString(dayIndex));
        holder.hour.setText(cursor.getString(hourIndex));
        DecimalFormat formatter;
        String dir ="";
        if (cursor.isNull(directionIndex) ||
                TxDirection.valueOf(cursor.getString(directionIndex)) == TxDirection.IN) {
            dir = "+ ";
        } else {
            dir = "- ";
        }

        Format.Currency.changeUnit(context, "NAN", new BigInteger(cursor.getString(qtAmountIndex)), mUd, delay, holder.amount, holder.defaultAmount, dir);

        holder.comment.setText(cursor.getString(commentIndex));

        if (cursor.isNull(stateIndex)) {
            view.setBackgroundColor(context.getResources().getColor(R.color.primaryLight));
        } else if (TxState.valueOf(cursor.getString(stateIndex)) == TxState.CONFIRMED) {
            view.setBackgroundColor(context.getResources().getColor(R.color.grey200));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.accentLight));
        }
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);

        if (newCursor == null) {
            return null;
        }

        dayOfWeekIndex = newCursor.getColumnIndex(SQLiteView.Operation.DAY_OF_WEEK);
        dayIndex = newCursor.getColumnIndex(SQLiteView.Operation.DAY);
        hourIndex = newCursor.getColumnIndex(SQLiteView.Operation.HOUR);
        relAmountThenIndex = newCursor.getColumnIndex(SQLiteView.Operation.RELATIVE_AMOUNT_THEN);
        timeAmountThenIndex = newCursor.getColumnIndex(SQLiteView.Operation.TIME_AMOUNT_THEN);
        directionIndex = newCursor.getColumnIndex(SQLiteView.Operation.DIRECTION);
        qtAmountIndex = newCursor.getColumnIndex(SQLiteView.Operation.QUANTITATIVE_AMOUNT);
        commentIndex = newCursor.getColumnIndex(SQLiteView.Operation.COMMENT);
        stateIndex = newCursor.getColumnIndex(SQLiteView.Operation.STATE);

        mCursor = newCursor;
        mSectionPosition.clear();
        int position = 0;
        String section = "";

        newCursor.moveToPosition(-1);

        HashMap<Integer, String> sectionPosition = new LinkedHashMap<>(16, (float) 0.75, false);
        while (newCursor.moveToNext()) {
            String month = newCursor.getString(newCursor.getColumnIndex(SQLiteView.Tx.MONTH));
            //todo handle timestamp for sending and receiving transactions
            if (month == null) month = Integer.toString(Month.UNKNOWN.ordinal());
            String year = newCursor.getString(newCursor.getColumnIndex(SQLiteView.Tx.YEAR));
            String newSection = Month.fromInt(Integer.parseInt(month)).toString(mContext) + " " + year;

            if (!newSection.equals(section)) {
                sectionPosition.put(position, newSection);
                section = newSection;
                position++;
            }
            position++;
        }
        mSectionPosition = sectionPosition;
        notifyDataSetChanged();

        return newCursor;
    }

    @Override
    public int getCount() {
        return super.getCount() + mSectionPosition.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return !mSectionPosition.containsKey(position);
    }


    private static class ViewHolder {
        public TextView day;
        public TextView hour;
        public TextView amount;
        public TextView defaultAmount;
        public TextView comment;
    }

}
package io.predict.example.journeys_app.trip_view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.predict.example.R;
import io.predict.example.journeys_app.location.models.JourneyData;


/**
 * Created by meghal on 25/5/17.
 */

public class JourneyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<JourneyData> journeyDataList = new ArrayList<>();
    private LayoutInflater layoutInflater;

    public JourneyAdapter(Context context, List<JourneyData> journeyDataList) {
        this.context = context;
        this.journeyDataList = journeyDataList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.journey_list_item, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final JourneyData journeyData = journeyDataList.get(position);

        ((JourneyViewHolder) holder).
                start_time_textview.setText("Start Time: "+String.valueOf(journeyData.getJourney_start__date_time()));

        ((JourneyViewHolder) holder).
                end_time_textview.setText("End Time: "+String.valueOf(journeyData.getJourney_end_date_time()));

        ((JourneyViewHolder) holder).
                journey_id_textview.setText("Journey Id: "+String.valueOf(journeyData.getJourney_id()));

        ((JourneyViewHolder) holder).travel_time_textview.setText("Travel Time: "+String.valueOf(journeyData.getTravel_time()));
        ((JourneyViewHolder) holder).distracted_time_textview.setText("Distracted Time: "+String.valueOf(journeyData.getDistracted_time()));
        ((JourneyViewHolder) holder).journey_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context instanceof JourneysActivity){
                    ((JourneysActivity)context).openMapsActivity(
                            journeyDataList.get(position).getJourney_id(),
                            journeyDataList.get(position).getTravel_time(),
                            journeyDataList.get(position).getDistracted_time()
                            );
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return journeyDataList.size();
    }

    private class JourneyViewHolder extends RecyclerView.ViewHolder {
        TextView end_time_textview;
        TextView start_time_textview;
        TextView journey_id_textview;
        TextView travel_time_textview;
        TextView distracted_time_textview;
        CardView journey_card;

        public JourneyViewHolder(View view) {
            super(view);
            journey_card=(CardView)view.findViewById(R.id.journey_card);
            end_time_textview = (TextView) view.findViewById(R.id.end_time);
            start_time_textview = (TextView) view.findViewById(R.id.start_time);
            journey_id_textview = (TextView) view.findViewById(R.id.journey_id);
            travel_time_textview=(TextView)view.findViewById(R.id.travel_time);
            distracted_time_textview=(TextView)view.findViewById(R.id.distracted_time);

        }
    }
}

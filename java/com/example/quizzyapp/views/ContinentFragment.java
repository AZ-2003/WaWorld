package com.example.quizzyapp.views;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.quizzyapp.R;

public class ContinentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_continent, container, false);

        // Initialize buttons for each continent
        Button asiaButton = rootView.findViewById(R.id.buttonAsia);
        Button europeButton = rootView.findViewById(R.id.buttonEurope);
        Button africaButton = rootView.findViewById(R.id.buttonAfrica);
        Button namericaButton = rootView.findViewById(R.id.buttonNAmerica);
        Button samericaButton = rootView.findViewById(R.id.buttonSAmerica);
        Button antarticaButton = rootView.findViewById(R.id.buttonAntarctica);
        Button australiaButton = rootView.findViewById(R.id.buttonAustralia);

        // Set up click listeners for each button
        View.OnClickListener continentClickListener = view -> {
            Bundle bundle = new Bundle();
            String selectedContinent = "";

            if (view == asiaButton) selectedContinent = "Asia";
            if (view == europeButton) selectedContinent = "Europe";
            if (view == africaButton) selectedContinent = "Africa";
            if (view == namericaButton) selectedContinent = "North America";
            if (view == samericaButton) selectedContinent = "South America";
            if (view == antarticaButton) selectedContinent = "Antarctica";
            if (view == australiaButton) selectedContinent = "Australia";

            bundle.putString("continent", selectedContinent);

            // Navigate to QuizFragment with the selected continent
            Navigation.findNavController(view).navigate(R.id.action_continentFragment_to_difficultyFragment, bundle);
        };

        asiaButton.setOnClickListener(continentClickListener);
        europeButton.setOnClickListener(continentClickListener);
        africaButton.setOnClickListener(continentClickListener);
        namericaButton.setOnClickListener(continentClickListener);
        samericaButton.setOnClickListener(continentClickListener);
        antarticaButton.setOnClickListener(continentClickListener);
        australiaButton.setOnClickListener(continentClickListener);

        return rootView;
    }
}
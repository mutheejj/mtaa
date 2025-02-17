import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mtaa.R;

public class CreateAccountFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_account, container, false);

        // Initialize back button
        ImageButton backButton = view.findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> {
            // Navigate back
            Navigation.findNavController(view).navigateUp();
        });

        // ... rest of your existing initialization code ...

        return view;
    }
} 
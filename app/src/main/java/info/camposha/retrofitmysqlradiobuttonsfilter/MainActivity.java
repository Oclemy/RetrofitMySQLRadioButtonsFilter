package info.camposha.retrofitmysqlradiobuttonsfilter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {

/**
   * STEP 1. PREPARATION OF URLS
   * I will use my ip address as my base url. This is because am using
    emulator as my testing device. You can obtain yours via cmd.
   */
  private static final String BASE_URL = "http://192.168.12.2";//This is my ip address.
  // "http://10.0.2.2"; or "http://10.0.3.2"; can also be used in some emulators.
  private static final String FULL_URL = BASE_URL + "/PHP/spaceship/";

  /**
   * STEP 2. PREPARATION OF DATA OBJECT
   * The serialized names must correspond to your json keys.
   */
  class Spacecraft {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("propellant")
    private String propellant;
    @SerializedName("destination")
    private String destination;
    @SerializedName("image_url")
    private String imageURL;
    @SerializedName("technology_exists")
    private int technologyExists;


//FIRST constructor
 public Spacecraft(String name) {
  this.name = name;
 }

 //SECOND CONSTRUCTOR. This is the one we will mainly use
 public Spacecraft(int id, String name, String propellant, String destination,
 String imageURL,
    int technologyExists) {
  this.id = id;
  this.name = name;
  this.propellant = propellant;
  this.destination = destination;
  this.imageURL = imageURL;
  this.technologyExists = technologyExists;
 }

    /*
     * I WILL GENERATE GETTERS AND SETTERS
     */
    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public String getPropellant() {
      return propellant;
    }

    public String getDestination() {
      return destination;
    }

    public String getImageURL() {
      return imageURL;
    }

    public int getTechnologyExists() {
      return technologyExists;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * PREPARATION OF HTTP CLIENT INTERFACE
   * This will contain methods to be mapped to our api.
   * We make a HTTP GET and POST requests.
   */
  interface MyAPIService {

    //HTTP GET request to retrieve all spaceship
    @GET("/PHP/spaceship")
    Call<List<Spacecraft>> getSpacecrafts();

    //HTTP POST request to search our php mysql server
    @FormUrlEncoded
    @POST("/PHP/spaceship/index.php")
    Call<List<Spacecraft>> searchSpacecraft(@Field("query") String query);
  }

  /**
   *Our factory class.
   RESPONSIBILITY: Generate a Retrofit instance we can re-use easily.
   */
  static class RetrofitClientInstance {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
      if (retrofit == null) {
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
          GsonConverterFactory.create()).build();
      }
      return retrofit;
    }
  }

  /**
   *Our ListView adapter class.
   RESPONSIBILITY: Inflate our custom layouts into view items then adapt data to them
   */
  class ListViewAdapter extends BaseAdapter {

    private List<Spacecraft> spacecrafts;
    private Context context;

    //Let's receive a context and list of spacecrafts
    public ListViewAdapter(Context context, List<Spacecraft> spacecrafts) {
      this.context = context;
      this.spacecrafts = spacecrafts;
    }

    //I will implement abstract methods defined in the BaseAdapter class
    @Override
    public int getCount() {
      return spacecrafts.size();
    }

    @Override
    public Object getItem(int pos) {
      return spacecrafts.get(pos);
    }

    @Override
    public long getItemId(int pos) {
      return pos;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
      //I will only inflate our model layout if it's null
      if (view == null) {
        view = LayoutInflater.from(context).inflate(R.layout.model, viewGroup, false);
      }

      //I will reference the widgets contained in that inflated layout
      TextView nameTxt = view.findViewById(R.id.nameTextView);
      TextView txtPropellant = view.findViewById(R.id.propellantTextView);
      CheckBox chkTechExists = view.findViewById(R.id.myCheckBox);
      ImageView spacecraftImageView = view.findViewById(R.id.spacecraftImageView);

      final Spacecraft thisSpacecraft = spacecrafts.get(position);

      //Am setting values into their respective views
      nameTxt.setText(thisSpacecraft.getName());
      txtPropellant.setText(thisSpacecraft.getPropellant());
      chkTechExists.setChecked(thisSpacecraft.getTechnologyExists() == 1);
      chkTechExists.setEnabled(false);

      //Am loading an image from url via Picasso
      if (thisSpacecraft.getImageURL() != null && thisSpacecraft.getImageURL().length() > 0) {
        Picasso.get().load(FULL_URL + "/images/" + thisSpacecraft.getImageURL()).placeholder
        (R.drawable.placeholder)
            .into(spacecraftImageView);
      } else {
        Toast.makeText(context, "Empty Image URL", Toast.LENGTH_LONG).show();
        Picasso.get().load(R.drawable.placeholder).into(spacecraftImageView);
      }

      //when our itemview for our ListView is clicked we will open a new activity
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Toast.makeText(context, thisSpacecraft.getName(), Toast.LENGTH_SHORT).show();
          String techExists = "";
          if (thisSpacecraft.getTechnologyExists() == 1) {
            techExists = "YES";
          } else {
            techExists = "NO";
          }
          String[] spacecrafts = { thisSpacecraft.getName(), thisSpacecraft.getPropellant(),
              thisSpacecraft.getDestination(), techExists, FULL_URL + "/images/" +
              thisSpacecraft.getImageURL() };
          openDetailActivity(spacecrafts);
        }
      });

      return view;
    }

    //Am creating a method to Open our detail activity passing the spacecraft details.
    private void openDetailActivity(String[] data) {
      Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
      intent.putExtra("NAME_KEY", data[0]);
      intent.putExtra("PROPELLANT_KEY", data[1]);
      intent.putExtra("DESTINATION_KEY", data[2]);
      intent.putExtra("TECHNOLOGY_EXISTS_KEY", data[3]);
      intent.putExtra("IMAGE_KEY", data[4]);
      startActivity(intent);
    }
  }

  //MainActivity instance fields. Add them within the context of main activity.
  private ListViewAdapter adapter;
  private ListView mListView;
  private ProgressBar mProgressBar;
  private RadioGroup mRadioGroup;
  private RadioButton nuclearRadioButton, plasmaRadioButton, allRadioButton,
  laserRadioButton, warpRadioButton;
  private Call<List<Spacecraft>> call;

  //I will then create a method to Initialize several views we are using
  private void initializeWidgets() {
    mListView = findViewById(R.id.mListView);
    mProgressBar = findViewById(R.id.mProgressBar);
    mProgressBar.setIndeterminate(true);
    mProgressBar.setVisibility(View.VISIBLE);
    //reference the widgets
    mRadioGroup = findViewById(R.id.mRadioGroup);
    nuclearRadioButton = findViewById(R.id.nuclearRadioButton);
    plasmaRadioButton = findViewById(R.id.plasmaRadioButton);
    laserRadioButton = findViewById(R.id.laserRadioButton);
    warpRadioButton = findViewById(R.id.warpRadioButton);
    allRadioButton = findViewById(R.id.allRadioButton);

  }

  //Am creating a method to help Populate our ListView with data
  private void populateListView(List<Spacecraft> spacecraftList) {
    adapter = new ListViewAdapter(this, spacecraftList);
    mListView.setAdapter(adapter);
  }

  //Our onCreate callback
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    this.initializeWidgets();

    /*Create handle for the RetrofitInstance interface*/
    final MyAPIService myAPIService = RetrofitClientInstance.getRetrofitInstance().
    create(MyAPIService.class);

    //Call<List<Spacecraft>> call = myAPIService.getSpacecrafts();
    call = myAPIService.searchSpacecraft("");
    call.enqueue(new Callback<List<Spacecraft>>() {
      //Am now going to handle successful response. Am showing progress bar and populating
      //listview
      @Override
      public void onResponse(Call<List<Spacecraft>> call, Response<List<Spacecraft>>
      response) {
        mProgressBar.setVisibility(View.GONE);
        populateListView(response.body());
      }

      @Override
      public void onFailure(Call<List<Spacecraft>> call, Throwable throwable) {
        mProgressBar.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
      }
    });

    //when any radiobutton in our radiogroup is checked, I will show its value
    mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i) {
        //I will then get my checked radiobutton from the radiogroup
        int checkedItemId = mRadioGroup.getCheckedRadioButtonId();
        RadioButton checkedRadioButton = findViewById(checkedItemId);
        String category = checkedRadioButton.getText().toString();
        Toast.makeText(MainActivity.this, category, Toast.LENGTH_SHORT).show();

        //I will check the category.
        if (category.equalsIgnoreCase("All")) {
          //then I return all categories
          call = myAPIService.searchSpacecraft("");
        } else {
          //otherwis I return a specific category
          call = myAPIService.searchSpacecraft(category);
        }

        //I will use `Enqueue` to queue our HTTP call in a background thread where it will be
        //called
        call.enqueue(new Callback<List<Spacecraft>>() {
          //If I get a successful response
          @Override
          public void onResponse(Call<List<Spacecraft>> call, Response<List<Spacecraft>>
           response) {
            mProgressBar.setVisibility(View.GONE);
            populateListView(response.body());
          }

          //If I fail, I will show my error in a toast message.
          @Override
          public void onFailure(Call<List<Spacecraft>> call, Throwable throwable) {
            populateListView(new ArrayList<Spacecraft>());
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "ERROR: " + throwable.getMessage(),
            Toast.LENGTH_LONG).show();
          }
        });
      }
    });

  }
}
//end

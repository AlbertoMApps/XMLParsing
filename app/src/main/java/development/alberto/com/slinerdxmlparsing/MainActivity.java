package development.alberto.com.slinerdxmlparsing;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements ResultsCallBack {

    private PlaceholderFragment placeholderFragment;
    private ListView articlesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState==null) {
            placeholderFragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction().add(placeholderFragment, "placeHolder").commit();
        } else {
            placeholderFragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("placeHolder");
        }
        placeholderFragment.startTask();
        articlesListView = (ListView) findViewById(R.id.listview);

    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onPostExecute(ArrayList<HashMap<String,String>> dataSource) {
        MyAdapter myAdapter = new MyAdapter(this, dataSource);
        articlesListView.setAdapter(myAdapter);
    }

    public static class PlaceholderFragment extends Fragment {
        private MyTask myTask;
        private ResultsCallBack resultsCallBack;

        public PlaceholderFragment() {
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            resultsCallBack = (ResultsCallBack) activity;
            if(myTask!=null){
                myTask.onAttach((Activity) resultsCallBack);
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setRetainInstance(true);
        }
        public void startTask() {
            if (myTask == null) {
                this.myTask = new MyTask(resultsCallBack);
                myTask.execute();
            } else {
                myTask.cancel(true);
            }
        }

        @Override
        public void onDetach() {
            super.onDetach();
            resultsCallBack = null;
            if(myTask!=null){
                myTask.onDetach();
            }
        }
    }
    public static class MyTask extends AsyncTask <Void, Void, ArrayList<HashMap<String,String>>> {
        ResultsCallBack resultsCallBack;

        public MyTask(ResultsCallBack callBack){
            this.resultsCallBack = callBack;
        }

        public void onAttach(Activity activity){
            resultsCallBack = (ResultsCallBack) activity;
        }
        public void onDetach(){
            resultsCallBack = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(resultsCallBack!=null){
                resultsCallBack.onPreExecute();
            }
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... params) {
            String downloadURL = "http://www.feedforall.com/sample.xml";
            ArrayList<HashMap<String,String>> dataSource = null;
            try {
                URL url = new URL(downloadURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                InputStream inputStream = httpURLConnection.getInputStream();
                dataSource = processXML(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return dataSource;
        }
        public ArrayList<HashMap<String,String>>  processXML(InputStream inputStream) throws Exception {

            ArrayList<HashMap<String,String>> dataSource = new ArrayList<>();
            HashMap<String, String> hashMap = new HashMap<>();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document xmlDocument = documentBuilder.parse(inputStream);
            Element rootElement = xmlDocument.getDocumentElement();
            Log.i("TAG: ", rootElement.getTagName());
            NodeList itemList = rootElement.getElementsByTagName("item");
            NodeList itemChildrenList = null;
            Node currentItem = null;
            Node currentChild = null;
            for(int i =0; i<itemList.getLength();i++){
                currentItem = itemList.item(i);
                Log.i("TAGS 2: ", currentItem.getNodeName() );
                itemChildrenList = currentItem.getChildNodes();
                for (int j = 0; j<itemChildrenList.getLength();j++){
                    currentChild = itemChildrenList.item(j);
                    if(currentChild.getNodeName().equalsIgnoreCase("title")) {
                        Log.i("TAGS 2 : ", currentChild.getNodeName() +":"+ currentChild.getTextContent());
                        hashMap.put( currentChild.getNodeName(), currentChild.getTextContent() );
                    }
                }
                dataSource.add(hashMap);
            }
            return dataSource;
        }

        @Override
        protected void onPostExecute( ArrayList<HashMap<String,String>> dataSource) {
            super.onPostExecute(dataSource);
            if(resultsCallBack!=null){
                resultsCallBack.onPostExecute(dataSource);
            }
        }
    }

}
interface ResultsCallBack {
    public void onPreExecute();
    public void onPostExecute( ArrayList<HashMap<String,String>> dataSource );
}

class MyAdapter extends BaseAdapter {

    ArrayList<HashMap<String,String>> dataSource;
    Context context;
    LayoutInflater layoutInflater;

    public MyAdapter(Context context,  ArrayList<HashMap<String,String>> dataSource){
        this.dataSource  = dataSource;
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MyHolder holder = null;
        if(row==null){
            row = layoutInflater.inflate(R.layout.row_layout, parent, false);
            holder = new MyHolder(row);
            row.setTag(holder);
        } else {
            holder = (MyHolder) row.getTag();
        }
        HashMap<String,String> dataSource = this.dataSource.get(position);
        holder.txt.setText(dataSource.get("title").toString());
        return row;
    }
    class MyHolder{
        TextView txt;
        public MyHolder(View view){
            txt = (TextView) view.findViewById(R.id.tx1);
        }
    }
}

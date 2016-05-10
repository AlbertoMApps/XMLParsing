package development.alberto.com.slinerdxmlparsing;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private PlaceholderFragment placeholderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState==null){
            placeholderFragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction().add(placeholderFragment, "placeHolder").commit();
        } else {
            placeholderFragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("placeHolder");
        }
        placeholderFragment.startTask();
    }

    public static class PlaceholderFragment extends Fragment {
        private MyTask myTask;
        public PlaceholderFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setRetainInstance(true);
        }
        public void startTask() {
            if (myTask == null) {
                this.myTask = new MyTask();
                myTask.execute();
            } else {
                myTask.cancel(true);
            }
        }
    }
    public static class MyTask extends AsyncTask <Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String downloadURL = "http://www.feedforall.com/sample.xml";
            try {
                URL url = new URL(downloadURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                InputStream inputStream = httpURLConnection.getInputStream();
                processXML(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        public void processXML(InputStream inputStream) throws Exception {
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
                    }
                }
            }
        }
    }
}

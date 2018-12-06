
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Copyright 2018 Rosun Nassir.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 * @author Rosun Nassir
 */
public class AdsBlocker {

    private int count = 0;
    private ArrayList<String> places = new ArrayList<>();

    public ArrayList<String> readHostFile() {
        Thread reader;
        reader = new Thread() {
            @Override
            public void run() {
                BufferedReader bf = null;
                try {
                    System.out.println("in thread");
                    URL resource = getClass().getClassLoader().getResource("resource/ads.txt");
                    InputStream inputStream = resource.openConnection().getInputStream();
                    
                    Reader targetReader = new InputStreamReader(inputStream);
                    bf = new BufferedReader(targetReader);
                    String thisLine = "";
                    while ((thisLine = bf.readLine()) != null) {
                        String domain = thisLine.substring(thisLine.indexOf("1 ") + 2);
                        getPlaces().add(domain);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(AdsBlocker.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(AdsBlocker.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        bf.close();
                    } catch (IOException ex) {
                        Logger.getLogger(AdsBlocker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        reader.run();
        ArrayList<String> list = null;
        if (!reader.isAlive()) {
            System.out.println("Dead");
            list = getList();
            System.out.println("Size " + list.size());
        }
        return list;
    }

    public ArrayList<String> getList() {
        return getPlaces();
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the places
     */
    public ArrayList<String> getPlaces() {
        return places;
    }

    /**
     * @param places the places to set
     */
    public void setPlaces(ArrayList<String> places) {
        this.places = places;
    }

}

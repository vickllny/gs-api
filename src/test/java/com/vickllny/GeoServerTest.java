package com.vickllny;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

public class GeoServerTest {

    static final Logger LOGGER = LoggerFactory.getLogger(GeoServerTest.class);

    static final String USERNAME = "admin";
    static final String PASSWORD = "geoserver";

    static String REST_URL  = "http://localhost:8080/geoserver";

    static GeoServerRESTManager manager = null;

    static {
        try {
            manager = new GeoServerRESTManager(new URL(REST_URL), USERNAME, PASSWORD);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static final String WS_NAME = "ll";
    static String path = "/Users/vickllny/gis/srtm_60_06/srtm_60_06.tif";

    @Test
    public void createWorkspace(){
        final boolean workspace = manager.getPublisher().createWorkspace(WS_NAME);
        LOGGER.debug("createWorkspace result => {}", workspace);
    }

    @Test
    public void publishGeoTIFF() throws FileNotFoundException {
        String storeName = "srtm_60_06";
        String layerName = "srtm_60_06";
        final File tifFile = new File("/Users/vickllny/gis/srtm_60_06/srtm_60_06.tif");

        boolean published = manager.getPublisher().publishGeoTIFF(WS_NAME, storeName, layerName, tifFile);
        if (published) {
            LOGGER.debug("GeoTIFF published.");
        } else {
            LOGGER.debug("GeoTIFF publication failed.");
        }



    }

    @Test
    public void publishShp() throws FileNotFoundException {
        String storeName = "CHN_rds";
        String layerName = "CHN_roads";
        final File file = new File("/Users/vickllny/gis/CHN_rds.zip");
        final boolean published = manager.getPublisher().publishShp(WS_NAME, storeName, layerName, file, "EPSG:4326", "v_boundary_lines");
        if (published) {
            LOGGER.debug("Shp published.");
        } else {
            LOGGER.debug("Shp publication failed.");
        }
    }

    @Test
    public void unPublishShp() throws FileNotFoundException {
        String storeName = "CHN_rds";
        String layerName = "CHN_roads";
        boolean ftRemoved = manager.getPublisher().unpublishFeatureType(WS_NAME, storeName, layerName);
        boolean dsRemoved = manager.getPublisher().removeDatastore(WS_NAME, storeName, true);
        LOGGER.debug("ftRemoved: {}", ftRemoved);
        LOGGER.debug("dsRemoved: {}", dsRemoved);
    }

    @Test
    public void parseGetCapabilities(){
//        String url = "http://127.0.0.1:8080/geoserver/gwc/service/wmts?service=WMTS&version=1.1.1&request=GetCapabilities";
        URL url = null;
        try {
            url = new URL("http://127.0.0.1:8080/geoserver/gwc/service/wmts");
        } catch (MalformedURLException e) {
            // will not happen
        }

        WebMapTileServer wmts = null;
        try {
            wmts = new WebMapTileServer(url);
        } catch (IOException e) {
            // There was an error communicating with the server
            // For example, the server is down
        } catch (ServiceException e) {
            // The server returned a ServiceException (unusual in this case)
        } catch (SAXException e) {
            // Unable to parse the response from the server
            // For example, the capabilities it returned was not valid
        }
    }
}

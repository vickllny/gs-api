package com.vickllny;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.coverage.GSCoverageEncoder;
import org.geotools.ows.ServiceException;
import org.geotools.ows.wms.StyleImpl;
import org.geotools.ows.wmts.WebMapTileServer;
import org.geotools.ows.wmts.model.WMTSCapabilities;
import org.geotools.ows.wmts.model.WMTSLayer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
    public void cql(){
        String layerName = "CHN_roads";
        final GeoServerRESTReader reader = manager.getReader();
        //获取要更新的图层信息
        RESTLayer layer = reader.getLayer(WS_NAME, layerName);

        LOGGER.debug("123");
        // 构造更新请求的 XML
//        String xml = "<layer><resource><id>" + layer.getResource().getId() + "</id><name>" + layer.getResource().getName()
//                + "</name><href>" + layer.getResource().getHref() + "</href><title>" + layer.getResource().getTitle()
//                + "</title></resource><enabled>true</enabled><name>" + layer.getName() + "</name><queryable>true</queryable>"
//                + "<cql_filter>age < 32</cql_filter></layer>";

        // 发送更新请求

//        String response = HTTPUtils.postXml("/rest/layers/" + layer.getName() + ".xml", xml, USERNAME, PASSWORD);
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
            assert url != null;
            wmts = new WebMapTileServer(url);

            final WMTSCapabilities capabilities = wmts.getCapabilities();
            final List<WMTSLayer> layerList = capabilities.getLayerList();

            for (final WMTSLayer layer : layerList) {
                LOGGER.debug("Layer: " + layer.getName());
                LOGGER.debug("       " + layer.getTitle());

                for (StyleImpl style : layer.getStyles()) {
                    // Print style info
                    LOGGER.debug("Style:");
                    LOGGER.debug("  Name:  " + style.getName());
                    LOGGER.debug("  Title: " + style.getTitle());
                }
            }

        } catch (IOException e) {
            // There was an error communicating with the server
            // For example, the server is down
        } catch (ServiceException e) {
            // The server returned a ServiceException (unusual in this case)
        }

    }
}

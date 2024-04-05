package com.vickllny;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.encoder.datastore.GSPostGISDatastoreEncoder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.ows.ServiceException;
import org.geotools.ows.wms.StyleImpl;
import org.geotools.ows.wmts.WebMapTileServer;
import org.geotools.ows.wmts.model.WMTSCapabilities;
import org.geotools.ows.wmts.model.WMTSLayer;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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

    static final String host = "127.0.0.1";
    static final int port = 5433;
    static final String username = "postgres";
    static final String password = "postgres";
    static final String database = "gs";

    @Test
    public void readShp2db() throws IOException {
        final String shp = "/Users/vickllny/gis/CHN_rds/CHN_roads.shp";
        // 打开Shapefile文件
        final File file = new File(shp);
        final URL url = file.toURI().toURL();
        final ShapefileDataStore dataStore = new ShapefileDataStore(url);
        dataStore.setCharset(Charset.forName("GBK"));

        // 读取feature类型
        SimpleFeatureType featureType = dataStore.getSchema();
        System.out.println("Feature Type: " + featureType.getTypeName());

        // 读取feature数据
        SimpleFeatureCollection collection = dataStore.getFeatureSource().getFeatures();
        final List<List<String>> values = new ArrayList<>(collection.size());
        SimpleFeatureIterator iterator = collection.features();
        while (iterator.hasNext()) {
            SimpleFeatureImpl feature = (SimpleFeatureImpl)iterator.next();
            // 获取属性名称和值
            final List<String> tempList = new ArrayList<>(7);
            values.add(tempList);
            final String id = feature.getID();
            tempList.add(id);
            for (int i = 0; i < featureType.getAttributeCount(); i++) {
                String attributeName = featureType.getDescriptor(i).getName().toString();
                tempList.add(String.valueOf(feature.getAttribute(attributeName)));
            }
        }
        iterator.close();
        dataStore.dispose();


        final String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/gs?ssl=false";
        final HikariConfig config = new HikariConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.postgresql.Driver");

        try (final HikariDataSource dataSource = new HikariDataSource(config)){
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            final String sql = "insert into shp_feature values (?,ST_GeomFromText(?),?,?,?,?,?)";
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    ps.setString(1, values.get(i).get(0));
                    ps.setString(2, values.get(i).get(1));
                    ps.setString(3, values.get(i).get(2));
                    ps.setString(4, values.get(i).get(3));
                    ps.setString(5, values.get(i).get(4));
                    ps.setString(6, values.get(i).get(5));
                    ps.setString(7, values.get(i).get(6));
                }

                @Override
                public int getBatchSize() {
                    return values.size();
                }
            });
        }
    }

    @Test
    public void createPostGIS(){
        final GeoServerRESTPublisher publisher = manager.getPublisher();
        final GSPostGISDatastoreEncoder encoder = new GSPostGISDatastoreEncoder("pg_ds");
        encoder.setDatabase(database);
        encoder.setHost(host);
        encoder.setPort(port);
        encoder.setUser(username);
        encoder.setPassword(password);
        encoder.setSchema("public");

        String sUrl = REST_URL + "/rest/workspaces/" + WS_NAME + "/datastores/";
        String xml = encoder.toString();
        String result = HTTPUtils.postXml(sUrl, xml, USERNAME, PASSWORD);
        LOGGER.debug(result);
    }
}

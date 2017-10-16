package sic.modelo;

import java.sql.Blob;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * Se utiliza como adaptador para poder trabajar con objetos 
 * tipo blob.
 */
public class BlobAdapter extends XmlAdapter<String, Blob> {

    @Override
    public Blob unmarshal(String v) throws Exception {
        //to do
        return null;
    }

    @Override
    public String marshal(Blob v) throws Exception {
        //to do
        return "";
    }
}

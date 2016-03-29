package navya.tech.navyatraveller.Databases;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class Factory extends Product {

    private String _productname;
    private String _factoryname;


    public Factory() {

    }

    public Factory(String productname, String factoryname) {
        this._productname = productname;
        this._factoryname = factoryname;
    }

    public void setProductName(String productname) {
        this._productname = productname;
    }

    public String getProductName() {
        return this._productname;
    }

    public void setFactoryName(String factoryname) {
        this._factoryname = factoryname;
    }

    public String getFactoryName() {
        return this._factoryname;
    }
}

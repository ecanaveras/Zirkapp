package com.ecp.gsy.dcs.zirkapp.app.util.locations;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Elder on 13/05/2015.
 */
public class ManagerAddress {

    private Location location;
    private Context context;

    public ManagerAddress(Context context, Location location) {
        this.location = location;
        this.context = context;
    }

    /**
     * Devuelve la direccion actual del dispositivo
     *
     * @return
     */
    public List<Address> getGeocoderAddress() {
        if (location != null && context != null) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocation(this.location.getLatitud(), this.location.getLongitud(), 1);
                return addressList;
            } catch (IOException e) {
                Log.e("Error : Geocoder", "Impossible to connect to Geocoder", e);
            }
        }
        return null;
    }

    /**
     * Retorna los datos de la direccion, segun el TypeAddres
     *
     * @param typeAddress
     * @return
     */
    private String getDataAddress(int typeAddress) {
        List<Address> addressList = getGeocoderAddress();
        if (addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);

            switch (typeAddress) {
                case 1:
                    return address.getAddressLine(0);
                case 2:
                    int maxLines = addressList.get(0).getMaxAddressLineIndex();
                    for (int i = 0; i < maxLines; i++) {
                        if ((maxLines - 1) == i) {
                            return addressList.get(0).getAddressLine(i);
                        }
                    }
                case 3:
                    return address.getPostalCode();
                case 4:
                    return address.getCountryName();
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Retorna la direccion resumida del dispositivo
     *
     * @return
     */
    public String getAddressLine() {
        return getDataAddress(1);
    }

    public String getLocality() {
        return getDataAddress(2);
    }

    public String getPostalCode() {
        return getDataAddress(3);
    }

    public String getCountryName() {
        return getDataAddress(4);
    }
}

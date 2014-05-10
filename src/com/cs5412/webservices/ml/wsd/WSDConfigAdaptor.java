package com.cs5412.webservices.ml.wsd;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


/**
 * Class that helps serializing WSDCongig 
 * @author pbp36
 *
 */
public class WSDConfigAdaptor implements JsonSerializer<WSDConfig> {

 @Override
 public JsonElement serialize(WSDConfig src, Type typeOfSrc,
            JsonSerializationContext context)
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("CO_WINDOW", src.getCO_WINDOW());
        obj.addProperty("CL_WINDOW",src.getCL_WINDOW());
        obj.addProperty("score",src.getScore());

        return obj;
    }
}
package com.delta.android.Core.WebApiClient;

import java.util.HashMap;

public class MesResponse {

    public MesResponse() {
    }

    public HashMap<String, TrackResponse> Tracks = new HashMap<String, TrackResponse>();

    public HashMap<String, InfoResponse> Infos = new HashMap<String, InfoResponse>();

}

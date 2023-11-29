package com.delta.android.Core.WebApiClient;

import java.util.HashMap;

public class MesRequest {


    public MesRequest() {
    }

    public HashMap<String, TrackRequest> Tracks = new HashMap<String, TrackRequest>();

    public HashMap<String, InfoRequest> Infos = new HashMap<String, InfoRequest>();
}
